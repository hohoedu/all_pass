package com.hohoedu.all_pass.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.PaymentKeyGenerator;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppReqDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.PaymentBill;
import com.hohoedu.all_pass.payment.model.PaymentCallback;
import com.hohoedu.all_pass.payment.model.PaymentDetail;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;

    // 결제 로그
    private void logHistory(String eventType, String eventSource, String oldStatus, String newStatus,
                            Integer amount, String description, String paymentKey, String userCode) {

        PaymentReqDTO.PaymentHistoryRecordDTO dto = PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                .eventType(eventType)
                .eventSource(eventSource)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .amount(amount)
                .description(description)
                .paymentKey(paymentKey)
                .userCode(userCode)
                .build();

        paymentRepository.insertPaymentHistory(dto);
    }

    public PaymentRespDTO.PaySendRespDTO sendBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PaySendReqDTO req) throws JsonProcessingException {

        PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(user.getCenterCode());


        if (conf == null) {
            throw new RuntimeException("해당 지점의 결제 설정이 없습니다. centerCode = " + user.getCenterCode());
        }

        String billId = generateBillId(conf.getPreBillId(), req.getIndex(), req.getType());

        String raw = billId + "," + req.getPhone() + "," + req.getPrice();
        String hashBillId = DigestUtils.sha256Hex(raw);

        Map<String, Object> bill = Map.of(
                "bill_id", billId,
                "product_nm", req.getType().equals("edu") ? "교육비" : "교재비",
                "message", req.getMessage(),
                "member_nm", req.getStudentName(),
                "phone", req.getPhone(),
                "price", req.getPrice(),
                "hash", hashBillId,
                "expire_dt", req.getExpireDt(),
                "callbackURL", conf.getCallbackUrl()
        );

        Map<String, Object> body = Map.of(
                "apikey", conf.getApiKey(),
                "member", conf.getMemberId(),
                "merchant", conf.getMerchantId(),
                "bill", bill
        );

        PaymentRespDTO.PaymintRespDTO paymintResp = callPaymint(conf.getSendUrl(), body);
        PaymentRespDTO.PaySendRespDTO respDTO = new PaymentRespDTO.PaySendRespDTO();
        respDTO.setBillId(billId);
        respDTO.setPaymintCode(paymintResp.getCode());
        respDTO.setPaymintMsg(paymintResp.getMsg());

        if (!"0000".equals(paymintResp.getCode())) {
            respDTO.setDbSaved(false);
            return respDTO;
        }


        boolean saved = false;

        try {
            String paymentKey = paymentRepository
                    .findLatestPaymentKeyByStudent(req.getStudentId(), req.getYy(), req.getMm());

            PaymentReqDTO.InsertBillDTO billDTO = new PaymentReqDTO.InsertBillDTO();
            billDTO.setPaymentKey(paymentKey);
            billDTO.setBillId(billId);
            billDTO.setAmount(req.getPrice());
            billDTO.setBillType(req.getType());
            billDTO.setStudentId(req.getStudentId());
            billDTO.setCenterCode(user.getCenterCode());
            billDTO.setExpireDate(req.getExpireDt());
            billDTO.setYy(req.getYy());
            billDTO.setMm(req.getMm());

            insertPaymentBill(billDTO, user.getUserCode());
            saved = true;

        } catch (Exception e) {
            log.error("DB 저장 실패", e);
            saved = false;
        }

        respDTO.setDbSaved(saved);

        return respDTO;
    }

    // ===================== 메서드 분리 ======================
    private String generateBillId(String prefix, int index, String type) {

        LocalDate now = LocalDate.now();
        LocalDate base = LocalDate.of(2025, 1, 1);

        long diffDays = ChronoUnit.DAYS.between(base, now);       // 날짜 → 36진수
        int secondsOfDay = LocalTime.now().toSecondOfDay();       // 하루 초 → 36진수

        String dayCode = Long.toString(diffDays, 36);
        String timeCode = Integer.toString(secondsOfDay, 36);

        String indexStr = String.format("%02d", index);           // 00 ~ 99
        String typeCode = type.equals("edu") ? "1" : "0";         // 교육비 = 1, 교재비 = 0

        return prefix
                + String.format("%" + 3 + "s", dayCode).replace(" ", "0")
                + String.format("%" + 4 + "s", timeCode).replace(" ", "0")
                + indexStr
                + typeCode;
    }

    private PaymentRespDTO.PaymintRespDTO callPaymint(String url, Map<String, Object> body) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<PaymentRespDTO.PaymintRespDTO> response = restTemplate.exchange(url, HttpMethod.POST, entity, PaymentRespDTO.PaymintRespDTO.class);
        System.out.println("=====================================================2");
        log.info("response = {}", response.getBody());
        System.out.println("=====================================================3");

        return response.getBody();
    }

    // ========================================================
    // 결제 생성
    public String createPayment(String studentId, String yy, String mm, String centerCode, String userCode) {

        String paymentKey = paymentRepository.findPaymentKeyByStudentAndYm(studentId, yy, mm);

        if (paymentKey != null) {
            return paymentKey;
        }

        paymentKey = PaymentKeyGenerator.generate(centerCode);

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .student(Student.builder().studentId(studentId).build())
                .center(Center.builder().centerCode(centerCode).build())
                .yy(yy)
                .mm(mm)
                .amount(0)
                .unpaidAmount(0)
                .status("pending")
                .build();

        paymentRepository.createPayment(payment);

        logHistory(
                "payment_created",
                "system",
                null,
                "pending",
                0,
                "결제 생성",
                paymentKey,
                userCode
        );

        return paymentKey;
    }

    // 결제 상세 내역 저장
    public void createPaymentDetail(String paymentKey, ClassRespDTO.ClassInfoDTO classInfoDTO, String userCode) {

        Payment payment = Payment.builder().paymentKey(paymentKey).build();

        User creator = User.builder().userCode(userCode).build();

        String classType = classInfoDTO.getClassType().equals("1") ? "한자" : "독서";

        // 1) 교육비
        PaymentDetail eduDetail = PaymentDetail.builder()
                .payment(payment)
                .user(creator)
                .itemType("EDU_FEE")
                .classType(classInfoDTO.getClassType())
                .amount(classInfoDTO.getClassFee())
                .note("수업료 (" + classType + ")")
                .timeTableKey(classInfoDTO.getTimeTableKey())
                .build();
        paymentRepository.createPaymentDetail(eduDetail);

        // 2) 교재비 (DTO에서 받아야 함)
        PaymentDetail bookDetail = PaymentDetail.builder()
                .payment(payment)
                .user(creator)
                .itemType("BOOK_FEE")
                .classType(classInfoDTO.getClassType())
                .amount(0)
                .note("교재비")
                .timeTableKey(classInfoDTO.getTimeTableKey())
                .build();
        paymentRepository.createPaymentDetail(bookDetail);

        paymentRepository.updateAmount(paymentKey);
    }


    // 수업료 청구 화면 필터링
    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode, String centerCode) {

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode, centerCode);

        return students;
    }

    // 센터별 수업별 수업료 조회
    public Integer findFeeByClassKey(String classKey, String centerCode) {
        return paymentRepository.findFeeByClassKey(classKey, centerCode);
    }

    public List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsBytudentId(PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
        return paymentRepository.findPaymentDetailsByStudentId(reqDTO.getStudentId(), reqDTO.getCount());
    }

    //     결제선생 청구서 저장
    public void insertPaymentBill(PaymentReqDTO.InsertBillDTO dto, String userCode) {
        String today = dateConfig.currentYearMonth().get("today");
        String status = "issued";
        log.info(today);

        Payment oldPayment = paymentRepository.findPaymentByKey(dto.getPaymentKey());
        log.info(oldPayment.getStatus());
        PaymentBill paymentBill = PaymentBill.builder()
                .billId(dto.getBillId())
                .payment(Payment.builder().paymentKey(dto.getPaymentKey()).build())
                .amount(dto.getAmount())
                .status(status)
                .expireDate(dto.getExpireDate())
                .issuedDate(today)
                .billType(dto.getBillType())
                .student(Student.builder().studentId(dto.getStudentId()).build())
                .center(Center.builder().centerCode(dto.getCenterCode()).build())
                .yy(dto.getYy())
                .mm(dto.getMm())
                .build();

        paymentRepository.createPaymentBill(paymentBill);

        paymentRepository.updatePaymentStatusOnIssue(dto.getPaymentKey());

        String eventType = "bill_issued";
        String eventSource = "system";
        String oldStatus = oldPayment.getStatus();
        String newStatus = status;
        Integer amount = dto.getAmount();
        String description = dto.getYy() + "년 " + dto.getMm() + "월 청구서 발행";
        String paymentKey = dto.getPaymentKey();

        logHistory(eventType, eventSource, oldStatus, newStatus, amount, description, paymentKey, userCode);

    }


    // 모달 데이터 조회
    public List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(PaymentReqDTO.PersonalDTO dto) {

        return paymentRepository.findPaymentByStudentId(dto);
    }

    public void insertPaymentCallback(PaymentReqDTO.PayCallbackDTO dto) {

        // 콜백 저장
        PaymentCallback paymentCallback = PaymentCallback.builder()
                .paymentBill(PaymentBill.builder().billId(dto.getBill_id()).build())
                .apiKey(dto.getApikey())
                .apprState(dto.getAppr_state())
                .apprDate(dto.getAppr_dt())
                .apprPayType(dto.getAppr_pay_type())
                .apprCardType(dto.getAppr_card_type())
                .apprIssuer(dto.getAppr_issuer())
                .apprNum(dto.getAppr_num())
                .build();

        paymentRepository.createPaymentCallback(paymentCallback);

        PaymentBill paymentBill = paymentRepository.findPaymentBill(dto.getBill_id());
        Payment payment = paymentRepository.findPaymentByBillId(dto.getBill_id());

        if (payment == null || paymentBill == null) {
            log.error("❌ Callback 처리 실패: payment 또는 bill 정보를 찾을 수 없음. bill_id=" + dto.getBill_id());
            return;
        }

        // payment_bill 상태 업데이트
        String rawDate = dto.getAppr_dt();
        String paidDate = rawDate.substring(0, 4) + "-" +
                rawDate.substring(4, 6) + "-" +
                rawDate.substring(6, 8) + " " +
                rawDate.substring(8, 10) + ":" +
                rawDate.substring(10, 12);
        Integer unpaidAmount = payment.getUnpaidAmount() - Integer.parseInt(dto.getAppr_price());
        paymentRepository.updateBillStatus(dto.getBill_id(), "approved");

        String newStatus = calculatePaymentStatus(payment.getPaymentKey());
        paymentRepository.updatePaymentStatus(payment.getPaymentKey(), newStatus, paidDate, unpaidAmount);

        logHistory(
                "callback_received",
                "callback",
                payment.getStatus(),
                newStatus,
                paymentBill.getAmount(),
                "Paymint 결제승인 콜백 처리",
                payment.getPaymentKey(),
                null
        );

        log.info("✅ 결제 콜백 처리 완료 (paymentKey: {}, bill_id: {})", payment.getPaymentKey(), dto.getBill_id());

    }

    public PaymentRespDTO.ManualPaymentRespDTO processManualPayment(PaymentReqDTO.ManualPaymentReqDTO dto) {

        // 1. 현재 결제건(payment) 조회
        Payment payment = paymentRepository.findByStudentAndYm(dto.getStudentId(), dto.getYear(), dto.getMonth());
        if (payment == null) {
            throw new IllegalArgumentException("해당 학생의 결제 정보가 없습니다.");
        }

        // 2. bill 조회
        List<PaymentBill> bills = paymentRepository.findBillsByPaymentKey(payment.getPaymentKey());
        if (bills == null || bills.isEmpty()) {
            throw new IllegalArgumentException("해당 결제의 청구서가 존재하지 않습니다.");
        }

        // 일반적으로 edu, material 두 개일 수 있음
        PaymentBill eduBill = bills.stream().filter(b -> "edu".equals(b.getBillType())).findFirst().orElse(null);
        PaymentBill materialBill = bills.stream().filter(b -> "material".equals(b.getBillType())).findFirst().orElse(null);

        // 3. 실제 결제된 금액 계산
        int paidEdu = (dto.getEduCard() == null ? 0 : dto.getEduCard()) +
                (dto.getEduCash() == null ? 0 : dto.getEduCash()) +
                (dto.getEduTransfer() == null ? 0 : dto.getEduTransfer());

        int paidBook = (dto.getBookCard() == null ? 0 : dto.getBookCard()) +
                (dto.getBookCash() == null ? 0 : dto.getBookCash()) +
                (dto.getBookTransfer() == null ? 0 : dto.getBookTransfer());

        int totalPaid = paidEdu + paidBook;
        int unpaidAmount = payment.getUnpaidAmount() - totalPaid;

        // 4. bill 상태 수정(= callback 시 approved와 동일)
        if (paidEdu > 0 && eduBill != null) {
            paymentRepository.updateBillStatus(eduBill.getBillId(), "approved");
        }
        if (paidBook > 0 && materialBill != null) {
            paymentRepository.updateBillStatus(materialBill.getBillId(), "approved");
        }

        // 5. payment 상태 업데이트
        String paidDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String newStatus = unpaidAmount == 0 ? "paid" : "partial_paid";

        paymentRepository.updatePaymentStatus(payment.getPaymentKey(), newStatus, paidDate, unpaidAmount);

        // 6. HISTORY 작성
        PaymentReqDTO.PaymentHistoryRecordDTO history = PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                .eventType("manual_paid")
                .eventSource("manual")
                .oldStatus(payment.getStatus())
                .newStatus(newStatus)
                .amount(totalPaid)
                .description("수기 결제 처리")
                .paymentKey(payment.getPaymentKey())
                .build();

        paymentRepository.insertPaymentHistory(history);

        // 7. bill “파기” 처리
        // delete 대신 status="canceled" 또는 "manual_paid" 등을 권장
        for (PaymentBill bill : bills) {
            paymentRepository.updateBillStatus(bill.getBillId(), "manual_paid");
        }

        PaymentBill targetBill = eduBill != null ? eduBill : materialBill;

        PaymentRespDTO.ManualPaymentRespDTO resp = new PaymentRespDTO.ManualPaymentRespDTO();
        resp.setPaymentKey(payment.getPaymentKey());
        resp.setBillId(targetBill.getBillId());
        resp.setPrice(targetBill.getAmount());
        resp.setStudentId(dto.getStudentId());
        resp.setMessage("수기 결제가 완료되었습니다.");

        log.info("수기 결제 처리 완료: student={}, paymentKey={}", dto.getStudentId(), payment.getPaymentKey());
        return resp;
    }

    @Transactional
    public void destroyBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PayDestroyReqDTO req) throws JsonProcessingException {

        PaymentRespDTO.PaymentConfigDTO conf =
                paymentRepository.findPayConfigByCenterCode(user.getCenterCode());

        if (conf == null) {
            throw new RuntimeException("해당 지점의 결제 설정이 없습니다. centerCode=" + user.getCenterCode());
        }

        String raw = req.getBillId() + "," + req.getPrice();
        String hash = DigestUtils.sha256Hex(raw);

        log.info("hash={}", hash);
        log.info("billId = {}", req.getBillId());
        log.info("price={}", req.getPrice());
        log.info("studentId={}", req.getStudentId());
        log.info("apiKey={}", conf.getApiKey());
        log.info("getMemberId={}", conf.getMemberId());
        log.info("getMerchantId={}", conf.getMerchantId());
        log.info("getPrice={}", req.getPrice());
        Map<String, Object> body = Map.of(
                "apikey", conf.getApiKey(),
                "member", conf.getMemberId(),
                "merchant", conf.getMerchantId(),
                "bill_id", req.getBillId(),
                "price", req.getPrice(),
                "hash", hash
        );
        log.info("body={}", body);
        PaymentRespDTO.PaymintRespDTO resp = callPaymint(conf.getDestroyUrl(), body);

        if (resp == null || !"0000".equals(resp.getCode())) {
            log.error("Paymint 청구서 파기 실패: {}", resp != null ? resp.getMsg() : "응답 없음");
            throw new RuntimeException("Paymint 청구서 파기 실패: " +
                    (resp != null ? resp.getMsg() : "응답 없음"));
        }

        // 4. bill 상태 변경 (destroyed)
        paymentRepository.updateBillStatus(req.getBillId(), "destroyed");

        // 5. HISTORY 기록
        PaymentReqDTO.PaymentHistoryRecordDTO history =
                PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                        .eventType("manual_bill_destroyed")
                        .eventSource("manual")
                        .oldStatus(null)
                        .newStatus("destroyed")
                        .amount(0)
                        .description("수기 결제로 인한 청구서 파기")
                        .paymentKey(req.getPaymentKey())
                        .userCode(user.getUserCode())
                        .build();

        paymentRepository.insertPaymentHistory(history);

        log.info("청구서 파기 완료 billId={}, studentId={}", req.getBillId(), req.getStudentId());
    }


    private String calculatePaymentStatus(String paymentKey) {
        List<PaymentBill> bills = paymentRepository.findBillsByPaymentKey(paymentKey);

        boolean allApproved = bills.stream()
                .allMatch(bill -> bill.getStatus().equals("approved"));

        boolean anyApproved = bills.stream()
                .anyMatch(bill -> bill.getStatus().equals("approved"));

        if (allApproved) return "approved";
        if (anyApproved) return "partial";

        return "issued";
    }


    public List<PaymentRespDTO.UnpaidStudentDTO> findUnpaidStudent(String centerCode, String userCode) {
        List<PaymentRespDTO.UnpaidStudentDTO> studentDTO = paymentRepository.findUnpaidStudent(centerCode, userCode);
        return studentDTO;
    }

    // 수업에서 학생 제거했을 때 상세내용 제거
    public void deleteDetail(String timeTableKey, String studentId) {
        String paymentKey = paymentRepository.findPaymentKeyByStudentId(studentId, timeTableKey);

        paymentRepository.deletePaymentDetail(paymentKey, timeTableKey);
    }

    public boolean insertPaymentRefund() {
        int result = paymentRepository.insertPaymentRefund();
        return result > 0;
    }

    public void cancelPayment(PaymentReqDTO.PaymentCancelReqDTO dto, UserRespDTO.LoginRespDTO user) throws JsonProcessingException {

        Payment payment = paymentRepository.findPaymentByKey(dto.getPaymentKey());
        PaymentBill paymentBill = paymentRepository.findPaymentBill(dto.getBillId());
        if (payment == null) {
            throw new IllegalStateException("결제 정보 없음");
        }

        if (!"approved".equals(payment.getStatus())) {
            throw new IllegalStateException("결제 완료 상태만 취소 가능");
        }

        PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(user.getCenterCode());


        if (conf == null) {
            throw new RuntimeException("해당 지점의 결제 설정이 없습니다. centerCode = " + user.getCenterCode());
        }

        String raw = dto.getBillId() + "," + paymentBill.getAmount();
        String hash = DigestUtils.sha256Hex(raw);

        Map<String, Object> body = Map.of(
                "apikey", conf.getApiKey(),
                "member", conf.getMemberId(),
                "merchant", conf.getMerchantId(),
                "bill_id", dto.getBillId(),
                "price", paymentBill.getAmount(),
                "hash", hash
        );

        //결제선생 취소 요청
        PaymentRespDTO.PaymintRespDTO resp = callPaymint("https://stg.paymint.co.kr/partner/if/bill/cancel", body);

            paymentRepository.updateBillStatus(dto.getBillId(), "CANCELLED");
            paymentRepository.updatePaymentStatus(payment.getPaymentKey(), "CANCELLED", payment.getPaidDate(), paymentBill.getAmount());
log.info(resp.toString());

    }
}