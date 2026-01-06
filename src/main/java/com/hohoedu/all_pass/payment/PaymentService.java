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
import com.hohoedu.all_pass.payment.model.CardCode;
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
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;

    // 결제 로그 저장
    private void logHistory(String eventType, String eventSource, String oldStatus, String newStatus, Integer amount, String description, String paymentKey, String userCode) {

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

    // ===================== 빌 아이디 생성 ====================== //
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

    // ===================== 페이민트 호출 ====================== //
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

    // 결제선생 url 가져오기
    public String getPaymintAccessURL(String centerCode) {
        return null;
    }

    // ===================== 결제 생성 ====================== //
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

    // ===================== 결제 상세 내역 저장 ===================== //
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
                .amount(classInfoDTO.getBookFee())
                .note("교재비")
                .timeTableKey(classInfoDTO.getTimeTableKey())
                .build();
        paymentRepository.createPaymentDetail(bookDetail);

        paymentRepository.updateAmount(paymentKey);
    }

    // ===================== 결제선생 청구서 발행 ====================== //
    public void sendBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PaySendReqDTO req) throws JsonProcessingException {



        /* =====================================================
         * 1. 타입 정규화
         * ===================================================== */
        String billType;
        if ("edu".equals(req.getType())) {
            billType = "EDU_FEE";
        } else if ("material".equals(req.getType())) {
            billType = "BOOK_FEE";
        } else {
            throw new IllegalArgumentException("잘못된 타입");
        }

        for (String studentId : req.getStudentIds()) {

            int count = paymentRepository.existsBill(
                    studentId,
                    req.getYy(),
                    req.getMm(),
                    billType
            );

            if (count > 0) {
                throw new RuntimeException(
                        "이미 "
                                + req.getMm() + "월 "
                                + ("EDU_FEE".equals(billType) ? "교육비" : "교재비")
                                + " 청구서가 발행된 학생이 있습니다."
                );
            }
        }

        /* =====================================================
         * 2. 결제 설정 조회
         * ===================================================== */
        PaymentRespDTO.PaymentConfigDTO conf =
                "EDU_FEE".equals(billType)
                        ? paymentRepository.findPayConfigByCenterCode(user.getCenterCode())
                        : paymentRepository.findPayConfigByCenterCode("PUS001");

        if (conf == null) {
            throw new IllegalStateException("결제 설정 없음");
        }

        /* =====================================================
         * 3. 청구 대상 조회
         * ===================================================== */
        List<PaymentRespDTO.PayTargetDTO> targets;
        if (req.isIncludeSibling()) {
            List<String> parentPhones =
                    paymentRepository.findParentPhonesByStudentIds(req.getStudentIds());

            if (parentPhones.isEmpty()) {
                throw new IllegalStateException("가구 정보 없음");
            }

            targets = paymentRepository.findTargetsByParentPhones(
                    parentPhones,
                    req.getYy(),
                    req.getMm(),
                    billType
            );

        } else {

            // 3-3. 기본: 선택된 학생만 조회
            targets = paymentRepository.findTargetsByStudentIds(
                    req.getStudentIds(),
                    req.getYy(),
                    req.getMm(),
                    billType
            );

            if (targets.isEmpty()) {
                throw new IllegalStateException("청구 대상 없음");
            }

        }


        /* =====================================================
         * 4. 가구(parentPhone) 기준 그룹핑
         * ===================================================== */
        Map<String, List<PaymentRespDTO.PayTargetDTO>> groupByParent =
                targets.stream()
                        .collect(Collectors.groupingBy(
                                PaymentRespDTO.PayTargetDTO::getParentPhone
                        ));

        int seq = 1;

        /* =====================================================
         * 5. 가구별 청구서 발행
         * ===================================================== */
        for (Map.Entry<String, List<PaymentRespDTO.PayTargetDTO>> entry : groupByParent.entrySet()) {

            String parentPhone = entry.getKey();
            List<PaymentRespDTO.PayTargetDTO> group = entry.getValue();

            // 5-1. 가구 전체 학생 ID
            List<String> groupStudentIds = group.stream()
                    .map(PaymentRespDTO.PayTargetDTO::getStudentId)
                    .distinct()
                    .collect(Collectors.toList());

            // 5-2. 중복 발행 체크 (가구 기준)
            int exists = paymentRepository.existsBillByStudentIds(
                    groupStudentIds,
                    req.getYy(),
                    req.getMm(),
                    billType
            );
            if (exists > 0) {
                continue;
            }

            // 5-3. 가구 금액 합산
            int totalPrice = group.stream()
                    .mapToInt(PaymentRespDTO.PayTargetDTO::getAmount)
                    .sum();

            // 5-4. billId / hash 생성
            String indexStr = String.format("%02d", seq++);

            String billId = generateBillId(
                    conf.getPreBillId(),
                    Integer.parseInt(indexStr),
                    billType
            );

            String raw = billId + "," + parentPhone + "," + totalPrice;
            String hash = DigestUtils.sha256Hex(raw);

            // 대표 이름 표시
            String memberName =
                    group.size() == 1
                            ? group.get(0).getStudentName()
                            : group.get(0).getStudentName() + " 외 " + (group.size() - 1) + "명";

            /* =====================================================
             * 5-5. Paymint 호출
             * ===================================================== */
            Map<String, Object> bill = Map.of(
                    "bill_id", billId,
                    "product_nm", "EDU_FEE".equals(billType) ? "교육비" : "교재비",
                    "message", "EDU_FEE".equals(billType)
                            ? req.getMessage()
                            : "교재비 관련 카카오페이 결제는 현재 가맹 및 시스템 연동 절차를 진행 중으로, 2026년부터 이용 가능하도록 준비하고 있습니다. 학부모님의 양해 부탁드립니다.",
                    "member_nm", memberName,
                    "phone", parentPhone,
                    "price", totalPrice,
                    "hash", hash,
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

            if (!"0000".equals(paymintResp.getCode())) {
                continue;
            }

            /* =====================================================
             * 5-6. 학생별 bill 저장 (🔥 bill_id는 가구 공통)
             * ===================================================== */
            for (PaymentRespDTO.PayTargetDTO t : group) {

                PaymentReqDTO.InsertBillDTO billDTO = new PaymentReqDTO.InsertBillDTO();

                billDTO.setBillId(billId);
                billDTO.setPaymentKey(t.getPaymentKey());
                billDTO.setStudentId(t.getStudentId());
                billDTO.setAmount(t.getAmount());   // 학생별 금액
                billDTO.setBillType(billType);
                billDTO.setPhone(parentPhone);
                billDTO.setCenterCode(user.getCenterCode());
                billDTO.setExpireDate(req.getExpireDt());
                billDTO.setYy(req.getYy());
                billDTO.setMm(req.getMm());

                insertPaymentBill(billDTO, user.getUserCode());
            }
        }
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
                .billId(dto.getBill_id())
                .apiKey(dto.getApikey())
                .apprState(dto.getAppr_state())
                .apprDate(dto.getAppr_dt())
                .apprPayType(dto.getAppr_pay_type())
                .apprCardType(dto.getAppr_card_type())
                .apprIssuer(dto.getAppr_issuer())
                .apprNum(dto.getAppr_num())
                .apprPrice(dto.getAppr_price())
                .build();

        paymentRepository.createPaymentCallback(paymentCallback);
    }

    public void callbackProcess(PaymentReqDTO.PayCallbackDTO dto) {
        String method = "paymint";
        List<PaymentRespDTO.PaymentAllBillDTO> bills = paymentRepository.findPaymentBill(dto.getBill_id());

        if (bills == null || bills.isEmpty()) {
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

        paymentRepository.updateBillStatus(dto.getBill_id(), "approved");
        for (PaymentRespDTO.PaymentAllBillDTO bill : bills) {

            Payment payment = paymentRepository.findPaymentByKey(bill.getPaymentKey());

            if (payment == null) {
                log.error("❌ payment 없음 paymentKey={}", bill.getPaymentKey());
                continue;
            }

            int newUnpaidAmount =
                    payment.getUnpaidAmount() - bill.getAmount();

            String newStatus = calculatePaymentStatus(payment.getPaymentKey());

            paymentRepository.updatePaymentStatus(
                    payment.getPaymentKey(),
                    newStatus,
                    paidDate,
                    newUnpaidAmount,
                    method
            );

            logHistory(
                    "callback_received",
                    "callback",
                    payment.getStatus(),
                    newStatus,
                    bill.getAmount(),
                    "Paymint 결제 승인 콜백 처리",
                    payment.getPaymentKey(),
                    null
            );

            log.info(
                    "✅ 콜백 처리 완료 paymentKey={}, billId={}, amount={}",
                    bill.getPaymentKey(),
                    dto.getBill_id(),
                    bill.getAmount()
            );
        }
    }


    // 청구서 파기
    @Transactional
    public void destroyBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PayDestroyReqDTO req) throws JsonProcessingException {

        log.info("destroy request paymentKey={}, type={}",
                req.getPaymentKey(), req.getDestroyType());

        String billId = paymentRepository.findBillIdByPaymentKey(req.getPaymentKey(), req.getDestroyType());

        if (billId == null) {
            throw new RuntimeException("청구서 정보가 없습니다.");
        }

        List<PaymentRespDTO.PaymentBillDTO> bills = paymentRepository.findBillsByBillIdAndType(billId, req.getDestroyType());

        if (bills == null || bills.isEmpty()) {
            throw new RuntimeException("파기 가능한 청구서가 없습니다.");
        }

        boolean allDestroyed = bills.stream()
                .allMatch(b -> "destroyed".equals(b.getStatus()));

        if (allDestroyed) {
            throw new RuntimeException("이미 파기된 청구서입니다.");
        }

        boolean hasApproved = bills.stream()
                .anyMatch(b -> "approved".equals(b.getStatus()));

        if (hasApproved) {
            throw new RuntimeException("결제 완료된 청구서는 파기할 수 없습니다.");
        }

        String targetCenterCode;

        if ("BOOK_FEE".equals(req.getDestroyType())) {
            targetCenterCode = "PUS001"; // 본사
        } else if ("EDU_FEE".equals(req.getDestroyType())) {
            targetCenterCode = user.getCenterCode();
        } else {
            throw new RuntimeException("알 수 없는 billType=" + req.getDestroyType());
        }

        PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(targetCenterCode);

        if (conf == null) {
            throw new RuntimeException("결제 설정이 없습니다. centerCode=" + targetCenterCode);
        }

        int billPrice = bills.stream()
                .mapToInt(PaymentRespDTO.PaymentBillDTO::getPrice)
                .sum();

        String raw = billId + "," + billPrice;
        String hash = DigestUtils.sha256Hex(raw);

        Map<String, Object> body = Map.of(
                "apikey", conf.getApiKey(),
                "member", conf.getMemberId(),
                "merchant", conf.getMerchantId(),
                "bill_id", billId,
                "price", billPrice,
                "hash", hash
        );

        PaymentRespDTO.PaymintRespDTO resp = callPaymint(conf.getDestroyUrl(), body);

        if (resp == null || !"0000".equals(resp.getCode())) {

            log.error("청구서 파기 실패 billId={}, msg={}", billId, resp != null ? resp.getMsg() : "응답 없음");
            throw new RuntimeException("청구서 파기 실패: " + (resp != null ? resp.getMsg() : "응답 없음"));

        }

        paymentRepository.updateBillStatus(billId, "destroyed");

        for (PaymentRespDTO.PaymentBillDTO bill : bills) {

            PaymentReqDTO.PaymentHistoryRecordDTO history =
                    PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                            .eventType("bill_destroyed")
                            .eventSource("manual")
                            .oldStatus(bill.getStatus())
                            .newStatus("destroyed")
                            .amount(bill.getPrice())
                            .description("청구서 파기 (" + req.getDestroyType() + ")")
                            .paymentKey(bill.getPaymentKey())
                            .userCode(user.getUserCode())
                            .build();

            paymentRepository.insertPaymentHistory(history);

        }
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


    public List<PaymentRespDTO.UnpaidStudentDTO> findUnpaidStudent(String centerCode, String userCode, String yy, String mm) {
        List<PaymentRespDTO.UnpaidStudentDTO> studentDTO = paymentRepository.findUnpaidStudent(centerCode, userCode, yy, mm);
        return studentDTO;
    }

    public void deleteDetail(String timeTableKey, String studentId) {
        String paymentKey = paymentRepository.findPaymentKeyByStudentId(studentId, timeTableKey);

        paymentRepository.deletePaymentDetail(paymentKey, timeTableKey);
    }

    public boolean insertPaymentRefund() {
        int result = paymentRepository.insertPaymentRefund();
        return result > 0;
    }

    //결제선생 취소 요청
    @Transactional
    public void cancelPayment(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PaymentCancelReqDTO req)
            throws JsonProcessingException {

        log.info("cancel request paymentKey={}, cancelType={}",
                req.getPaymentKey(), req.getCancelType());

        String billId = paymentRepository.findCancelBillIdByPaymentKey(
                req.getPaymentKey(),
                req.getCancelType()
        );

        if (billId == null) {
            throw new RuntimeException("취소할 청구서 정보가 없습니다.");
        }

        /* =====================================================
         * 2️⃣ billId 기준 형제 전체 bill 조회
         * ===================================================== */
        List<PaymentRespDTO.PaymentBillDTO> bills =
                paymentRepository.findBillsByBillIdAndType(
                        billId,
                        req.getCancelType()
                );

        if (bills == null || bills.isEmpty()) {
            throw new RuntimeException("취소 가능한 결제 내역이 없습니다.");
        }

        // issued 존재 시 취소 불가
        if (bills.stream().anyMatch(b -> "issued".equals(b.getStatus()))) {
            throw new RuntimeException("결제 대기 상태의 청구서는 취소할 수 없습니다.");
        }

        // approved 대상만 취소
        List<PaymentRespDTO.PaymentBillDTO> approvedBills = bills.stream()
                .filter(b -> "approved".equals(b.getStatus()))
                .toList();

        if (approvedBills.isEmpty()) {
            throw new RuntimeException("결제 완료된 내역이 없습니다.");
        }

        /* =====================================================
         * 3️⃣ 결제(payment) 조회
         * ===================================================== */
        Payment payment = paymentRepository.findPaymentByKey(req.getPaymentKey());
        if (payment == null) {
            throw new RuntimeException("결제 정보가 없습니다.");
        }

        /* =====================================================
         * 4️⃣ 결제 설정 조회
         * ===================================================== */
        String targetCenterCode;
        if ("BOOK_FEE".equals(req.getCancelType())) {
            targetCenterCode = "PUS001"; // 본사
        } else if ("EDU_FEE".equals(req.getCancelType())) {
            targetCenterCode = user.getCenterCode();
        } else {
            throw new RuntimeException("알 수 없는 cancelType=" + req.getCancelType());
        }

        PaymentRespDTO.PaymentConfigDTO conf =
                paymentRepository.findPayConfigByCenterCode(targetCenterCode);

        if (conf == null) {
            throw new RuntimeException("결제 설정이 없습니다. centerCode=" + targetCenterCode);
        }

        /* =====================================================
         * 5️⃣ 취소 금액 합산 + Paymint 취소 (billId 기준 1회)
         * ===================================================== */
        int cancelAmount = approvedBills.stream()
                .mapToInt(PaymentRespDTO.PaymentBillDTO::getPrice)
                .sum();

        String raw = billId + "," + cancelAmount;
        String hash = DigestUtils.sha256Hex(raw);

        Map<String, Object> body = Map.of(
                "apikey", conf.getApiKey(),
                "member", conf.getMemberId(),
                "merchant", conf.getMerchantId(),
                "bill_id", billId,
                "price", cancelAmount,
                "hash", hash
        );

        PaymentRespDTO.PaymintRespDTO resp = callPaymint(conf.getCancelUrl(), body);

        if (resp == null || !"0000".equals(resp.getCode())) {
            log.error("Paymint 결제 취소 실패 billId={}, msg={}",
                    billId,
                    resp != null ? resp.getMsg() : "응답 없음"
            );
            throw new RuntimeException(
                    "Paymint 결제 취소 실패: " +
                            (resp != null ? resp.getMsg() : "응답 없음")
            );
        }

        /* =====================================================
         * 6️⃣ bill 상태 변경 (approved → canceled 만)
         * ===================================================== */
        paymentRepository.updateBillStatusByBillIdAndStatus(
                billId,
                "approved",
                "canceled"
        );

        /* =====================================================
         * 7️⃣ payment 미납 금액 갱신
         * ===================================================== */
        int newUnpaidAmount = payment.getUnpaidAmount() + cancelAmount;

        paymentRepository.updatePaymentCancel(
                payment.getPaymentKey(),
                "canceled",
                payment.getPaidDate(),
                newUnpaidAmount
        );

        /* =====================================================
         * 8️⃣ HISTORY (paymentKey 기준 개별 기록)
         * ===================================================== */
        for (PaymentRespDTO.PaymentBillDTO bill : approvedBills) {

            PaymentReqDTO.PaymentHistoryRecordDTO history =
                    PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                            .eventType("payment_cancel")
                            .eventSource("manual")
                            .oldStatus("approved")
                            .newStatus("canceled")
                            .amount(bill.getPrice())
                            .description(req.getCancelReason())
                            .paymentKey(bill.getPaymentKey())
                            .userCode(user.getUserCode())
                            .build();

            paymentRepository.insertPaymentHistory(history);

            log.info(
                    "결제 취소 완료 billId={}, paymentKey={}, amount={}",
                    billId,
                    bill.getPaymentKey(),
                    bill.getPrice()
            );
        }
    }

    public void updateEduFeeAndRecalculate(PaymentReqDTO.EduFeeUpdateReqDTO dto) {
        if (dto.getStudentId() == null || dto.getYy() == null || dto.getMm() == null) {
            throw new IllegalArgumentException("필수 값 누락");
        }

        List<String> paymentKeys = paymentRepository.findPaymentKeys(dto.getStudentId(), dto.getYy(), dto.getMm());

        if (paymentKeys == null || paymentKeys.isEmpty()) {
            return;
        }

        int hanEduFee = dto.getHanEduFee() != null ? dto.getHanEduFee() : 0;
        int hanMaterialFee = dto.getHanMaterialFee() != null ? dto.getHanMaterialFee() : 0;
        int bookEduFee = dto.getBookEduFee() != null ? dto.getBookEduFee() : 0;
        int bookMaterialFee = dto.getBookMaterialFee() != null ? dto.getBookMaterialFee() : 0;

        for (String paymentKey : paymentKeys) {
            paymentRepository.updateEduFeeDetailByPaymentKey(
                    paymentKey,
                    hanEduFee,
                    hanMaterialFee,
                    bookEduFee,
                    bookMaterialFee
            );

            List<String> paymentDetails = paymentRepository.findPaymentDetailByPaymentKey(paymentKey);

            int amount = paymentDetails.stream()
                    .mapToInt(v -> {
                        if (v == null || v.isBlank()) {
                            return 0;
                        }
                        try {
                            return Integer.parseInt(v);
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .sum();

            paymentRepository.updateAmountAndUnpaidAmountByPaymentKey(paymentKey, amount, amount);
            paymentRepository.updateTeacherAssiginMaterialFee(dto.getStudentId(), dto.getHanMaterialFee(), dto.getBookMaterialFee());
        }
    }

    @Transactional
    public PaymentRespDTO.ManualPaymentRespDTO insertPaymentManual(PaymentReqDTO.ManualPaymentReqDTO reqDTO) {

        int exist = paymentRepository.existManualByPaymentKey(reqDTO.getStudentId(), reqDTO.getPaymentKey(), reqDTO.getYy(), reqDTO.getMm());

        if (exist > 1) {
            throw new RuntimeException("이미 입력된 청구서입니다.");
        }

        Payment payment = paymentRepository.findByStudentAndYm(reqDTO.getStudentId(), reqDTO.getYy(), reqDTO.getMm());
        if (payment == null) {
            throw new RuntimeException("해당 학생의 결제 정보가 없습니다.");
        }

        boolean hasCard = reqDTO.getCardAmount() != 0;
        boolean hasCash = reqDTO.getCashAmount() != 0;
        boolean hasTransfer = reqDTO.getTransferAmount() != 0;

        String method;

        if (hasCard && !hasCash && !hasTransfer) {
            method = "card";
        } else if (!hasCard && hasCash && !hasTransfer) {
            method = "cash";
        } else if (!hasCard && !hasCash && hasTransfer) {
            method = "transfer";
        } else {
            method = "mixed"; // 복합 결제
        }

        int paidEdu = (!hasCard ? 0 : reqDTO.getCardAmount()) +
                (!hasCash ? 0 : reqDTO.getCashAmount()) +
                (!hasTransfer ? 0 : reqDTO.getTransferAmount());

        int unpaidAmount = payment.getUnpaidAmount() - paidEdu;

        PaymentRespDTO.BillRespDTO bill = paymentRepository.existsBillByPaymentKey(reqDTO.getStudentId(), reqDTO.getPaymentKey(), reqDTO.getYy(), reqDTO.getMm());
//
//
//        if (bill.getCount() > 0) {
//            // 빌 있으니까 approved로 업데이트
//        }

        String status;
        PaymentRespDTO.PaymentDetailDTO detail = paymentRepository.findEduPaymentDetailByPaymentKey(reqDTO.getPaymentKey());
        int manualPrice = reqDTO.getCashAmount() + reqDTO.getCardAmount() + reqDTO.getTransferAmount();
        if (manualPrice == detail.getAmount()) {
            status = "approved";
        } else {
            status = "partial";
        }
        reqDTO.setStatus(status);


        paymentRepository.insertPaymentManual(reqDTO);
        paymentRepository.updatePaymentStatus(
                reqDTO.getPaymentKey(),
                status,
                reqDTO.getPaidDate(),
                unpaidAmount,
                method
        );

        PaymentReqDTO.PaymentHistoryRecordDTO history = PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                .eventType("manual_paid")
                .eventSource("manual")
                .oldStatus(payment.getStatus())
                .newStatus(status)
                .amount(paidEdu)
                .userCode(reqDTO.getUserCode())
                .description("수기 결제 처리")
                .paymentKey(payment.getPaymentKey())
                .build();

        paymentRepository.insertPaymentHistory(history);

        PaymentRespDTO.ManualPaymentRespDTO resp = new PaymentRespDTO.ManualPaymentRespDTO();
        resp.setPaymentKey(reqDTO.getPaymentKey());
        resp.setPrice(paidEdu);
        resp.setStudentId(reqDTO.getStudentId());
        resp.setMessage("수기 결제가 완료되었습니다.");

        return resp;
    }

    public List<CardCode> findCardCode() {
        return paymentRepository.findUseCardCode();
    }

    public List<PaymentRespDTO.MonthlyPaymentDTO> findMonthlyPayments(String centerCode, String yy, String mm) {
        List<PaymentRespDTO.MonthlyPaymentDTO> resp =
                paymentRepository.findMonthlyPayments(centerCode, yy, mm);

        return paymentRepository.findMonthlyPayments(centerCode, yy, mm)
                .stream()
                .peek(dto -> {
                    String paidDate = dto.getPaidDate();
                    dto.setPaidDate(
                            (paidDate == null || paidDate.isBlank())
                                    ? "-"
                                    : paidDate
                    );
                })
                .toList();
    }


    public void reissueBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PayReissueReqDTO req) throws JsonProcessingException {

        log.info("🔥 재발행 시작 - billIds: {}", req.getBillIds());

        if (req.getBillIds() == null || req.getBillIds().isEmpty()) {
            throw new IllegalArgumentException("재발행할 청구서를 선택하세요.");
        }

        /* =====================================================
         * 1. 기존 청구서 정보 조회
         * ===================================================== */
        List<PaymentRespDTO.BillDetailDTO> existingBills =
                paymentRepository.findBillsByBillIds(req.getBillIds());

        log.info("🔥 조회된 청구서 개수: {}", existingBills != null ? existingBills.size() : 0);

        if (existingBills == null || existingBills.isEmpty()) {
            throw new IllegalStateException("청구서를 찾을 수 없습니다.");
        }

        // 결제 완료된 청구서 체크
        long paidCount = existingBills.stream()
                .filter(b -> "PAID".equals(b.getStatus()))
                .count();

        if (paidCount > 0) {
            throw new IllegalStateException("이미 결제 완료된 청구서는 재발행할 수 없습니다.");
        }

        /* =====================================================
         * 2. billId 기준으로 그룹핑 (가구별)
         * ===================================================== */
        Map<String, List<PaymentRespDTO.BillDetailDTO>> groupByBillId =
                existingBills.stream()
                        .collect(Collectors.groupingBy(
                                PaymentRespDTO.BillDetailDTO::getBillId
                        ));

        log.info("🔥 그룹 개수: {}", groupByBillId.size());

        /* =====================================================
         * 3. 가구별 재발행 처리 (같은 billId로 재전송)
         * ===================================================== */
        for (Map.Entry<String, List<PaymentRespDTO.BillDetailDTO>> entry : groupByBillId.entrySet()) {

            String billId = entry.getKey();
            List<PaymentRespDTO.BillDetailDTO> bills = entry.getValue();

            log.info("🔥 재발행 처리 - billId: {}, 학생 수: {}", billId, bills.size());

            // 3-1. 대표 청구서 정보
            PaymentRespDTO.BillDetailDTO representative = bills.get(0);
            String billType = representative.getBillType();
            String parentPhone = representative.getPhone();

            log.info("🔥 billType: {}, parentPhone: {}", billType, parentPhone);

            /* =====================================================
             * 3-2. 결제 설정 조회
             * ===================================================== */
            PaymentRespDTO.PaymentConfigDTO conf =
                    "EDU_FEE".equals(billType)
                            ? paymentRepository.findPayConfigByCenterCode(user.getCenterCode())
                            : paymentRepository.findPayConfigByCenterCode("PUS001");

            log.info("🔥 결제 설정 조회 완료: {}", conf != null);

            if (conf == null) {
                throw new IllegalStateException("결제 설정 없음");
            }

            log.info("🔥 bill 객체 생성 완료");

            Map<String, Object> body = Map.of(
                    "apikey", conf.getApiKey() != null ? conf.getApiKey() : "",
                    "member", conf.getMemberId() != null ? conf.getMemberId() : "",
                    "merchant", conf.getMerchantId() != null ? conf.getMerchantId() : "",
                    "bill_id", billId
            );

            log.info("🔥 Paymint 호출 시작 - URL: {}", conf.getResendUrl());

            PaymentRespDTO.PaymintRespDTO paymintResp = callPaymint(
                    conf.getResendUrl(),  // 🔥 재발행은 resend URL 사용
                    body
            );

            log.info("🔥 Paymint 응답: code={}, message={}",
                    paymintResp.getCode(),
                    paymintResp.getMsg()
            );

            if (!"0000".equals(paymintResp.getCode())) {
                throw new RuntimeException("Paymint 호출 실패: " + paymintResp.getMsg());
            }
        }

        log.info("🔥 재발행 완료");
    }
}







