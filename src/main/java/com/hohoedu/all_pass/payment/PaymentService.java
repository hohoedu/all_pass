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
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;

    // ===================== 🔥 핵심 개선: Payment 상태 재계산 ====================== //
    /**
     * payment 마스터 상태 재계산
     * - 하위 테이블(detail, bill, callback, manual) 데이터 집계
     * - 상태 자동 결정
     * - 모든 하위 테이블 변경 시 호출
     */
    @Transactional
    public void recalculatePaymentStatus(String paymentKey, String userCode) {

        Payment payment = paymentRepository.findPaymentByKey(paymentKey);
        if (payment == null) {
            log.error("❌ payment 없음 paymentKey={}", paymentKey);
            return;
        }

        String oldStatus = payment.getStatus();

        // 1. detail 총 금액 조회
        int totalAmount = paymentRepository.findPaymentDetailByPaymentKey(paymentKey).stream()
                .mapToInt(v -> {
                    if (v == null || v.isBlank()) return 0;
                    try {
                        return Integer.parseInt(v);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .sum();

        // 2. bill 청구 금액 조회 (destroyed, canceled 제외)
        List<PaymentBill> bills = paymentRepository.findBillsByPaymentKey(paymentKey);
        int billedAmount = bills.stream()
                .filter(b -> !b.getStatus().equals("destroyed") && !b.getStatus().equals("canceled"))
                .mapToInt(PaymentBill::getAmount)
                .sum();

        // 3. callback 결제 금액 조회 (결제선생)
        int callbackAmount = bills.stream()
                .filter(b -> b.getStatus().equals("approved"))
                .mapToInt(b -> {
                    // bill_id로 callback 조회
                    try {
                        // 실제로는 callback 테이블 조인 필요
                        return b.getAmount();
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .sum();

        // 4. manual 결제 금액 조회 (현장결제)
        int manualAmount = 0;
        try {
            // manual 조회 로직 (기존 쿼리 활용)
            int existManual = paymentRepository.existManualByPaymentKey(
                    payment.getStudent().getStudentId(),
                    paymentKey,
                    payment.getYy(),
                    payment.getMm()
            );
            if (existManual > 0) {
                // manual 금액 계산 로직 필요
                manualAmount = 0; // TODO: manual 금액 조회 쿼리 필요
            }
        } catch (Exception e) {
            log.warn("manual 금액 조회 실패: {}", e.getMessage());
        }

        // 5. 총 결제 완료 금액
        int paidAmount = callbackAmount + manualAmount;

        // 6. 미납 금액 계산
        int unpaidAmount = totalAmount - paidAmount;

        // 7. 상태 결정
        String newStatus = determinePaymentStatus(billedAmount, paidAmount, totalAmount);

        // 8. payment 업데이트
        paymentRepository.updateAmountAndUnpaidAmountByPaymentKey(
                paymentKey,
                totalAmount,
                unpaidAmount
        );

        // 상태가 변경된 경우에만 업데이트
        if (!oldStatus.equals(newStatus)) {
            paymentRepository.updatePaymentStatus(
                    paymentKey,
                    newStatus,
                    payment.getPaidDate(),
                    unpaidAmount,
                    payment.getMethod()
            );

            // 9. 로그 기록
            logHistory(
                    "status_recalculated",
                    "system",
                    oldStatus,
                    newStatus,
                    paidAmount,
                    String.format("상태 재계산 (total:%d, paid:%d, unpaid:%d)", totalAmount, paidAmount, unpaidAmount),
                    paymentKey,
                    userCode
            );
        }

        log.info("✅ Payment 상태 업데이트 완료 paymentKey={}, status: {} → {}, total={}, paid={}, unpaid={}",
                paymentKey, oldStatus, newStatus, totalAmount, paidAmount, unpaidAmount);
    }

    /**
     * 상태 결정 로직
     */
    private String determinePaymentStatus(int billedAmount, int paidAmount, int totalAmount) {
        // 청구된 금액이 없으면 pending
        if (billedAmount == 0) {
            return "pending";
        }

        // 결제 금액이 없으면 issued
        if (paidAmount == 0) {
            return "issued";
        }

        // 전액 결제 완료
        if (paidAmount >= totalAmount) {
            return "approved";
        }

        // 부분 결제
        return "partial";
    }

    // ===================== 결제 로그 저장 ====================== //
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

    // ===================== 청구서 유효기간이 지났지만 issued인 청구서 상태 변경  ===============================//
    @Transactional
    public void destroyExpiredBills() {
        int count = paymentRepository.updateExpiredBillsToDestroyed();
        log.info("만료된 청구서 {} 건 파기 처리", count);

        // 🔥 개선: 파기된 청구서의 payment 상태 재계산
        // TODO: 영향받은 paymentKey 목록 조회 후 재계산
        // 현재는 전체 재계산하지 않고 개별 처리 시 재계산
    }

    // ===================== 빌 아이디 생성 ====================== //
    private String generateBillId(String prefix, int index, String type) {

        LocalDate now = LocalDate.now();
        LocalDate base = LocalDate.of(2025, 1, 1);

        long diffDays = ChronoUnit.DAYS.between(base, now);
        int secondsOfDay = LocalTime.now().toSecondOfDay();

        String dayCode = Long.toString(diffDays, 36);
        String timeCode = Integer.toString(secondsOfDay, 36);

        String indexStr = String.format("%02d", index);
        String typeCode = type.equals("edu") ? "1" : "0";

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

        return response.getBody();
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

    // ===================== 결제 상세 내역 저장 (개선) ===================== //
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

        // 2) 교재비
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

        // 🔥 개선: payment 상태 재계산
        recalculatePaymentStatus(paymentKey, userCode);
    }

    // ===================== 결제선생 청구서 발행 ====================== //
    public void sendBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PaySendReqDTO req) throws JsonProcessingException {

        String billType;
        if ("edu".equals(req.getType())) {
            billType = "EDU_FEE";
        } else if ("material".equals(req.getType())) {
            billType = "BOOK_FEE";
        } else {
            throw new IllegalArgumentException("잘못된 타입");
        }

        PaymentRespDTO.PaymentConfigDTO conf =
                "EDU_FEE".equals(billType)
                        ? paymentRepository.findPayConfigByCenterCode(user.getCenterCode())
                        : paymentRepository.findPayConfigByCenterCode("PUS001");

        if (conf == null) {
            throw new IllegalStateException("결제선생 설정이 없습니다.");
        }

        List<PaymentRespDTO.PayTargetDTO> targets;
        if (req.isIncludeSibling()) {
            List<String> parentPhones =
                    paymentRepository.findParentPhonesByStudentIds(req.getStudentIds());

            if (parentPhones.isEmpty()) {
                throw new RuntimeException("청구서를 발행할 번호가 없습니다.");
            }

            targets = paymentRepository.findTargetsByParentPhones(
                    parentPhones,
                    req.getYy(),
                    req.getMm(),
                    billType
            );

        } else {
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

        List<PaymentRespDTO.PayTargetDTO> adjustedTargets = new ArrayList<>();

        for (PaymentRespDTO.PayTargetDTO target : targets) {
            int billedAmount = paymentRepository.sumBilledAmountByPaymentKey(
                    target.getPaymentKey(),
                    req.getYy(),
                    req.getMm(),
                    billType,
                    Arrays.asList("issued", "approved", "partial")
            );

            int availableAmount = target.getAmount() - billedAmount;

            if (availableAmount > 0) {
                target.setAmount(availableAmount);
                adjustedTargets.add(target);
            }
        }

        if (adjustedTargets.isEmpty()) {
            throw new RuntimeException("추가 청구할 금액이 없습니다.");
        }

        Map<String, List<PaymentRespDTO.PayTargetDTO>> groupByParent =
                adjustedTargets.stream()
                        .collect(Collectors.groupingBy(
                                PaymentRespDTO.PayTargetDTO::getParentPhone
                        ));

        int seq = 1;

        for (Map.Entry<String, List<PaymentRespDTO.PayTargetDTO>> entry : groupByParent.entrySet()) {

            String parentPhone = entry.getKey();
            List<PaymentRespDTO.PayTargetDTO> group = entry.getValue();

            int totalPrice = group.stream()
                    .mapToInt(PaymentRespDTO.PayTargetDTO::getAmount)
                    .sum();

            if (totalPrice <= 0) {
                continue;
            }

            String indexStr = String.format("%02d", seq++);

            String billId = generateBillId(
                    conf.getPreBillId(),
                    Integer.parseInt(indexStr),
                    billType
            );

            String raw = billId + "," + parentPhone + "," + totalPrice;
            String hash = DigestUtils.sha256Hex(raw);

            String memberName =
                    group.size() == 1
                            ? group.get(0).getStudentName()
                            : group.get(0).getStudentName() + " 외 " + (group.size() - 1) + "명";

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

            for (PaymentRespDTO.PayTargetDTO t : group) {

                PaymentReqDTO.InsertBillDTO billDTO = new PaymentReqDTO.InsertBillDTO();

                billDTO.setBillId(billId);
                billDTO.setPaymentKey(t.getPaymentKey());
                billDTO.setStudentId(t.getStudentId());
                billDTO.setAmount(t.getAmount());
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

    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode, String centerCode) {
        log.info(userCode);
        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode, centerCode);

        return students;
    }

    public Integer findFeeByClassKey(String classKey, String centerCode) {
        return paymentRepository.findFeeByClassKey(classKey, centerCode);
    }

    public List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsBytudentId(PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
        return paymentRepository.findPaymentDetailsByStudentId(reqDTO.getStudentId(), reqDTO.getCount());
    }

    // ===================== 결제선생 청구서 저장 (개선) ===================== //
    public void insertPaymentBill(PaymentReqDTO.InsertBillDTO dto, String userCode) {
        String today = dateConfig.currentYearMonth().get("today");
        String status = "issued";

        Payment oldPayment = paymentRepository.findPaymentByKey(dto.getPaymentKey());

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

        // 🔥 개선: payment 상태 재계산
        recalculatePaymentStatus(dto.getPaymentKey(), userCode);

        logHistory(
                "bill_issued",
                "system",
                oldPayment.getStatus(),
                status,
                dto.getAmount(),
                dto.getYy() + "년 " + dto.getMm() + "월 청구서 발행",
                dto.getPaymentKey(),
                userCode
        );
    }

    public List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(PaymentReqDTO.PersonalDTO dto) {
        return paymentRepository.findPaymentByStudentId(dto);
    }

    public List<PaymentRespDTO.DetailPaymentBillDTO> findPaymentDetailsByStudentId(PaymentReqDTO.PersonalDTO dto) {
        return paymentRepository.findDetailPaymentBillByStudentId(dto.getStudentId());
    }

    public void insertPaymentCallback(PaymentReqDTO.PayCallbackDTO dto) {
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

    // ===================== 콜백 처리 (개선) ===================== //
    public void callbackProcess(PaymentReqDTO.PayCallbackDTO dto) {
        List<PaymentRespDTO.PaymentAllBillDTO> bills = paymentRepository.findPaymentBill(dto.getBill_id());

        if (bills == null || bills.isEmpty()) {
            log.error("❌ Callback 처리 실패: payment 또는 bill 정보를 찾을 수 없음. bill_id=" + dto.getBill_id());
            return;
        }

        String rawDate = dto.getAppr_dt();
        String paidDate = rawDate.substring(0, 4) + "-" +
                rawDate.substring(4, 6) + "-" +
                rawDate.substring(6, 8) + " " +
                rawDate.substring(8, 10) + ":" +
                rawDate.substring(10, 12);

        // bill 상태 업데이트
        paymentRepository.updateBillStatus(dto.getBill_id(), "approved");

        // 🔥 개선: 각 payment 상태 재계산 (중복 방지)
        Set<String> processedPaymentKeys = new HashSet<>();

        for (PaymentRespDTO.PaymentAllBillDTO bill : bills) {
            String paymentKey = bill.getPaymentKey();

            // 중복 처리 방지
            if (processedPaymentKeys.contains(paymentKey)) {
                continue;
            }
            processedPaymentKeys.add(paymentKey);

            Payment payment = paymentRepository.findPaymentByKey(paymentKey);

            if (payment == null) {
                log.error("❌ payment 없음 paymentKey={}", paymentKey);
                continue;
            }

            String oldStatus = payment.getStatus();

            // payment 상태 재계산
            recalculatePaymentStatus(paymentKey, null);

            Payment updatedPayment = paymentRepository.findPaymentByKey(paymentKey);

            logHistory(
                    "callback_received",
                    "paymint",
                    oldStatus,
                    updatedPayment.getStatus(),
                    bill.getAmount(),
                    "Paymint 결제 승인 콜백 처리",
                    paymentKey,
                    null
            );

            log.info(
                    "✅ 콜백 처리 완료 paymentKey={}, billId={}, amount={}",
                    paymentKey,
                    dto.getBill_id(),
                    bill.getAmount()
            );
        }
    }

    // ===================== 청구서 파기 (개선) ===================== //
    @Transactional
    public void destroyBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PayDestroyReqDTO req) throws JsonProcessingException {

        log.info("destroy request billId={}, paymentKey={}, type={}",
                req.getBillId(), req.getPaymentKey(), req.getDestroyType());

        String billId = req.getBillId();

        if (billId == null || billId.isEmpty()) {
            throw new RuntimeException("청구서 ID가 없습니다.");
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
            targetCenterCode = "PUS001";
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

        // 🔥 개선: 영향받은 payment 상태 재계산
        Set<String> affectedPaymentKeys = new HashSet<>();

        for (PaymentRespDTO.PaymentBillDTO bill : bills) {
            affectedPaymentKeys.add(bill.getPaymentKey());

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

        // 모든 영향받은 payment 재계산
        for (String paymentKey : affectedPaymentKeys) {
            recalculatePaymentStatus(paymentKey, user.getUserCode());
        }
    }

    public List<PaymentRespDTO.UnpaidStudentDTO> findUnpaidStudent(String centerCode, String userCode, String yy, String mm) {
        List<PaymentRespDTO.UnpaidStudentDTO> studentDTO = paymentRepository.findUnpaidStudent(centerCode, userCode, yy, mm);
        return studentDTO;
    }

    // ===================== Detail 삭제 (개선) ===================== //
    public void deleteDetail(String timeTableKey, String studentId) {
        String paymentKey = paymentRepository.findPaymentKeyByStudentId(studentId, timeTableKey);

        paymentRepository.deletePaymentDetail(paymentKey, timeTableKey);

        // 🔥 개선: payment 상태 재계산
        recalculatePaymentStatus(paymentKey, null);
    }

    public boolean insertPaymentRefund() {
        int result = paymentRepository.insertPaymentRefund();
        return result > 0;
    }

    // ===================== 결제선생 취소 요청 (개선) ===================== //
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

        List<PaymentRespDTO.PaymentBillDTO> bills =
                paymentRepository.findBillsByBillIdAndType(
                        billId,
                        req.getCancelType()
                );

        if (bills == null || bills.isEmpty()) {
            throw new RuntimeException("취소 가능한 결제 내역이 없습니다.");
        }

        if (bills.stream().anyMatch(b -> "issued".equals(b.getStatus()))) {
            throw new RuntimeException("결제 대기 상태의 청구서는 취소할 수 없습니다.");
        }

        List<PaymentRespDTO.PaymentBillDTO> approvedBills = bills.stream()
                .filter(b -> "approved".equals(b.getStatus()))
                .toList();

        if (approvedBills.isEmpty()) {
            throw new RuntimeException("결제 완료된 내역이 없습니다.");
        }

        Payment payment = paymentRepository.findPaymentByKey(req.getPaymentKey());
        if (payment == null) {
            throw new RuntimeException("결제 정보가 없습니다.");
        }

        String targetCenterCode;
        if ("BOOK_FEE".equals(req.getCancelType())) {
            targetCenterCode = "PUS001";
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

        paymentRepository.updateBillStatusByBillIdAndStatus(
                billId,
                "approved",
                "canceled"
        );

        // 🔥 개선: 영향받은 payment 상태 재계산
        Set<String> affectedPaymentKeys = new HashSet<>();

        for (PaymentRespDTO.PaymentBillDTO bill : approvedBills) {
            affectedPaymentKeys.add(bill.getPaymentKey());

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

        // 모든 영향받은 payment 재계산
        for (String paymentKey : affectedPaymentKeys) {
            recalculatePaymentStatus(paymentKey, user.getUserCode());
        }
    }

    // ===================== 교육비 업데이트 (개선) ===================== //
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

            // 🔥 개선: payment 상태 재계산
            recalculatePaymentStatus(paymentKey, null);

            paymentRepository.updateTeacherAssiginMaterialFee(
                    dto.getStudentId(),
                    dto.getHanMaterialFee(),
                    dto.getBookMaterialFee()
            );
        }
    }

    // ===================== 수기 결제 입력 (개선) ===================== //
    @Transactional
    public PaymentRespDTO.ManualPaymentRespDTO insertPaymentManual(PaymentReqDTO.ManualPaymentReqDTO reqDTO) {

        Payment payment = paymentRepository.findByStudentAndYm(
                reqDTO.getStudentId(),
                reqDTO.getYy(),
                reqDTO.getMm()
        );

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
            method = "mixed";
        }

        int paidAmount = reqDTO.getCardAmount() + reqDTO.getCashAmount() + reqDTO.getTransferAmount();

        String oldStatus = payment.getStatus();

        // detail 총 금액 조회
        PaymentRespDTO.PaymentDetailDTO detail = paymentRepository.findEduPaymentDetailByPaymentKey(reqDTO.getPaymentKey());

        // 상태 결정
        String status;
        if (paidAmount >= detail.getAmount()) {
            status = "approved";
        } else {
            status = "partial";
        }
        reqDTO.setStatus(status);

        // manual 데이터 저장
        paymentRepository.insertPaymentManual(reqDTO);

        // 🔥 개선: payment 상태 재계산
        recalculatePaymentStatus(reqDTO.getPaymentKey(), reqDTO.getUserCode());

        Payment updatedPayment = paymentRepository.findPaymentByKey(reqDTO.getPaymentKey());

        PaymentReqDTO.PaymentHistoryRecordDTO history = PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                .eventType("manual_paid")
                .eventSource("manual")
                .oldStatus(oldStatus)
                .newStatus(updatedPayment.getStatus())
                .amount(paidAmount)
                .userCode(reqDTO.getUserCode())
                .description("수기 결제 처리 (" + method + ")")
                .paymentKey(payment.getPaymentKey())
                .build();

        paymentRepository.insertPaymentHistory(history);

        PaymentRespDTO.ManualPaymentRespDTO resp = new PaymentRespDTO.ManualPaymentRespDTO();
        resp.setPaymentKey(reqDTO.getPaymentKey());
        resp.setPrice(paidAmount);
        resp.setStudentId(reqDTO.getStudentId());
        resp.setMessage("수기 결제가 완료되었습니다.");

        return resp;
    }

    public List<CardCode> findCardCode() {
        return paymentRepository.findUseCardCode();
    }

    public List<PaymentRespDTO.MonthlyPaymentDTO> findMonthlyPayments(String userCode, String centerCode, String yy, String mm) {
        return paymentRepository.findMonthlyPayments(userCode, centerCode, yy, mm)
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

        List<PaymentRespDTO.BillDetailDTO> existingBills =
                paymentRepository.findBillsByBillIds(req.getBillIds());

        log.info("🔥 조회된 청구서 개수: {}", existingBills != null ? existingBills.size() : 0);

        if (existingBills == null || existingBills.isEmpty()) {
            throw new IllegalStateException("청구서를 찾을 수 없습니다.");
        }

        long paidCount = existingBills.stream()
                .filter(b -> "PAID".equals(b.getStatus()))
                .count();

        if (paidCount > 0) {
            throw new IllegalStateException("이미 결제 완료된 청구서는 재발행할 수 없습니다.");
        }

        Map<String, List<PaymentRespDTO.BillDetailDTO>> groupByBillId =
                existingBills.stream()
                        .collect(Collectors.groupingBy(
                                PaymentRespDTO.BillDetailDTO::getBillId
                        ));

        log.info("🔥 그룹 개수: {}", groupByBillId.size());

        for (Map.Entry<String, List<PaymentRespDTO.BillDetailDTO>> entry : groupByBillId.entrySet()) {

            String billId = entry.getKey();
            List<PaymentRespDTO.BillDetailDTO> bills = entry.getValue();

            log.info("🔥 재발행 처리 - billId: {}, 학생 수: {}", billId, bills.size());

            PaymentRespDTO.BillDetailDTO representative = bills.get(0);
            String billType = representative.getBillType();

            PaymentRespDTO.PaymentConfigDTO conf =
                    "EDU_FEE".equals(billType)
                            ? paymentRepository.findPayConfigByCenterCode(user.getCenterCode())
                            : paymentRepository.findPayConfigByCenterCode("PUS001");

            log.info("🔥 결제 설정 조회 완료: {}", conf != null);

            if (conf == null) {
                throw new IllegalStateException("결제 설정 없음");
            }

            Map<String, Object> body = Map.of(
                    "apikey", conf.getApiKey() != null ? conf.getApiKey() : "",
                    "member", conf.getMemberId() != null ? conf.getMemberId() : "",
                    "merchant", conf.getMerchantId() != null ? conf.getMerchantId() : "",
                    "bill_id", billId
            );

            log.info("🔥 Paymint 호출 시작 - URL: {}", conf.getResendUrl());

            PaymentRespDTO.PaymintRespDTO paymintResp = callPaymint(
                    conf.getResendUrl(),
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