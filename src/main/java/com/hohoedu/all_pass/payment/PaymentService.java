package com.hohoedu.all_pass.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.KeyGenerator;
import com.hohoedu.all_pass._core.utils.PaymentKeyGenerator;
import com.hohoedu.all_pass._core.utils.SseEmitterHolder;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppReqDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO.PaidStudentListDTO;
import com.hohoedu.all_pass.payment.model.*;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SseEmitterHolder sseEmitterHolder;
    private final AtomicInteger billCounter = new AtomicInteger(0);
    private volatile long currentSecond = Instant.now().getEpochSecond();

    public List<PaymentRespDTO.MainPaymentSummaryDTO> getPaymentSummary(String centerCode, String userCode,
                                                                        String roleKey, String type) {
        String year = DateConfig.currentYearMonth().get("currentYear");
        String month = DateConfig.currentYearMonth().get("currentMonth");
        return paymentRepository.findPaymentSummary(centerCode, userCode, roleKey, type, year, month, year, "01");
    }

    // 기간별 미납 조회
    public List<PaymentRespDTO.MainPaymentSummaryDTO> getPaymentSummaryByPeriod(String centerCode, String userCode,
                                                                                String roleKey, String type) {
        String year = DateConfig.currentYearMonth().get("currentYear");
        String month = DateConfig.currentYearMonth().get("currentMonth");
        return paymentRepository.findPaymentSummaryByPeriod(centerCode, userCode, roleKey, type, year, month);
    }

    /**
     * payment 상태 재계산 (교육비 기준)
     * <p>
     * 🎯 핵심 로직:
     * - payment 상태는 교육비(EDU_FEE)만 고려
     * - 교육비 = bill(EDU_FEE) + manual 모두 포함
     * - 교재비(BOOK_FEE)는 독립적 (bill 상태로만 관리)
     * <p>
     * 상태 결정:
     * - pending: 청구된 금액이 없음
     * - issued: 청구는 되었으나 결제 금액이 없음
     * - partial: 부분 결제
     * - approved: 전액 결제
     *
     * @param paymentKey - payment 키
     * @param userCode   - 작업자 코드
     */
    @Transactional
    public void recalculatePaymentStatus(String paymentKey, String userCode) {

        Payment payment = paymentRepository.findPaymentByKey(paymentKey);
        if (payment == null) {
            log.error("❌ payment 없음 paymentKey={}", paymentKey);
            return;
        }

        String oldStatus = payment.getStatus();

        log.info("oldStatus={}", oldStatus);

        /*
         * =========================================================
         * 1️⃣ 전체 detail 금액 (교육비 + 교재비) → payment.amount 용
         * =========================================================
         */
        List<String> allDetails = paymentRepository.findPaymentDetailByPaymentKey(paymentKey);
        int totalDetailAmount = allDetails.stream().mapToInt(v -> {
            if (v == null || v.isBlank())
                return 0;
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                return 0;
            }
        }).sum();

        /*
         * =========================================================
         * 2️⃣ 교육비 detail 총액 (상태 판단 기준)
         * =========================================================
         */
        PaymentRespDTO.PaymentDetailDTO eduDetail = paymentRepository.findEduPaymentDetailByPaymentKey(paymentKey);

        int eduDetailTotal = (eduDetail != null && eduDetail.getAmount() != null) ? eduDetail.getAmount() : 0;

        /*
         * =========================================================
         * 3️⃣ 교육비 bill 승인 금액 (approved 만)
         * =========================================================
         */
        List<PaymentBill> bills = paymentRepository.findBillsByPaymentKey(paymentKey);

        int eduBillApprovedAmount = bills.stream().filter(b -> "EDU_FEE".equals(b.getBillType()))
                .filter(b -> "approved".equals(b.getStatus())).mapToInt(PaymentBill::getAmount).sum();

        /*
         * =========================================================
         * 4️⃣ manual 결제 금액 (교육비만)
         * =========================================================
         */
        int manualPaidAmount;
        try {
            manualPaidAmount = paymentRepository.sumManualAmountByPaymentKey(paymentKey);
        } catch (Exception e) {
            log.warn("manual 금액 조회 실패 paymentKey={}, msg={}", paymentKey, e.getMessage());
            manualPaidAmount = 0;
        }

        /*
         * =========================================================
         * 5️⃣ 교육비 실제 납부 금액
         * =========================================================
         */
        int eduPaidTotal = eduBillApprovedAmount + manualPaidAmount;

        /*
         * =========================================================
         * 6️⃣ 교육비 미납 금액 (핵심 공식)
         * =========================================================
         */
        int eduUnpaidAmount = eduDetailTotal - eduPaidTotal;
        if (eduUnpaidAmount < 0)
            eduUnpaidAmount = 0;

        /*
         * =========================================================
         * 7️⃣ payment 상태 결정 (교육비 기준 ONLY)
         * =========================================================
         */
        String newStatus;

        if (eduDetailTotal == 0) {
            newStatus = "pending";
        } else if (eduPaidTotal == 0) {
            newStatus = "issued";
        } else if (eduUnpaidAmount == 0) {
            newStatus = "approved";
        } else {
            newStatus = "partial";
        }

        log.info("newStatus={}", newStatus);

        /*
         * =========================================================
         * 8️⃣ payment.amount / unpaid_amount 업데이트
         * - amount : 전체(detail) 금액
         * - unpaid_amount : 전체 미납 (교육비 + 교재비 기준)
         * =========================================================
         */
        int totalPaidAmount = eduBillApprovedAmount + manualPaidAmount
                + bills.stream().filter(b -> "BOOK_FEE".equals(b.getBillType()))
                .filter(b -> "approved".equals(b.getStatus())).mapToInt(PaymentBill::getAmount).sum();

        int totalUnpaidAmount = totalDetailAmount - totalPaidAmount;
        if (totalUnpaidAmount < 0)
            totalUnpaidAmount = 0;

        paymentRepository.updateAmountAndUnpaidAmountByPaymentKey(paymentKey, totalDetailAmount, totalUnpaidAmount);

        /*
         * =========================================================
         * 9️⃣ 상태 변경 시 payment 업데이트
         * =========================================================
         */
        if (!Objects.equals(oldStatus, newStatus)) {

            String paidDate = payment.getPaidDate();
            if (eduPaidTotal > 0 && (paidDate == null || paidDate.isBlank())) {
                paidDate = DateConfig.currentYearMonth().get("today");
            }

            String method;
            if (manualPaidAmount > 0 && eduBillApprovedAmount > 0) {
                method = "mixed";
            } else if (manualPaidAmount > 0) {
                method = "manual";
            } else if (eduBillApprovedAmount > 0) {
                method = "paymint";
            } else {
                method = payment.getMethod();
            }

            paymentRepository.updatePaymentStatus(paymentKey, newStatus, paidDate, totalUnpaidAmount, method);

            logHistory("status_recalculated", "system", oldStatus, newStatus, eduPaidTotal,
                    String.format("교육비 재계산 (detail:%d, bill:%d, manual:%d, unpaid:%d)", eduDetailTotal,
                            eduBillApprovedAmount, manualPaidAmount, eduUnpaidAmount),
                    paymentKey, userCode);
        }

        log.info("✅ payment 재계산 완료 paymentKey={}, status:{}→{}, eduDetail={}, eduPaid={}, eduUnpaid={}, totalUnpaid={}",
                paymentKey, oldStatus, newStatus, eduDetailTotal, eduPaidTotal, eduUnpaidAmount, totalUnpaidAmount);
    }

    /**
     * 결제 이력 로그 저장
     * <p>
     * 용도: 모든 결제 관련 이벤트를 erp_payment_history 테이블에 기록
     *
     * @param eventType   - 이벤트 타입 (payment_created, bill_issued, callback_received
     *                    등)
     * @param eventSource - 이벤트 발생원 (system, manual, callback)
     * @param oldStatus   - 변경 전 상태
     * @param newStatus   - 변경 후 상태
     * @param amount      - 금액
     * @param description - 설명
     * @param paymentKey  - payment 키
     * @param userCode    - 작업자 코드
     */
    private void logHistory(String eventType, String eventSource, String oldStatus, String newStatus, Integer amount,
                            String description, String paymentKey, String userCode) {

        PaymentReqDTO.PaymentHistoryRecordDTO dto = PaymentReqDTO.PaymentHistoryRecordDTO.builder().eventType(eventType)
                .eventSource(eventSource).oldStatus(oldStatus).newStatus(newStatus).amount(amount)
                .description(description).paymentKey(paymentKey).userCode(userCode).build();

        paymentRepository.insertPaymentHistory(dto);
    }

    /**
     * billId 생성 (유니크 ID)
     * <p>
     * 목적: 결제선생 청구서의 고유 ID를 생성
     * <p>
     * 생성 규칙:
     * - prefix (센터별 고유값)
     * - 날짜 코드 (36진수, 3자리)
     * - 시간 코드 (36진수, 4자리)
     * - 순번 (00~99)
     * - 타입 코드 (교육비:1, 교재비:0)
     * <p>
     * 예시: ABC1a2b3c041
     *
     * @param prefix - 센터별 접두사
     * @param type   - "edu" 또는 "material"
     * @return 생성된 청구서 ID
     */
    private String generateBillId(String prefix, String type) {

        int index;
        long nowSecond;
        LocalTime localTime;

        // ===== 1. 시간 + index를 하나의 블록에서 처리 =====
        synchronized (this) {
            Instant now = Instant.now();

            nowSecond = now.getEpochSecond();
            localTime = LocalTime.ofInstant(now, ZoneId.systemDefault());

            if (nowSecond != currentSecond) {
                billCounter.set(0);
                currentSecond = nowSecond;
            }

            index = billCounter.getAndIncrement();

            if (index >= 1000) {
                throw new RuntimeException("1초당 billId 1000건 초과");
            }
        }

        // ===== 2. 기존 로직 =====
        LocalDate nowDate = LocalDate.now();
        LocalDate base = LocalDate.of(2025, 1, 1);

        long diffDays = ChronoUnit.DAYS.between(base, nowDate);
        int secondsOfDay = localTime.toSecondOfDay();

        String dayCode = String.format("%3s", Long.toString(diffDays, 36)).replace(" ", "0");
        String timeCode = String.format("%4s", Integer.toString(secondsOfDay, 36)).replace(" ", "0");
        String indexStr = String.format("%2s", Integer.toString(index, 36)).replace(" ", "0");
        String typeCode = type.equals("edu") ? "1" : "0";

        return prefix + dayCode + timeCode + indexStr + typeCode;
    }

    /**
     * 결제선생(Paymint) API 호출
     * <p>
     * 용도: 결제선생 시스템과 통신 (청구서 발행, 파기, 취소, 재발행)
     *
     * @param url  - API 엔드포인트 URL
     * @param body - 요청 데이터 (Map → JSON 변환)
     * @return Paymint 응답 객체
     * @throws JsonProcessingException JSON 변환 오류
     */
    private PaymentRespDTO.PaymintRespDTO callPaymint(String url, Map<String, Object> body)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<PaymentRespDTO.PaymintRespDTO> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                PaymentRespDTO.PaymintRespDTO.class);

        return response.getBody();
    }

    /**
     * 💳 결제 마스터 생성
     * <p>
     * 시점: 학생이 시간표에 등록될 때 호출
     * <p>
     * 기능:
     * 1. 이미 해당 년월의 payment가 있는지 확인
     * 2. 없으면 새로운 payment 생성
     * 3. 초기 상태: 'pending' (청구 전)
     * 4. 초기 금액: 0원
     * <p>
     * 테이블: erp_payment
     *
     * @param studentId  - 학생 ID
     * @param yy         - 년도
     * @param mm         - 월
     * @param centerCode - 센터 코드
     * @param userCode   - 작업자 코드
     * @return 생성된 paymentKey (또는 기존 paymentKey)
     */
    public String createPayment(String studentId, String yy, String mm, String centerCode, String userCode) {

        String paymentKey = paymentRepository.findPaymentKeyByStudentAndYm(studentId, yy, mm);

        if (paymentKey != null) {
            return paymentKey;
        }

        paymentKey = PaymentKeyGenerator.generate(centerCode);

        Payment payment = Payment.builder().paymentKey(paymentKey)
                .student(Student.builder().studentId(studentId).build())
                .center(Center.builder().centerCode(centerCode).build()).yy(yy).mm(mm).amount(0).unpaidAmount(0)
                .status("pending").build();

        paymentRepository.createPayment(payment);

        logHistory("payment_created", "system", null, "pending", 0, "결제 생성", paymentKey, userCode);

        return paymentKey;
    }

    /**
     * 📋 결제 상세 내역 저장
     * <p>
     * 시점: 시간표 등록 시 호출 (createPayment 이후)
     * <p>
     * 기능:
     * 1. 교육비(EDU_FEE) detail 생성
     * 2. 교재비(BOOK_FEE) detail 생성
     * 3. payment의 총 금액(amount, unpaidAmount) 업데이트
     * <p>
     * 테이블: erp_payment_detail
     * <p>
     * 🔥 개선: detail 생성 후 payment 상태 재계산
     */
    public void createPaymentDetail(String paymentKey, ClassRespDTO.ClassInfoDTO classInfoDTO, String userCode) {

        Payment payment = Payment.builder().paymentKey(paymentKey).build();
        User creator = User.builder().userCode(userCode).build();

        String classType = classInfoDTO.getClassType().equals("1") ? "한자" : "독서";

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
        // recalculatePaymentStatus(paymentKey, userCode);
    }

    /**
     * 결제선생 청구서 발행 (핵심 메서드)
     * <p>
     * 시점: 관리자가 청구서 발행 버튼 클릭 시
     * <p>
     * 처리 흐름:
     * 1️⃣ 타입 정규화 (edu → EDU_FEE, material → BOOK_FEE)
     * 2️⃣ 결제 설정 조회 (센터별 API 키 등)
     * 3️⃣ 청구 대상 조회
     * - 형제 포함 여부에 따라 부모 전화번호 기준 또는 학생 ID 기준
     * 4️⃣ 기발행 금액 차감
     * - 이미 발행된 청구서 금액을 제외한 나머지만 청구
     * 5️⃣ 가구(부모 전화번호) 기준 그룹핑
     * 6️⃣ 가구별 청구서 발행
     * - billId 생성
     * - Paymint API 호출
     * - 학생별 bill 저장
     * <p>
     * 테이블: erp_payment_bill
     */
    @Transactional
    public Map<String, Integer> sendBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PaySendReqDTO req)
            throws JsonProcessingException {
        log.info("req = {}", req.toString());

        // 1. 청구 타입 결정
        String billType;
        if ("edu".equals(req.getType())) {
            billType = "EDU_FEE";
        } else if ("material".equals(req.getType())) {
            billType = "BOOK_FEE";
        } else {
            throw new IllegalArgumentException("잘못된 타입: " + req.getType());
        }

        // 2. 결제 설정 조회
        String configCenterCode = "EDU_FEE".equals(billType) ? user.getCenterCode() : "PUS001";
        PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(configCenterCode);

        if (conf == null) {
            throw new IllegalStateException("결제 설정이 없습니다.");
        }

        // 3. 청구 대상 조회
        List<PaymentRespDTO.PayTargetDTO> targets;
        if (req.isIncludeSibling()) {
            List<String> parentPhones = paymentRepository.findParentPhonesByStudentIds(req.getStudentIds());

            if (parentPhones.isEmpty()) {
                throw new RuntimeException("청구서를 발행할 번호가 없습니다.");
            }

            targets = paymentRepository.findTargetsByParentPhones(parentPhones, req.getYy(), req.getMm(), billType);
        } else {
            targets = paymentRepository.findTargetsByStudentIds(req.getStudentIds(), req.getYy(), req.getMm(),
                    billType);
        }

        if (targets.isEmpty()) {
            throw new IllegalStateException("청구 대상이 없습니다.");
        }

        // 4. 청구 가능 여부 체크 및 금액 계산
        List<PaymentRespDTO.PayTargetDTO> finalTargets = new ArrayList<>();

        for (PaymentRespDTO.PayTargetDTO target : targets) {
            String paymentKey = target.getPaymentKey();
            String studentId = target.getStudentId();

            Payment payment = paymentRepository.findPaymentByKey(paymentKey);
            if (payment == null) {
                log.warn("payment 없음 - paymentKey: {}", paymentKey);
                continue;
            }

            // 교육비만 payment.status로 승인 완료 체크
            if ("EDU_FEE".equals(billType) && "approved".equals(payment.getStatus())) {

                // approved여도 추가 청구 가능 금액이 있으면 통과
                int totalDetailAmount = target.getAmount();
                int billedAmount = paymentRepository.sumBilledAmountByPaymentKey(
                        paymentKey, req.getYy(), req.getMm(), billType,
                        Arrays.asList("issued", "approved"));

                if (totalDetailAmount <= billedAmount) {
                    log.info("✅ 청구 제외 - payment.status가 approved이고 추가 청구 금액 없음");
                    continue;
                }

                log.info("✅ approved 상태이나 추가 청구 금액 존재 - 청구 진행");
            }

            // detail 총 금액 (현재 청구해야 할 총 금액)
            int totalDetailAmount = target.getAmount();

            // 직접입력 금액이 있으면 사용
            int requestAmount = req.getCustomPrice() != null ? req.getCustomPrice() : totalDetailAmount;

            // detail보다 큰 금액은 청구 불가
            if (requestAmount > totalDetailAmount) {
                log.warn("청구 불가 - paymentKey: {}, 요청 금액({})이 detail 총액({})보다 큼", paymentKey, requestAmount,
                        totalDetailAmount);
                continue;
            }

            // 이미 청구된 bill 금액 조회 (destroyed, canceled 제외)
            int billedAmount = paymentRepository.sumBilledAmountByPaymentKey(paymentKey, req.getYy(), req.getMm(),
                    billType, Arrays.asList("issued", "approved"));

            // manual 결제 금액 조회 (교육비만 해당)
            int manualPaidAmount = 0;
            if ("EDU_FEE".equals(billType)) {
                try {
                    manualPaidAmount = paymentRepository.sumManualAmountByPaymentKey(paymentKey);
                } catch (Exception e) {
                    log.warn("manual 금액 조회 실패: {}", e.getMessage());
                    manualPaidAmount = 0;
                }
            }

            // 청구 가능 금액 계산
            int availableAmount = requestAmount - billedAmount - manualPaidAmount;

            // 과목 추가 케이스 판단
            boolean isAdditionalCharge = false;
            String chargeReason = "";

            if (billedAmount > 0 && availableAmount > 0) {
                isAdditionalCharge = true;
                chargeReason = String.format("과목 추가로 인한 추가 청구 (기존: %d원, 현재: %d원, 추가: %d원)", billedAmount, requestAmount,
                        availableAmount);
                log.info("✅ 추가 청구 - studentId: {}, paymentKey: {}, {}", studentId, paymentKey, chargeReason);
            }

            if (availableAmount <= 0) {
                if (billedAmount > 0) {
                    log.info("✅ 청구 제외 - studentId: {}, paymentKey: {}, 이미 전액 청구됨 (요청: {}, bill: {}, manual: {})",
                            studentId, paymentKey, requestAmount, billedAmount, manualPaidAmount);
                } else {
                    log.info("✅ 청구 제외 - studentId: {}, paymentKey: {}, 추가 청구 금액 없음 (요청: {}, bill: {}, manual: {})",
                            studentId, paymentKey, requestAmount, billedAmount, manualPaidAmount);
                }
                continue;
            }

            target.setAmount(availableAmount);
            target.setAdditionalCharge(isAdditionalCharge);
            finalTargets.add(target);

            if (isAdditionalCharge) {
                log.info("✅✅ 추가 청구 대상 - studentId: {}, 학생: {}, paymentKey: {}, 추가 금액: {} (기존 bill: {}, 현재 total: {})",
                        studentId, target.getStudentName(), paymentKey, availableAmount, billedAmount, requestAmount);
            } else {
                log.info("✅ 신규 청구 대상 - studentId: {}, 학생: {}, paymentKey: {}, 청구 금액: {} (요청: {}, bill: {}, manual: {})",
                        studentId, target.getStudentName(), paymentKey, availableAmount, requestAmount, billedAmount,
                        manualPaidAmount);
            }
        }

        if (finalTargets.isEmpty()) {
            throw new RuntimeException("추가 청구할 대상이 없습니다. 모든 대상이 이미 청구 완료되었거나 청구 조건을 만족하지 않습니다.");
        }

        long additionalChargeCount = finalTargets.stream().filter(t -> t.isAdditionalCharge()).count();
        long newChargeCount = finalTargets.size() - additionalChargeCount;
        log.info("✅ 청구 대상 분류 - 신규 청구: {}건, 추가 청구: {}건", newChargeCount, additionalChargeCount);

        // 5. 부모별로 그룹화
        Map<String, List<PaymentRespDTO.PayTargetDTO>> groupByParent = finalTargets.stream()
                .collect(Collectors.groupingBy(t -> (t.getBillingPhone() != null && !t.getBillingPhone().isBlank())
                        ? t.getBillingPhone()
                        : t.getParentPhone()));

        log.info("✅ 부모별 그룹 수: {}", groupByParent.size());

        int total = groupByParent.size();
        int current = 0;
        int seq = 1;
        int successCount = 0;
        int failCount = 0;

        for (Map.Entry<String, List<PaymentRespDTO.PayTargetDTO>> entry : groupByParent.entrySet()) {
            current++;
            String sendPhone = entry.getKey();
            List<PaymentRespDTO.PayTargetDTO> group = entry.getValue();

            sendProgress(req.getJobId(), current, total, "processing",
                    group.get(0).getStudentName(), successCount, failCount);

            int totalPrice = group.stream().mapToInt(PaymentRespDTO.PayTargetDTO::getAmount).sum();

            if (totalPrice <= 0) {
                log.warn("청구 금액이 0 이하 - 부모 번호: {}", sendPhone);
                failCount++;
                continue;
            }

            boolean hasAdditionalCharge = group.stream().anyMatch(PaymentRespDTO.PayTargetDTO::isAdditionalCharge);

            try {
                String billId = generateBillId(conf.getPreBillId(), billType);

                String raw = billId + "," + sendPhone + "," + totalPrice;
                String hash = DigestUtils.sha256Hex(raw);

                String memberName = group.size() == 1
                        ? group.get(0).getStudentName()
                        : group.get(0).getStudentName() + " 외 " + (group.size() - 1) + "명";

                String message;
                if ("EDU_FEE".equals(billType)) {
                    if (hasAdditionalCharge) {
                        message = req.getMessage() + "\n※ 과목 추가로 인한 추가 청구서가 포함되어 있습니다.";
                    } else {
                        message = req.getMessage();
                    }
                } else {
                    if (hasAdditionalCharge) {
                        message = "교재비 관련 카카오페이 결제는 현재 가맹 및 시스템 연동 절차를 진행 중으로, "
                                + "2026년부터 이용 가능하도록 준비하고 있습니다. 학부모님의 양해 부탁드립니다."
                                + "\n※ 교재 추가로 인한 추가 청구서가 포함되어 있습니다.";
                    } else {
                        message = "교재비 관련 카카오페이 결제는 현재 가맹 및 시스템 연동 절차를 진행 중으로, "
                                + "2026년부터 이용 가능하도록 준비하고 있습니다. 학부모님의 양해 부탁드립니다.";
                    }
                }

                Map<String, Object> bill = Map.of(
                        "bill_id", billId,
                        "product_nm", "EDU_FEE".equals(billType) ? "교육비" : "교재비",
                        "message", message,
                        "member_nm", memberName,
                        "phone", sendPhone,
                        "price", totalPrice,
                        "hash", hash,
                        "expire_dt", req.getExpireDt(),
                        "callbackURL", conf.getCallbackUrl());

                Map<String, Object> body = Map.of(
                        "apikey", conf.getApiKey(),
                        // "apikey", "TEST-API-KEY-TALK",
                        "member", conf.getMemberId(),
                        // "member", "TEST-MEMBER-FOR-API",
                        "merchant", conf.getMerchantId(),
                        // "merchant", "TEST-MERCHANT-FOR-API",
                        "bill", bill);

                log.info("✅ 결제 API 호출 - billId: {}, phone: {}, amount: {}, 형제 수: {}, 추가청구: {}",
                        billId, sendPhone, totalPrice, group.size(), hasAdditionalCharge ? "Yes" : "No");
                PaymentRespDTO.PaymintRespDTO paymintResp = callPaymint(conf.getSendUrl(), body);
                // PaymentRespDTO.PaymintRespDTO paymintResp =
                // callPaymint("https://stg.paymint.co.kr/partner/if/bill/send", body);

                if (!"0000".equals(paymintResp.getCode())) {
                    log.error("결제 API 실패 - billId: {}, code: {}, message: {}", billId, paymintResp.getCode(),
                            paymintResp.getMsg());
                    failCount++;
                    continue;
                }

                log.info("✅ 결제 API 성공 - billId: {}", billId);

                for (PaymentRespDTO.PayTargetDTO target : group) {
                    PaymentReqDTO.InsertBillDTO billDTO = new PaymentReqDTO.InsertBillDTO();
                    billDTO.setBillId(billId);
                    billDTO.setPaymentKey(target.getPaymentKey());
                    billDTO.setStudentId(target.getStudentId());
                    billDTO.setAmount(target.getAmount());
                    billDTO.setBillType(billType);
                    billDTO.setPhone(sendPhone);
                    billDTO.setCenterCode(user.getCenterCode());
                    billDTO.setExpireDate(req.getExpireDt());
                    billDTO.setYy(req.getYy());
                    billDTO.setMm(req.getMm());

                    insertPaymentBill(billDTO, user.getUserCode());

                    String chargeType = target.isAdditionalCharge() ? "추가 청구" : "신규 청구";
                    log.info("✅ Bill 저장 완료 - studentId: {}, paymentKey: {}, amount: {}, type: {}",
                            target.getStudentId(), target.getPaymentKey(), target.getAmount(), chargeType);
                }

                successCount++;

            } catch (Exception e) {
                log.error("청구서 발행 실패 - 부모 번호: {}, 오류: {}", sendPhone, e.getMessage(), e);
                failCount++;
            }

        }

        sendProgress(req.getJobId(), total, total, "done", null, successCount, failCount);
        sseEmitterHolder.complete(req.getJobId());

        log.info("✅ 청구서 발행 완료 - 성공: {}건, 실패: {}건", successCount, failCount);
        return Map.of("successCount", successCount, "failCount", failCount);
    }

    private void sendProgress(String jobId, int current, int total, String status,
                              String studentName, int successCount, int failCount) {
        if (jobId == null || jobId.isBlank())
            return;

        Map<String, Object> data = new HashMap<>();
        data.put("current", current);
        data.put("total", total);
        data.put("status", status);
        data.put("successCount", successCount);
        data.put("failCount", failCount);
        if (studentName != null) {
            data.put("studentName", studentName);
        }
        sseEmitterHolder.send(jobId, data);
    }

    /**
     * 결제선생 청구서 저장
     * <p>
     * 시점: sendBill 메서드에서 Paymint API 호출 성공 후 호출
     * <p>
     * 기능:
     * 1. erp_payment_bill 테이블에 청구서 저장
     * 2. 교육비 bill인 경우 payment 상태 재계산
     * 3. 이력 로그 저장
     * <p>
     * 🔥 개선:
     * - 교육비(EDU_FEE) bill → payment 상태 재계산
     * - 교재비(BOOK_FEE) bill → 독립적 (payment 영향 없음)
     */
    public void insertPaymentBill(PaymentReqDTO.InsertBillDTO dto, String userCode) {
        String today = DateConfig.currentYearMonth().get("today");
        String status = "issued";

        Payment oldPayment = paymentRepository.findPaymentByKey(dto.getPaymentKey());

        PaymentBill paymentBill = PaymentBill.builder().billId(dto.getBillId())
                .payment(Payment.builder().paymentKey(dto.getPaymentKey()).build()).amount(dto.getAmount())
                .status(status).expireDate(dto.getExpireDate()).issuedDate(today).billType(dto.getBillType())
                .student(Student.builder().studentId(dto.getStudentId()).build())
                .center(Center.builder().centerCode(dto.getCenterCode()).build()).yy(dto.getYy()).mm(dto.getMm())
                .build();

        paymentRepository.createPaymentBill(paymentBill);

        if ("EDU_FEE".equals(dto.getBillType())) {
            recalculatePaymentStatus(dto.getPaymentKey(), userCode);
        }

        logHistory("bill_issued", "system", oldPayment.getStatus(), status, dto.getAmount(),
                dto.getYy() + "년 " + dto.getMm() + "월 청구서 발행 (" + dto.getBillType() + ")", dto.getPaymentKey(),
                userCode);
    }

    /**
     * 수업료 청구 화면 데이터 조회
     */
    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode,
                                                                      String centerCode, String itemType) {
        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode,
                centerCode, itemType);
        return students;
    }

    /**
     * 센터별 수업별 수업료 조회
     */
    public Integer findFeeByClassKey(String classKey, String centerCode) {
        return paymentRepository.findFeeByClassKey(classKey, centerCode);
    }

    /**
     * 앱용 납부내역 조회
     */
    public List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsBytudentId(
            PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
        return paymentRepository.findPaymentDetailsByStudentId(reqDTO.getStudentId(), reqDTO.getCount());
    }

    /**
     * 학생별 결제 정보 모달 조회
     */
    public List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(PaymentReqDTO.PersonalDTO dto) {
        return paymentRepository.findPaymentByStudentId(dto);
    }

    /**
     * 학생별 결제 상세 내역 조회
     */
    public List<PaymentRespDTO.DetailPaymentBillDTO> findPaymentDetailsByStudentId(PaymentReqDTO.PersonalDTO dto) {
        return paymentRepository.findDetailPaymentBillByStudentId(dto.getStudentId());
    }

    /**
     * 결제선생 콜백 데이터 저장
     */
    public void insertPaymentCallback(PaymentReqDTO.PayCallbackDTO dto) {

        log.info("paymentCallBack = {}", dto.toString());
        PaymentCallback paymentCallback = PaymentCallback.builder().apiKey(dto.getApikey()) // 연동코드
                .billId(dto.getBill_id()) // 청구서 ID
                .apprCatId(dto.getAppr_cat_id()).apprPayType(dto.getAppr_pay_type()) // 결제수단
                .apprCardType(dto.getAppr_card_type()) // 결제카드 종류
                .apprDate(dto.getAppr_dt()) // 승인일시
                .apprIssuer(dto.getAppr_issuer()) // 결제은행 / 카드명
                .apprIssuerCode(dto.getAppr_issuer_cd()) // 은행코드
                .apprIssuerNum(dto.getAppr_issuer_num()) // 결제 카드번호
                .apprNum(dto.getAppr_num()) // 승인번호
                .apprPrice(dto.getAppr_price()) // 승인금액
                .apprState(dto.getAppr_state()) // 결제상태
                .apprMonthly(dto.getAppr_monthly()) // 결제 할부 개월 수
                .apprAcquirerCode(dto.getAppr_acquirer_cd()) // 매입사 코드
                .apprAcquirerName(dto.getAppr_acquirer_nm()) // 매입사 명
                .apprOriginDate(dto.getAppr_origin_dt()) // 원거래 일시
                .apprOriginNum(dto.getAppr_origin_num()) // 원거래 승인번호
                .build();

        paymentRepository.createPaymentCallback(paymentCallback);
    }

    /**
     * 🔄 결제선생 콜백 처리 (핵심 메서드)
     * <p>
     * 처리 흐름:
     * 1️⃣ bill_id로 해당하는 모든 bill 조회 (형제 포함 가능)
     * 2️⃣ bill 상태를 'approved'로 변경
     * 3️⃣ bill_type 확인:
     * - EDU_FEE: payment 상태 재계산 (bill + manual 고려)
     * - BOOK_FEE: 독립적 (payment 영향 없음)
     * <p>
     * 🔥 개선: 교육비/교재비 분리 처리
     */
    public void callbackProcess(PaymentReqDTO.PayCallbackDTO dto) {
        String method = "paymint";
        List<PaymentRespDTO.PaymentAllBillDTO> bills = paymentRepository.findPaymentBill(dto.getBill_id());

        if (bills == null || bills.isEmpty()) {
            log.error("❌ Callback 처리 실패: payment 또는 bill 정보를 찾을 수 없음. bill_id=" + dto.getBill_id());
            return;
        }

        String rawDate = dto.getAppr_dt();
        String paidDate = rawDate.substring(0, 4) + "-" + rawDate.substring(4, 6) + "-" + rawDate.substring(6, 8) + " "
                + rawDate.substring(8, 10) + ":" + rawDate.substring(10, 12);

        // bill 상태를 'approved'로 변경
        paymentRepository.updateBillStatus(dto.getBill_id(), "approved");

        // 🔥 개선: 교육비 bill만 payment 상태에 영향
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

            // 🔥 개선: bill_type 확인 후 분기 처리
            List<PaymentBill> paymentBills = paymentRepository.findBillsByPaymentKey(paymentKey);
            boolean hasEduBill = paymentBills.stream()
                    .anyMatch(b -> "EDU_FEE".equals(b.getBillType()) && dto.getBill_id().equals(b.getBillId()));

            if (hasEduBill) {
                // 교육비 bill → payment 상태 재계산
                recalculatePaymentStatus(paymentKey, null);

                Payment updatedPayment = paymentRepository.findPaymentByKey(paymentKey);

                logHistory("callback_received", "callback", oldStatus, updatedPayment.getStatus(), bill.getAmount(),
                        "Paymint 교육비 결제 승인 콜백 처리", paymentKey, null);
            } else {
                // 교재비 bill → payment 상태에 영향 없음 (bill.status로만 관리)
                logHistory("callback_received", "callback", null, "approved", bill.getAmount(),
                        "Paymint 교재비 결제 승인 콜백 처리 (payment 영향 없음)", paymentKey, null);
            }

            log.info("✅ 콜백 처리 완료 paymentKey={}, billId={}, amount={}, type={}", paymentKey, dto.getBill_id(),
                    bill.getAmount(), hasEduBill ? "교육비" : "교재비");
        }
    }

    /**
     * 청구서 파기
     * <p>
     * 🔥 개선:
     * - EDU_FEE bill 파기: payment 상태 재계산
     * - BOOK_FEE bill 파기: 독립적 (payment 영향 없음)
     */
    @Transactional
    public void destroyBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PayDestroyReqDTO req)
            throws JsonProcessingException {

        log.info("destroy request billId={}, paymentKey={}, type={}", req.getBillId(), req.getPaymentKey(),
                req.getDestroyType());

        String billId = req.getBillId();

        if (billId == null || billId.isEmpty()) {
            throw new RuntimeException("청구서 ID가 없습니다.");
        }

        List<PaymentRespDTO.PaymentBillDTO> bills = paymentRepository.findBillsByBillIdAndType(billId,
                req.getDestroyType());

        if (bills == null || bills.isEmpty()) {
            throw new RuntimeException("파기 가능한 청구서가 없습니다.");
        }

        boolean allDestroyed = bills.stream().allMatch(b -> "destroyed".equals(b.getStatus()));

        if (allDestroyed) {
            throw new RuntimeException("이미 파기된 청구서입니다.");
        }

        boolean hasApproved = bills.stream().anyMatch(b -> "approved".equals(b.getStatus()));

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

        int billPrice = bills.stream().mapToInt(PaymentRespDTO.PaymentBillDTO::getPrice).sum();

        String raw = billId + "," + billPrice;
        String hash = DigestUtils.sha256Hex(raw);

        Map<String, Object> body = Map.of("apikey", conf.getApiKey(), "member", conf.getMemberId(), "merchant",
                conf.getMerchantId(), "bill_id", billId, "price", billPrice, "hash", hash);

        PaymentRespDTO.PaymintRespDTO resp = callPaymint(conf.getDestroyUrl(), body);

        if (resp == null || !"0000".equals(resp.getCode())) {
            log.error("청구서 파기 실패 billId={}, msg={}", billId, resp != null ? resp.getMsg() : "응답 없음");
            throw new RuntimeException("청구서 파기 실패: " + (resp != null ? resp.getMsg() : "응답 없음"));
        }

        paymentRepository.updateBillStatus(billId, "destroyed");

        // 🔥 개선: 교육비 bill 파기 시에만 payment 상태 재계산
        Set<String> affectedPaymentKeys = new HashSet<>();

        for (PaymentRespDTO.PaymentBillDTO bill : bills) {
            affectedPaymentKeys.add(bill.getPaymentKey());

            PaymentReqDTO.PaymentHistoryRecordDTO history = PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                    .eventType("bill_destroyed").eventSource("manual").oldStatus(bill.getStatus())
                    .newStatus("destroyed").amount(bill.getPrice()).description("청구서 파기 (" + req.getDestroyType() + ")")
                    .paymentKey(bill.getPaymentKey()).userCode(user.getUserCode()).build();

            paymentRepository.insertPaymentHistory(history);
        }

        // 교육비 bill 파기 시에만 payment 상태 재계산
        if ("EDU_FEE".equals(req.getDestroyType())) {
            for (String paymentKey : affectedPaymentKeys) {
                recalculatePaymentStatus(paymentKey, user.getUserCode());
            }
        }
        // 교재비는 독립적이므로 payment 영향 없음
    }

    /**
     * 미납 학생 조회
     */
    public List<PaymentRespDTO.UnpaidStudentDTO> findUnpaidStudent(String centerCode, String userCode, String yy,
                                                                   String mm) {
        List<PaymentRespDTO.UnpaidStudentDTO> studentDTO = paymentRepository.findUnpaidStudent(centerCode, userCode, yy,
                mm);
        return studentDTO;
    }

    /**
     * Detail 삭제
     * <p>
     * 🔥 개선: detail 삭제 후 payment 상태 재계산
     */
    public void deleteDetail(String timeTableKey, String studentId) {
        String paymentKey = paymentRepository.findPaymentKeyByStudentId(studentId, timeTableKey);

        paymentRepository.deletePaymentDetail(paymentKey, timeTableKey);

        recalculatePaymentStatus(paymentKey, null);
    }

    /**
     * 환불 데이터 저장
     */
    public boolean insertPaymentRefund() {
        int result = paymentRepository.insertPaymentRefund();
        return result > 0;
    }

    /**
     * 결제선생 취소 요청
     * <p>
     * 🔥 개선:
     * - EDU_FEE 취소: payment 상태 재계산
     * - BOOK_FEE 취소: 독립적 (payment 영향 없음)
     */
    @Transactional
    public void cancelPayment(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PaymentCancelReqDTO req)
            throws JsonProcessingException {

        log.info("cancel request paymentKey={}, cancelType={}", req.getPaymentKey(), req.getCancelType());

        String billId = paymentRepository.findCancelBillIdByPaymentKey(req.getPaymentKey(), req.getCancelType());

        if (billId == null) {
            throw new RuntimeException("취소할 청구서 정보가 없습니다.");
        }

        List<PaymentRespDTO.PaymentBillDTO> bills = paymentRepository.findBillsByBillIdAndType(billId,
                req.getCancelType());

        if (bills == null || bills.isEmpty()) {
            throw new RuntimeException("취소 가능한 결제 내역이 없습니다.");
        }

        if (bills.stream().anyMatch(b -> "issued".equals(b.getStatus()))) {
            throw new RuntimeException("결제 대기 상태의 청구서는 취소할 수 없습니다.");
        }

        List<PaymentRespDTO.PaymentBillDTO> approvedBills = bills.stream().filter(b -> "approved".equals(b.getStatus()))
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

        PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(targetCenterCode);

        if (conf == null) {
            throw new RuntimeException("결제 설정이 없습니다. centerCode=" + targetCenterCode);
        }

        int cancelAmount = approvedBills.stream().mapToInt(PaymentRespDTO.PaymentBillDTO::getPrice).sum();

        String raw = billId + "," + cancelAmount;
        String hash = DigestUtils.sha256Hex(raw);

        Map<String, Object> body = Map.of("apikey", conf.getApiKey(), "member", conf.getMemberId(), "merchant",
                conf.getMerchantId(), "bill_id", billId, "price", cancelAmount, "hash", hash);

        PaymentRespDTO.PaymintRespDTO resp = callPaymint(conf.getCancelUrl(), body);

        if (resp == null || !"0000".equals(resp.getCode())) {
            log.error("Paymint 결제 취소 실패 billId={}, msg={}", billId, resp != null ? resp.getMsg() : "응답 없음");
            throw new RuntimeException("Paymint 결제 취소 실패: " + (resp != null ? resp.getMsg() : "응답 없음"));
        }

        paymentRepository.updateBillStatusByBillIdAndStatus(billId, "approved", "canceled");

        // 🔥 개선: 교육비 취소 시에만 payment 상태 재계산
        Set<String> affectedPaymentKeys = new HashSet<>();

        for (PaymentRespDTO.PaymentBillDTO bill : approvedBills) {
            affectedPaymentKeys.add(bill.getPaymentKey());

            PaymentReqDTO.PaymentHistoryRecordDTO history = PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                    .eventType("payment_cancel").eventSource("manual").oldStatus("approved").newStatus("canceled")
                    .amount(bill.getPrice()).description(req.getCancelReason()).paymentKey(bill.getPaymentKey())
                    .userCode(user.getUserCode()).build();

            paymentRepository.insertPaymentHistory(history);

            log.info("결제 취소 완료 billId={}, paymentKey={}, amount={}", billId, bill.getPaymentKey(), bill.getPrice());
        }

        // 교육비 취소 시에만 payment 상태 재계산
        if ("EDU_FEE".equals(req.getCancelType())) {
            for (String paymentKey : affectedPaymentKeys) {
                recalculatePaymentStatus(paymentKey, user.getUserCode());
            }
        }
        // 교재비는 독립적이므로 payment 영향 없음
    }

    /**
     * 교육비/교재비 수정 및 재계산
     * <p>
     * 🔥 개선: detail 수정 후 payment 상태 재계산
     */
    public void updateEduFeeAndRecalculate(PaymentReqDTO.EduFeeUpdateReqDTO dto) {
        if (dto.getStudentId() == null) {
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
            paymentRepository.updateEduFeeDetailByPaymentKey(paymentKey, hanEduFee, hanMaterialFee, bookEduFee,
                    bookMaterialFee);

            paymentRepository.updateTeacherAssiginMaterialFee(dto.getStudentId(), dto.getHanMaterialFee(),
                    dto.getBookMaterialFee());
        }
    }

    public void updateMaterialFeeAndRecalculate(StudentWebReqDTO.StudentPaymentUpdateDTO dto) {
        if (dto.getStudentId() == null || dto.getPaymentKey() == null) {
            throw new IllegalArgumentException("필수 값 누락");
        }

        int hanEduFee = dto.getHanEduFee() != null ? dto.getHanEduFee() : 0;
        int hanMaterialFee = dto.getHanMaterialFee() != null ? dto.getHanMaterialFee() : 0;
        int bookEduFee = dto.getBookEduFee() != null ? dto.getBookEduFee() : 0;
        int bookMaterialFee = dto.getBookMaterialFee() != null ? dto.getBookMaterialFee() : 0;

        paymentRepository.updateEduFeeDetailByPaymentKey(dto.getPaymentKey(), hanEduFee, hanMaterialFee, bookEduFee,
                bookMaterialFee);
        recalculatePaymentStatus(dto.getPaymentKey(), null);

        paymentRepository.updateTeacherAssiginMaterialFee(dto.getStudentId(), dto.getHanMaterialFee(),
                dto.getBookMaterialFee());
    }

    /**
     * 수기 결제 입력 (현장 결제)
     * <p>
     * 🔥 개선: manual 입력 후 payment 상태 재계산
     * - manual은 교육비(EDU_FEE)만 해당
     */

    @Transactional
    public String insertPaymentManual(PaymentReqDTO.ManualPaymentReqDTO reqDTO) {

        /*
         * ================================================
         * 1. 입력값 검증 및 결제수단 판별
         * ================================================
         */
        List<PaymentReqDTO.ManualPaymentReqDTO.StudentPaymentInfo> students = reqDTO.getStudents();
        if (students == null || students.isEmpty()) {
            throw new IllegalArgumentException("학생 정보가 없습니다.");
        }

        int totalPaidAmount = reqDTO.getCardAmount() + reqDTO.getCashAmount() + reqDTO.getTransferAmount();

        boolean hasCard = reqDTO.getCardAmount() != 0;
        boolean hasCash = reqDTO.getCashAmount() != 0;
        boolean hasTransfer = reqDTO.getTransferAmount() != 0;

        String method;
        if (hasCard && !hasCash && !hasTransfer)
            method = "CARD";
        else if (!hasCard && hasCash && !hasTransfer)
            method = "CASH";
        else if (!hasCard && !hasCash && hasTransfer)
            method = "TRANSFER";
        else
            method = "MIXED";

        /*
         * ================================================
         * 2. 학생별 잔여 청구금액 합산 (totalBillAmount)
         * - 기납부금 차감한 effectiveEduFee 기준
         * ================================================
         */
        int totalBillAmount = 0;
        for (PaymentReqDTO.ManualPaymentReqDTO.StudentPaymentInfo studentInfo : students) {
            Payment payment = paymentRepository.findByStudentAndYm(studentInfo.getStudentId(), reqDTO.getYy(),
                    reqDTO.getMm());
            if (payment == null)
                throw new RuntimeException("결제 정보 없음. studentId: " + studentInfo.getStudentId());

            Integer eduFee = paymentRepository.findPaymentDetailEduFee(studentInfo.getPaymentKey(),
                    studentInfo.getStudentId());
            if (eduFee == null || eduFee == 0)
                throw new RuntimeException("청구 금액 없음. studentId: " + studentInfo.getStudentId());

            Integer alreadyPaid = paymentRepository.findAlreadyPaidAmount(studentInfo.getPaymentKey());
            int effectiveEduFee = eduFee - (alreadyPaid != null ? alreadyPaid : 0);

            if (effectiveEduFee > 0)
                totalBillAmount += effectiveEduFee;
            log.info("학생 ID: {}, 청구: {}, 기납부: {}, 잔여: {}", studentInfo.getStudentId(), eduFee, alreadyPaid,
                    effectiveEduFee);
        }
        log.info("전체 잔여 청구금액: {}, 실제 결제금액: {}", totalBillAmount, totalPaidAmount);

        /*
         * ================================================
         * 3. manual_key 생성 및 순차충당 변수 초기화
         * ================================================
         */
        String manualKey = KeyGenerator.generateManualKey(reqDTO.getCenterCode(), reqDTO.getYy(), reqDTO.getMm());

        int remainingCard = reqDTO.getCardAmount();
        int remainingCash = reqDTO.getCashAmount();
        int remainingTransfer = reqDTO.getTransferAmount();

        /*
         * ================================================
         * 4. manual_group 등록
         * ================================================
         */
        String groupStatus = (totalPaidAmount >= totalBillAmount) ? "approved" : "partial";

        PaymentReqDTO.InsertManualGroupDTO groupDTO = PaymentReqDTO.InsertManualGroupDTO.builder()
                .manualKey(manualKey)
                .totalAmount(totalPaidAmount)
                .cardName(reqDTO.getCardName())
                .method(method)
                .paidDate(reqDTO.getPaidDate())
                .yy(reqDTO.getYy())
                .mm(reqDTO.getMm())
                .studentId(students.get(0).getStudentId())
                .centerCode(reqDTO.getCenterCode())
                .userCode(reqDTO.getUserCode())
                .status(groupStatus)
                .cancelReason(null)
                .cashbillId(null)
                .build();

        paymentRepository.insertPaymentManualGroup(groupDTO);
        log.info("manual_group 생성 - manualKey: {}, 대표학생: {}, 학생수: {}", manualKey, students.get(0).getStudentId(),
                students.size());

        /*
         * ================================================
         * 5. 학생별 순차충당 및 manual insert
         * - preset_balance_after: 기존 preset 잔액 기록
         * (신규 leftover preset은 6섹션에서 UPDATE)
         * ================================================
         */
        List<StudentPaymentResult> paymentResults = new ArrayList<>();

        for (PaymentReqDTO.ManualPaymentReqDTO.StudentPaymentInfo studentInfo : students) {

            Integer eduFee = paymentRepository.findPaymentDetailEduFee(studentInfo.getPaymentKey(),
                    studentInfo.getStudentId());
            Integer alreadyPaid = paymentRepository.findAlreadyPaidAmount(studentInfo.getPaymentKey());
            int effectiveEduFee = eduFee - (alreadyPaid != null ? alreadyPaid : 0);

            if (effectiveEduFee <= 0) {
                log.info("학생 ID: {}, 완납 - 스킵", studentInfo.getStudentId());
                continue;
            }

            // 순차 충당
            int studentRemaining = effectiveEduFee;

            int studentCardAmount = Math.min(remainingCard, studentRemaining);
            remainingCard -= studentCardAmount;
            studentRemaining -= studentCardAmount;
            int studentCashAmount = Math.min(remainingCash, studentRemaining);
            remainingCash -= studentCashAmount;
            studentRemaining -= studentCashAmount;
            int studentTransferAmount = Math.min(remainingTransfer, studentRemaining);
            remainingTransfer -= studentTransferAmount;
            studentRemaining -= studentTransferAmount;

            int studentTotalPaid = studentCardAmount + studentCashAmount + studentTransferAmount;

            if (studentTotalPaid == 0) {
                log.info("학생 ID: {}, 결제금액 없음 - 스킵", studentInfo.getStudentId());
                continue;
            }

            // 상태 결정 및 payment 업데이트
            String studentStatus;
            if (studentTotalPaid >= effectiveEduFee) {
                studentStatus = "approved";
                paymentRepository.updatePaymentStatus(studentInfo.getPaymentKey(), studentStatus, reqDTO.getPaidDate(),
                        0, method);
            } else {
                studentStatus = "partial";
                paymentRepository.updatePaymentStatus(studentInfo.getPaymentKey(), studentStatus, reqDTO.getPaidDate(),
                        effectiveEduFee - studentTotalPaid, method);
            }

            PaymentReqDTO.ManualPaymentReqDTO manualDTO = PaymentReqDTO.ManualPaymentReqDTO.builder()
                    .studentId(studentInfo.getStudentId())
                    .paymentKey(studentInfo.getPaymentKey())
                    .manualKey(manualKey)
                    .cardAmount(studentCardAmount)
                    .cashAmount(studentCashAmount)
                    .transferAmount(studentTransferAmount)
                    .cardName(reqDTO.getCardName())
                    .paidDate(reqDTO.getPaidDate())
                    .userCode(reqDTO.getUserCode())
                    .centerCode(reqDTO.getCenterCode())
                    .yy(reqDTO.getYy())
                    .mm(reqDTO.getMm())
                    .status(studentStatus)
                    .prepaidAmount(0)
                    .build();

            Integer manualPaymentId = paymentRepository.insertPaymentManual(manualDTO);
            log.info("manual 생성 - studentId: {}, status: {}", studentInfo.getStudentId(), studentStatus);

            paymentResults.add(new StudentPaymentResult(
                    studentInfo.getStudentId(), studentInfo.getPaymentKey(),
                    effectiveEduFee, studentTotalPaid, 0, manualPaymentId));
        }

        /*
         * ================================================
         * 6. 선납금(leftover) 처리 및 preset_balance_after UPDATE
         * - 모든 학생 완납 후 잔여금 → 첫 번째 학생 preset
         * - preset 생성 후 해당 manual의 preset_balance_after 업데이트
         * ================================================
         */
        int leftover = remainingCard + remainingCash + remainingTransfer;

        if (leftover > 0 && !paymentResults.isEmpty()) {
            StudentPaymentResult first = paymentResults.get(0);
            first.prepaidAmount = leftover;

            PaymentReqDTO.InsertPresetDTO presetDTO = PaymentReqDTO.InsertPresetDTO.builder()
                    .studentId(first.studentId)
                    .centerCode(reqDTO.getCenterCode())
                    .userCode(reqDTO.getUserCode())
                    .totalAmount(leftover)
                    .originalAmount(first.paidAmount)
                    .usedMonths(0)
                    .presetKey(UUID.randomUUID().toString())
                    .status("active")
                    .cardName(reqDTO.getCardName())
                    .method(method)
                    .paidDate(reqDTO.getPaidDate())
                    .originalManualPaymentId(first.manualPaymentId)
                    .manualKey(manualKey)
                    .note("초과 결제 선납금 " + String.format("%,d", leftover) + "원")
                    .build();

            paymentRepository.insertPaymentPreset(presetDTO);

            // ✅ preset 생성 후 → 해당 manual의 preset_balance_after 업데이트
            paymentRepository.updatePresetBalanceAfter(manualKey, first.studentId, leftover);
            log.info("선납금 preset 생성 및 preset_balance_after 업데이트 - studentId: {}, 금액: {}원, 이건 뭐야? : {}", first.studentId,
                    leftover, first.manualPaymentId);
        }

        /*
         * ================================================
         * 7. 현금영수증 발행
         * ================================================
         */
        if (reqDTO.getCashbillInfo() != null && (method.equals("CASH") || method.equals("TRANSFER"))) {
            issueCashbillForManualPayment(manualKey, reqDTO.getCashbillInfo(), reqDTO.getCenterCode());
        }

        log.info("전체 처리 완료");
        return "서비스 종료";
    }

    private void issueCashbillForManualPayment(String manualKey,
                                               PaymentReqDTO.ManualPaymentReqDTO.CashbillInfoDTO cashbillInfo, String centerCode) {

        log.info("🔥 현금영수증 발행 시작 - manualKey: {}, 금액: {}", manualKey, cashbillInfo.getPrice());

        try {
            // 1. 결제 설정 조회
            PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(centerCode);

            if (conf == null) {
                throw new IllegalStateException("결제 설정이 없습니다.");
            }

            // 2. bill_id 생성
            LocalDate now = LocalDate.now();
            LocalDate base = LocalDate.of(2025, 1, 1);

            long diffDays = ChronoUnit.DAYS.between(base, now);
            int secondsOfDay = LocalTime.now().toSecondOfDay();
            int millis = LocalTime.now().getNano();

            String dayCode = Long.toString(diffDays, 36);
            String timeCode = Integer.toString(secondsOfDay, 36);
            String millisStr = String.format("%02d", millis % 100);

            String cashbillId = conf.getPreBillId() + "-" +
                    String.format("%3s", dayCode).replace(" ", "0") +
                    String.format("%4s", timeCode).replace(" ", "0") +
                    millisStr;

            log.info("✅ cashbill_id 생성: {}", cashbillId);

            // 3. hash 생성
            String raw = cashbillId + "," + cashbillInfo.getPrice();
            String hash = DigestUtils.sha256Hex(raw);

            // 4. 청구서 정보 구성
            Map<String, Object> bill = Map.of(
                    "bill_id", cashbillId,
                    "price", Integer.parseInt(cashbillInfo.getPrice()),
                    "supply_price", Integer.parseInt(cashbillInfo.getSupplyPrice()),
                    "tax", Integer.parseInt(cashbillInfo.getTax()),
                    "hash", hash,
                    "trader", cashbillInfo.getTrader(),
                    "issuance_number", cashbillInfo.getReceiptNumber());

            // 5. API 요청
            Map<String, Object> body = Map.of(
                    "apikey", conf.getApiKey(),
                    "member", conf.getMemberId(),
                    "merchant", conf.getMerchantId(),
                    "bill", bill);

            // 6. 결제선생 API 호출
            PaymentRespDTO.CashBillRespDTO paymintResp = callPaymintCashbill(conf.getCashbillIssueUrl(), body);

            if (!"0000".equals(paymintResp.getCode())) {
                log.error("현금영수증 발행 실패 - code: {}, message: {}", paymintResp.getCode(), paymintResp.getMsg());
                throw new RuntimeException("현금영수증 발행 실패: " + paymintResp.getMsg());
            }

            log.info("✅ 결제선생 API 성공 - 승인번호: {}", paymintResp.getAppr_cash_num());

            // 7. erp_cashbill 테이블 저장
            PaymentCashbill cashbill = PaymentCashbill.builder()
                    .studentId(cashbillInfo.getStudentId())
                    .paymentKey(cashbillInfo.getPaymentKey())
                    .billId(cashbillId)
                    .price(cashbillInfo.getPrice())
                    .supplyPrice(cashbillInfo.getSupplyPrice())
                    .tax(cashbillInfo.getTax())
                    .apprCashNum(paymintResp.getAppr_cash_num())
                    .hashValue(hash)
                    .trader(cashbillInfo.getTrader())
                    .receiptNumber(cashbillInfo.getReceiptNumber())
                    .receiptType(cashbillInfo.getReceiptType())
                    .taxType(cashbillInfo.getTaxType())
                    .issueDate(cashbillInfo.getIssueDate())
                    .status("ISSUED")
                    .build();

            paymentRepository.insertCashbill(cashbill);

            // 8. manual_group 테이블 업데이트
            paymentRepository.updateManualGroupCashbillId(manualKey, cashbillId);

            log.info("✅ 현금영수증 발행 완료 - manualKey: {}, cashbillId: {}, 승인번호: {}",
                    manualKey, cashbillId, paymintResp.getAppr_cash_num());

        } catch (Exception e) {
            log.error("❌ 현금영수증 발행 실패 - manualKey: {}, error: {}", manualKey, e.getMessage(), e);
            // 예외를 던지지 않고 로그만 남김 (수기 결제는 성공한 상태)
        }
    }

    @Data
    @AllArgsConstructor
    private static class StudentPaymentResult {
        private String studentId;
        private String paymentKey;
        private Integer eduFee; // 청구금액 (예: 120,000)
        private Integer paidAmount; // 할당받은 금액 (예: 150,000)
        private Integer prepaidAmount; // 선납금 (예: 30,000)
        private Integer manualPaymentId; // 방금 생성된 manual ID
    }

    public void processPresetPaymentIfExists(String studentId, String paymentKey, Integer billAmount,
                                             String userCode, String centerCode, String yy, String mm) {
        try {
            // 1. 활성화된 선납금 조회
            PaymentPreset preset = paymentRepository.findActivePresetByStudentId(studentId);
            boolean isFromSibling = false;
            if (preset == null) {
                preset = paymentRepository.findActivePresetBySiblingKey(studentId, centerCode);

                if (preset == null) {
                    log.info("선납금 없음 (본인+형제 모두) - studentId: {}", studentId);
                    return;
                }
                isFromSibling = true;
                log.info("형제 선납금 사용 - studentId: {}, presetOwner: {}", studentId, preset.getId());
            }

            // 2. 부분결제 여부 및 금액 계산
            boolean isPartial = preset.getTotalAmount() < billAmount;
            int presetUsedAmount = isPartial ? preset.getTotalAmount() : billAmount;
            int remainingAmount = billAmount - presetUsedAmount;

            // 차감 전 잔액 스냅샷
            int balanceBefore = preset.getTotalAmount();
            int newBalance = balanceBefore - presetUsedAmount;
            int balanceAfter = isPartial ? -remainingAmount : newBalance;
            String newStatus = newBalance <= 0 ? "completed" : "active";

            log.info("선납금 자동 결제 시작 - studentId: {}, 차감: {}, 차감 전: {}, 차감 후: {}",
                    studentId, presetUsedAmount, balanceBefore, newBalance);

            String manualKey = KeyGenerator.generateManualKey(centerCode, yy, mm);

            PaymentReqDTO.ManualPaymentReqDTO manualDTO = PaymentReqDTO.ManualPaymentReqDTO.builder()
                    .studentId(studentId)
                    .manualKey(manualKey)
                    .paymentKey(paymentKey)
                    .paidDate(LocalDate.now().toString())
                    .cardAmount(0)
                    .cashAmount(0)
                    .transferAmount(0)
                    .userCode(userCode)
                    .centerCode(centerCode)
                    .status(isPartial ? "partial" : "approved")
                    .prepaidAmount(presetUsedAmount)
                    .yy(yy)
                    .mm(mm)
                    .build();

            // 3. payment_manual 저장
            paymentRepository.insertPaymentManualFromPreset(
                    manualDTO,
                    preset.getId(),
                    presetUsedAmount,
                    preset.getMethod(),
                    preset.getCardName(),
                    isFromSibling ? null : balanceBefore,
                    isFromSibling ? null : balanceAfter);

            // 4. payment_manual_group 저장 (source=preset)
            paymentRepository.insertPaymentManualGroupFromPreset(
                    manualDTO,
                    preset.getMethod(),
                    centerCode,
                    preset.getCardName());

            // 5. preset 업데이트는 형 preset 기준 그대로
            paymentRepository.updatePresetAfterUse(
                    preset.getId(), // 형의 preset id
                    newBalance,
                    preset.getUsedMonths() + 1,
                    newStatus);

            // 6. 잔여금액 처리
            if (remainingAmount > 0) {
                log.warn("선납금 부족으로 부분결제 처리 - paymentKey: {}, 잔여: {}", paymentKey, remainingAmount);
            }

            // 7. 결제 상태 재계산
            recalculatePaymentStatus(paymentKey, userCode);

            // 8. 히스토리 기록
            Payment payment = paymentRepository.findPaymentByKey(paymentKey);
            String eventType = isPartial ? "partial_paid_from_preset" : "auto_paid_from_preset";
            String description = isPartial
                    ? String.format("선납금 부분 차감 %d원 | 차감 전 %d원 → 잔여 %d원 미납 (preset_id: %d)",
                    presetUsedAmount, balanceBefore, remainingAmount, preset.getId())
                    : String.format("선납금 자동 차감 %d원 | 차감 전 %d원 → 잔여 %d원 (preset_id: %d)",
                    presetUsedAmount, balanceBefore, newBalance, preset.getId());

            paymentRepository.insertPaymentHistory(
                    PaymentReqDTO.PaymentHistoryRecordDTO.builder()
                            .eventType(eventType)
                            .eventSource("auto")
                            .oldStatus("pending")
                            .newStatus(payment.getStatus())
                            .amount(presetUsedAmount)
                            .userCode(userCode)
                            .description(description)
                            .paymentKey(paymentKey)
                            .build());

            log.info("선납금 자동 결제 완료 - 차감: {}, 차감 전: {}, 차감 후: {}, 잔여청구: {}",
                    presetUsedAmount, balanceBefore, newBalance, remainingAmount);

        } catch (Exception e) {
            log.error("선납금 자동 처리 실패 - studentId: {}, error: {}", studentId, e.getMessage(), e);
        }
    }

    /**
     * 카드 코드 목록 조회
     */
    public List<CardCode> findCardCode() {
        return paymentRepository.findUseCardCode();
    }

    /**
     * 월별 결제 내역 조회
     */
    public List<PaymentRespDTO.MonthlyPaymentDTO> findMonthlyPayments(String userCode, String centerCode, String yy,
                                                                      String mm) {
        return paymentRepository.findMonthlyPayments(centerCode, userCode, yy, mm).stream().peek(dto -> {
            String paidDate = dto.getPaidDate();
            dto.setPaidDate((paidDate == null || paidDate.isBlank()) ? "-" : paidDate);
        }).toList();
    }

    /**
     * 청구서 재발행
     */
    public void reissueBill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.PayReissueReqDTO req)
            throws JsonProcessingException {

        log.info("🔥 재발행 시작 - billIds: {}", req.getBillIds());

        if (req.getBillIds() == null || req.getBillIds().isEmpty()) {
            throw new IllegalArgumentException("재발행할 청구서를 선택하세요.");
        }

        List<PaymentRespDTO.BillDetailDTO> existingBills = paymentRepository.findBillsByBillIds(req.getBillIds());

        log.info("🔥 조회된 청구서 개수: {}", existingBills != null ? existingBills.size() : 0);

        if (existingBills == null || existingBills.isEmpty()) {
            throw new IllegalStateException("청구서를 찾을 수 없습니다.");
        }

        long paidCount = existingBills.stream().filter(b -> "PAID".equals(b.getStatus())).count();

        if (paidCount > 0) {
            throw new IllegalStateException("이미 결제 완료된 청구서는 재발행할 수 없습니다.");
        }

        Map<String, List<PaymentRespDTO.BillDetailDTO>> groupByBillId = existingBills.stream()
                .collect(Collectors.groupingBy(PaymentRespDTO.BillDetailDTO::getBillId));

        log.info("🔥 그룹 개수: {}", groupByBillId.size());

        for (Map.Entry<String, List<PaymentRespDTO.BillDetailDTO>> entry : groupByBillId.entrySet()) {

            String billId = entry.getKey();
            List<PaymentRespDTO.BillDetailDTO> bills = entry.getValue();

            log.info("🔥 재발행 처리 - billId: {}, 학생 수: {}", billId, bills.size());

            PaymentRespDTO.BillDetailDTO representative = bills.get(0);
            String billType = representative.getBillType();

            PaymentRespDTO.PaymentConfigDTO conf = "EDU_FEE".equals(billType)
                    ? paymentRepository.findPayConfigByCenterCode(user.getCenterCode())
                    : paymentRepository.findPayConfigByCenterCode("PUS001");

            log.info("🔥 결제 설정 조회 완료: {}", conf != null);

            if (conf == null) {
                throw new IllegalStateException("결제 설정 없음");
            }

            Map<String, Object> body = Map.of("apikey", conf.getApiKey() != null ? conf.getApiKey() : "", "member",
                    conf.getMemberId() != null ? conf.getMemberId() : "", "merchant",
                    conf.getMerchantId() != null ? conf.getMerchantId() : "", "bill_id", billId);

            log.info("🔥 Paymint 호출 시작 - URL: {}", conf.getResendUrl());

            PaymentRespDTO.PaymintRespDTO paymintResp = callPaymint(conf.getResendUrl(), body);

            log.info("🔥 Paymint 응답: code={}, message={}", paymintResp.getCode(), paymintResp.getMsg());

            if (!"0000".equals(paymintResp.getCode())) {
                throw new RuntimeException("Paymint 호출 실패: " + paymintResp.getMsg());
            }
        }

        log.info("🔥 재발행 완료");
    }

    public void paymentBillStatusChange() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);
        String ymd = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        List<PaymentRespDTO.ExpiredBillDTO> result = paymentRepository.findExpriedBill(ymd);

        for (PaymentRespDTO.ExpiredBillDTO dto : result) {
            paymentRepository.updateStatusToDestroyed(dto.getBillId(), ymd);
        }

    }

    public List<PaymentRespDTO.CashbillStudentRespDTO> getCashPaymentStudents(String year, String month,
                                                                              String centerCode) {
        return paymentRepository.findCashbillStudents(year, month, centerCode);
    }

    public void issueCashbill(UserRespDTO.LoginRespDTO user, PaymentReqDTO.CashbillIssueReqDTO dto)
            throws JsonProcessingException {
        log.info("🔥 현금영수증 발행 시작 - studentId: {}, paymentKey: {}", dto.getStudentId(), dto.getPaymentKey());

        // 1. 결제 설정 조회 (교육비는 센터별, 교재비는 PUS001)
        PaymentRespDTO.PaymentConfigDTO conf = paymentRepository.findPayConfigByCenterCode(user.getCenterCode());

        if (conf == null) {
            log.error("결제 설정이 없습니다 - centerCode: {}", user.getCenterCode());
            throw new IllegalStateException("결제 설정이 없습니다.");
        }

        log.info("✅ 결제 설정 조회 완료 - apiKey: {}, member: {}, merchant: {}",
                conf.getApiKey(), conf.getMemberId(), conf.getMerchantId());
        LocalDate now = LocalDate.now();
        LocalDate base = LocalDate.of(2025, 1, 1);

        long diffDays = ChronoUnit.DAYS.between(base, now);
        int secondsOfDay = LocalTime.now().toSecondOfDay();
        int millis = LocalTime.now().getNano();

        String dayCode = Long.toString(diffDays, 36);
        String timeCode = Integer.toString(secondsOfDay, 36);
        String millisStr = String.format("%02d", millis % 100);
        // 2. bill_id 생성
        String billId = conf.getPreBillId() + "-" +
                String.format("%3s", dayCode).replace(" ", "0") +
                String.format("%4s", timeCode).replace(" ", "0") +
                millisStr;
        log.info("✅ bill_id 생성 완료: {}", billId);

        // 3. hash 생성
        String raw = billId + "," + dto.getPrice();
        String hash = DigestUtils.sha256Hex(raw);
        log.info("✅ hash 생성 완료: {}", hash);

        // 4. 청구서 정보 구성
        Map<String, Object> bill = Map.of(
                "bill_id", billId,
                "price", dto.getPrice(),
                "supply_price", dto.getSupplyPrice(),
                "tax", dto.getTax(),
                "hash", hash,
                "trader", dto.getTrader(),
                "issuance_number", dto.getReceiptNumber());

        // 5. API 요청 바디
        Map<String, Object> body = Map.of(
                "apikey", conf.getApiKey(),
                "member", conf.getMemberId(),
                "merchant", conf.getMerchantId(),
                "bill", bill);

        log.info("🔥 결제선생 API 호출 시작 - billId: {}, 발급구분: {}, 발급번호: {}, 금액: {}",
                billId, dto.getReceiptType(), dto.getReceiptNumber(), dto.getPrice());

        // 6. 외부 결제 API 호출
        PaymentRespDTO.CashBillRespDTO paymintResp = callPaymintCashbill(conf.getCashbillIssueUrl(), body);

        log.info("🔥 결제선생 응답 - apprCashNum: {}, billId: {}", paymintResp.getAppr_cash_num(), paymintResp.getBill_id());

        // 7. 응답 확인
        if (!"0000".equals(paymintResp.getCode())) {
            log.error("결제선생 API 실패 - billId: {}, code: {}, message: {}",
                    billId, paymintResp.getCode(), paymintResp.getMsg());
            throw new RuntimeException("현금영수증 발행 실패: " + paymintResp.getMsg());
        }

        log.info("✅ 결제선생 API 성공 - billId: {}, 승인번호: {}", billId, paymintResp.getAppr_cash_num());

        // 8. erp_cashbill 테이블에 INSERT
        PaymentCashbill cashbill = PaymentCashbill.builder()
                .studentId(dto.getStudentId())
                .billId(billId)
                .paymentKey(dto.getPaymentKey())
                .price(dto.getPrice())
                .supplyPrice(dto.getSupplyPrice())
                .tax(dto.getTax())
                .apprCashNum(paymintResp.getAppr_cash_num())
                .hashValue(hash)
                .trader(dto.getTrader())
                .receiptNumber(dto.getReceiptNumber())
                .receiptType(dto.getReceiptType())
                .taxType(dto.getTaxType())
                .issueDate(dto.getIssueDate())
                .status("ISSUED")
                .build();

        paymentRepository.insertCashbill(cashbill);

        log.info("✅ erp_cashbill 저장 완료 - cashbillId: {}", cashbill.getBillId());

        // 9. erp_payment_manual 테이블 UPDATE
        int updated = paymentRepository.updateCashbillInfo(
                dto.getStudentId(),
                dto.getPaymentKey(),
                billId);

        if (updated == 0) {
            log.error("erp_payment_manual 업데이트 실패 - paymentKey: {}", dto.getPaymentKey());
            throw new RuntimeException("결제 정보 업데이트 실패");
        }

        log.info("✅ erp_payment_manual 업데이트 완료 - paymentKey: {}, 업데이트 건수: {}",
                dto.getPaymentKey(), updated);

        log.info("🔥 현금영수증 발행 완료 - billId: {}, 승인번호: {}", billId, paymintResp.getAppr_cash_num());

    }

    private PaymentRespDTO.CashBillRespDTO callPaymintCashbill(String url, Map<String, Object> body)
            throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(body);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<PaymentRespDTO.CashBillRespDTO> response = restTemplate.exchange(url, HttpMethod.POST, entity,
                PaymentRespDTO.CashBillRespDTO.class);

        return response.getBody();
    }

    public List<PaymentRespDTO.ClaimDto> getFilteredClaims(PaymentReqDTO.ClaimFilterDTO filters) {

        List<PaymentRespDTO.ClaimDto> response = paymentRepository.getPaymentInfo(filters);
        return response;
    }

    public List<PaymentRespDTO.CashBillHistoryDTO> getCashbillHistory(String centerCode, String year, String month) {
        List<PaymentRespDTO.CashBillHistoryDTO> response = paymentRepository.findCashbillHistory(centerCode, year,
                month);
        return response;
    }

    @Transactional
    public PaymentRespDTO.CashbillCancelRespDTO cancelCashbills(String billId, String reason, String centerCode) {
        log.info("현금영수증 취소 시작: billIds = {}", billId);
        int successCount = 0;
        int failCount = 0;

        PaymentCashbill cashbill = paymentRepository.findCashbillByBillId(billId);

        if (cashbill == null) {
            throw new RuntimeException("취소할 현금영수증을 찾을 수 없습니다.");
        }
        if ("CANCELED".equals(cashbill.getStatus())) {
            throw new RuntimeException("이미 취소된 현금영수증입니다.");
        }

        // 2. 요청 준비
        // 센터별 설정값 조회 (첫 번째 cashbill의 센터 기준)
        PaymentRespDTO.PaymentConfigDTO config = paymentRepository.findPayConfigByCenterCode(centerCode);

        if (config == null) {
            throw new RuntimeException("결제 설정을 찾을 수 없습니다.");
        }
        try {
            // 요청 바디 생성
            Map<String, Object> requestBody = buildCancelRequestBody(config, cashbill);

            // 3. API 호출
            String cancelUrl = config.getCashbillCancelUrl();
            PaymentRespDTO.PaymintRespDTO response = callPaymint(cancelUrl, requestBody);

            log.info("취소 응답: billId={}, code={}, msg={}",
                    cashbill.getBillId(), response.getCode(), response.getMsg());

            // 4. 응답값으로 DB 수정
            if (response != null && "0000".equals(response.getCode())) {
                paymentRepository.updateCashbillStatus(cashbill.getBillId(), "CANCELED", reason);
                log.info("현금영수증 취소 성공: {}", cashbill.getBillId());

                // 5. 컨트롤러 리턴
                PaymentRespDTO.CashbillCancelRespDTO responseDTO = new PaymentRespDTO.CashbillCancelRespDTO();
                responseDTO.setSuccess(true);
                responseDTO.setMessage("현금영수증이 취소되었습니다.");
                return responseDTO;

            } else {
                log.error("현금영수증 취소 실패: {}, 응답코드: {}, 메시지: {}",
                        cashbill.getBillId(), response.getCode(), response.getMsg());
                throw new RuntimeException("취소 실패: " + response.getMsg());
            }

        } catch (Exception e) {
            log.error("현금영수증 취소 중 오류: billId={}", cashbill.getBillId(), e);
            throw new RuntimeException("현금영수증 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private Map<String, Object> buildCancelRequestBody(PaymentRespDTO.PaymentConfigDTO config,
                                                       PaymentCashbill cashbill) {
        Map<String, Object> body = new HashMap<>();

        body.put("apikey", config.getApiKey());
        body.put("member", config.getMemberId());
        body.put("merchant", config.getMerchantId());
        body.put("bill_id", cashbill.getBillId());
        body.put("price", cashbill.getPrice());
        body.put("trader", cashbill.getTrader());
        body.put("hash", cashbill.getHashValue());

        return body;
    }

    public List<PaymentRespDTO.UnpaidStudentRespDTO> findUnpaidStudentForAlimtalk(
            PaymentReqDTO.UnpaidStudentReqDTO reqDTO) {

        return paymentRepository.findUnpaidStudentForAlimtalk(reqDTO.getCenterCode(), reqDTO.getYear(),
                reqDTO.getMonth());
    }

    public void updatePayment(Integer id, PaymentReqDTO.UpdatePaymentDTO dto, String centerCode) {

        // 1. id로 매뉴얼 조회
        PaymentManual manual = paymentRepository.findManualById(id);
        if (manual == null) {
            throw new IllegalArgumentException("존재하지 않는 결제 내역입니다.");
        }

        String manualKey = manual.getManualKey();

        // 2. 매뉴얼 수정
        paymentRepository.updateManual(id, dto);

        // 3. 같은 manualKey 매뉴얼 전체 조회 (수정된 값 포함하여 재조회)
        List<PaymentManual> siblings = paymentRepository.findAllByManualKey(manualKey);

        // 4. 전체 합산 (수정된 id는 dto 값으로, 나머지는 기존 값으로)
        long totalAmount = siblings.stream()
                .mapToLong(s -> {
                    if (s.getId().equals(id)) {
                        // 수정된 매뉴얼은 dto 값으로 계산
                        return (dto.getCardAmount() != null ? dto.getCardAmount() : 0L)
                                + (dto.getCashAmount() != null ? dto.getCashAmount() : 0L)
                                + (dto.getTransferAmount() != null ? dto.getTransferAmount() : 0L);
                    } else {
                        // 나머지는 기존 값
                        return (s.getCardAmount() != null ? s.getCardAmount() : 0L)
                                + (s.getCashAmount() != null ? s.getCashAmount() : 0L)
                                + (s.getTransferAmount() != null ? s.getTransferAmount() : 0L);
                    }
                })
                .sum();

        // 5. 매뉴얼 그룹 수정 (paidDate + totalAmount)
        paymentRepository.updateManualGroup(manualKey, dto.getPaidDate(), totalAmount);
    }

    /* ── 삭제 ── */
    public void deletePayment(Integer id, String centerCode) {

        // 1. id로 매뉴얼 조회
        PaymentManual manual = paymentRepository.findManualById(id);
        if (manual == null) {
            throw new IllegalArgumentException("존재하지 않는 결제 내역입니다.");
        }

        String manualKey = manual.getManualKey();

        // 2. 같은 매뉴얼 키를 가진 매뉴얼이 몇 개인지 확인
        List<PaymentManual> siblings = paymentRepository.findAllByManualKey(manualKey);

        // 3. 매뉴얼 키로 연결된 매뉴얼 전체 삭제 (형제 매뉴얼 포함)
        paymentRepository.deleteAllByManualKey(manualKey);

        // 4. 매뉴얼 그룹 삭제
        paymentRepository.deleteManualGroup(manualKey);
    }

    public List<PaymentRespDTO.CashbillPrintDTO> findCashbillPrint(String yy, String mm,
                                                                   UserRespDTO.LoginRespDTO user) {
        String ym = yy + "-" + mm;
        String centerCode = user.getCenterCode();

        return paymentRepository.findCashbillPrint(ym, centerCode).stream()
                .peek(dto -> {
                    String num = dto.getReceiptNum() == null ? "" : dto.getReceiptNum().replaceAll("[^0-9]", "");
                    if (num.equals("0100001234")) {
                        // 자진발급 그대로
                    } else if (num.length() == 11 && num.startsWith("010")) {
                        dto.setReceiptNum(num.substring(0, 3) + "-****-" + num.substring(7));
                    } else if (num.length() == 10) {
                        dto.setReceiptNum(num.substring(0, 3) + "-**-" + num.substring(5));
                    }
                })
                .collect(Collectors.toList());
    }

    public List<PaymentAppRespDTO.StudentDTO> search(String keyword, UserRespDTO.LoginRespDTO user, String ym) {
        List<PaymentAppRespDTO.StudentDTO> students = paymentRepository.searchStudents(keyword, user.getCenterCode(),
                ym);

        for (PaymentAppRespDTO.StudentDTO s : students) {
            List<String> siblingIds = paymentRepository.getSiblingIds(s.getStudentId());
            s.setSiblings(siblingIds != null ? siblingIds : Collections.emptyList());

            s.setSubjects(buildSubjects(s));

            List<PaymentAppRespDTO.StudentDTO> siblingDetails = new ArrayList<>();
            for (String siblingId : s.getSiblings()) {
                PaymentAppRespDTO.StudentDTO sibling = paymentRepository.getStudentById(siblingId, user.getCenterCode(),
                        ym);
                if (sibling != null) {
                    sibling.setSubjects(buildSubjects(sibling));
                    siblingDetails.add(sibling);
                }
            }
            s.setSiblingDetails(siblingDetails);
        }
        return students;
    }

    private List<String> buildSubjects(PaymentAppRespDTO.StudentDTO s) {
        List<String> subjects = new ArrayList<>();
        if (s.isSubHan())
            subjects.add("한스쿨");
        if (s.isSubBook())
            subjects.add("북스쿨");
        if (s.isSubHoho())
            subjects.add("호호스쿨");
        return subjects;
    }

    public List<PaidStudentListDTO> getPaidStudentList() {
        String centerCode = "PUS002";
        String studentName = "고나연";
        String year = "2026";
        String month = "05";
        return paymentRepository.findPaidStudent(centerCode, year, month, studentName);
    }

    public List<PaymentRespDTO.FlatReceiptDTO> getReceiptPrintData(PaymentReqDTO.ReceiptPrintReqDTO req, String centerCode) {
        return paymentRepository.findReceiptPrintData(req.getRowKeys(), centerCode);
    }

}