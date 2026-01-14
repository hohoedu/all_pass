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

    /**
     * payment 상태 재계산 (교육비 기준)
     *
     * 🎯 핵심 로직:
     * - payment 상태는 교육비(EDU_FEE)만 고려
     * - 교육비 = bill(EDU_FEE) + manual 모두 포함
     * - 교재비(BOOK_FEE)는 독립적 (bill 상태로만 관리)
     *
     * 상태 결정:
     * - pending: 청구된 금액이 없음
     * - issued: 청구는 되었으나 결제 금액이 없음
     * - partial: 부분 결제
     * - approved: 전액 결제
     *
     * @param paymentKey - payment 키
     * @param userCode - 작업자 코드
     */
    @Transactional
    public void recalculatePaymentStatus(String paymentKey, String userCode) {

        Payment payment = paymentRepository.findPaymentByKey(paymentKey);
        if (payment == null) {
            log.error("❌ payment 없음 paymentKey={}", paymentKey);
            return;
        }

        String oldStatus = payment.getStatus();

        // 1️⃣ 교육비(EDU_FEE) detail 총 금액 조회
        PaymentRespDTO.PaymentDetailDTO eduDetail = paymentRepository.findEduPaymentDetailByPaymentKey(paymentKey);
        int totalEduAmount = eduDetail != null && eduDetail.getAmount() != null ? eduDetail.getAmount() : 0;

        // 2️⃣ 교육비(EDU_FEE) bill 조회
        List<PaymentBill> allBills = paymentRepository.findBillsByPaymentKey(paymentKey);

        // 교육비 bill만 필터링 (destroyed, canceled 제외)
        List<PaymentBill> eduBills = allBills.stream()
                .filter(b -> "EDU_FEE".equals(b.getBillType()))
                .filter(b -> !b.getStatus().equals("destroyed") && !b.getStatus().equals("canceled"))
                .toList();

        // 교육비 청구 금액
        int eduBilledAmount = eduBills.stream()
                .mapToInt(PaymentBill::getAmount)
                .sum();

        // 교육비 bill 결제 완료 금액
        int eduBillPaidAmount = eduBills.stream()
                .filter(b -> "approved".equals(b.getStatus()))
                .mapToInt(PaymentBill::getAmount)
                .sum();

        // 3️⃣ 교육비 manual 결제 금액 조회 ✅ 수정됨
        int eduManualPaidAmount = 0;
        try {
            // ✅ Repository에서 직접 합계 조회 (unpaid 역산 방식 제거)
            eduManualPaidAmount = paymentRepository.sumManualAmountByPaymentKey(paymentKey);
        } catch (Exception e) {
            log.warn("manual 금액 조회 실패: {}", e.getMessage());
            eduManualPaidAmount = 0;
        }

        // 4️⃣ 교육비 총 결제 완료 금액 (bill + manual)
        int totalEduPaidAmount = eduBillPaidAmount + eduManualPaidAmount;

        // 5️⃣ 교육비 미납 금액
        int unpaidAmount = totalEduAmount - totalEduPaidAmount;
        if (unpaidAmount < 0) unpaidAmount = 0;

        // 6️⃣ payment 상태 결정 (교육비 기준)
        String newStatus = determinePaymentStatus(eduBilledAmount, totalEduPaidAmount, totalEduAmount);

        // 7️⃣ payment 업데이트 (금액)
        paymentRepository.updateAmountAndUnpaidAmountByPaymentKey(
                paymentKey,
                totalEduAmount,
                unpaidAmount
        );

        // 8️⃣ payment 상태가 변경된 경우에만 업데이트
        if (!oldStatus.equals(newStatus)) {
            String paidDate = payment.getPaidDate();
            if (totalEduPaidAmount > 0 && (paidDate == null || paidDate.isBlank())) {
                paidDate = dateConfig.currentYearMonth().get("today");
            }

            String method = payment.getMethod();
            if (eduManualPaidAmount > 0 && eduBillPaidAmount > 0) {
                method = "mixed";  // bill + manual 둘 다
            } else if (eduManualPaidAmount > 0) {
                method = "manual";  // manual만
            } else if (eduBillPaidAmount > 0) {
                method = "paymint";  // bill만
            } else if (method == null || method.isBlank()) {
                method = "paymint";  // 기본값
            }

            paymentRepository.updatePaymentStatus(
                    paymentKey,
                    newStatus,
                    paidDate,
                    unpaidAmount,
                    method
            );

            // 9️⃣ 로그 기록
            logHistory(
                    "status_recalculated",
                    "system",
                    oldStatus,
                    newStatus,
                    totalEduPaidAmount,
                    String.format("교육비 상태 재계산 (total:%d, bill:%d, manual:%d, paid:%d, unpaid:%d)",
                            totalEduAmount, eduBillPaidAmount, eduManualPaidAmount, totalEduPaidAmount, unpaidAmount),
                    paymentKey,
                    userCode
            );
        }

        log.info("✅ Payment 상태 업데이트 완료 paymentKey={}, status: {} → {}, eduTotal={}, billPaid={}, manualPaid={}, totalPaid={}, unpaid={}",
                paymentKey, oldStatus, newStatus, totalEduAmount, eduBillPaidAmount, eduManualPaidAmount, totalEduPaidAmount, unpaidAmount);
    }

    /**
     * payment 상태 결정 (교육비 기준)
     *
     * @param billedAmount - 청구된 금액
     * @param paidAmount   - 결제 완료 금액 (bill + manual)
     * @param totalAmount  - 총 금액
     * @return 상태 (pending/issued/partial/approved)
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

    /**
     * 결제 이력 로그 저장
     * <p>
     * 용도: 모든 결제 관련 이벤트를 erp_payment_history 테이블에 기록
     *
     * @param eventType   - 이벤트 타입 (payment_created, bill_issued, callback_received 등)
     * @param eventSource - 이벤트 발생원 (system, manual, callback)
     * @param oldStatus   - 변경 전 상태
     * @param newStatus   - 변경 후 상태
     * @param amount      - 금액
     * @param description - 설명
     * @param paymentKey  - payment 키
     * @param userCode    - 작업자 코드
     */
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

    /**
     * 만료된 청구서 자동 파기 (스케줄러)
     * <p>
     * 기능: expire_date가 지났지만 상태가 'issued'인 청구서를 'destroyed'로 변경
     * <p>
     * 실행 시점: 로그인 시 1회 실행
     * <p>
     * 처리 내용:
     * - erp_payment_bill 테이블에서 만료된 청구서 조회
     * - 상태를 'destroyed'로 업데이트
     * <p>
     * 🔥 개선: 파기 후 교육비 bill인 경우 payment 상태 재계산
     */
    @Transactional
    public void destroyExpiredBills() {
        int count = paymentRepository.updateExpiredBillsToDestroyed();
        log.info("만료된 청구서 {} 건 파기 처리", count);

        // TODO: 파기된 교육비 bill의 payment 재계산
        // 현재는 bill이 이미 destroyed로 변경된 후라 paymentKey 조회 필요
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
     * @param index  - 순번 (가구별로 증가)
     * @param type   - "edu" 또는 "material"
     * @return 생성된 청구서 ID
     */
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
        recalculatePaymentStatus(paymentKey, userCode);
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

            if (req.getCustomPrice() != null) {
                for (PaymentRespDTO.PayTargetDTO target : targets) {
                    target.setAmount(req.getCustomPrice());
                }
            }

            if (targets.isEmpty()) {
                throw new IllegalStateException("청구 대상 없음");
            }
        }

        List<PaymentRespDTO.PayTargetDTO> adjustedTargets = new ArrayList<>();
        if (req.getCustomPrice() != null) {
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

        // 🔥 개선: 교육비 bill인 경우에만 payment 상태 재계산
        if ("EDU_FEE".equals(dto.getBillType())) {
            recalculatePaymentStatus(dto.getPaymentKey(), userCode);
        }
        // 교재비(BOOK_FEE)는 독립적이므로 bill.status로만 관리

        logHistory(
                "bill_issued",
                "system",
                oldPayment.getStatus(),
                status,
                dto.getAmount(),
                dto.getYy() + "년 " + dto.getMm() + "월 청구서 발행 (" + dto.getBillType() + ")",
                dto.getPaymentKey(),
                userCode
        );
    }

    /**
     * 수업료 청구 화면 데이터 조회
     */
    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode, String centerCode) {
        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode, centerCode);
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
    public List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsBytudentId(PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
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
        String paidDate = rawDate.substring(0, 4) + "-" +
                rawDate.substring(4, 6) + "-" +
                rawDate.substring(6, 8) + " " +
                rawDate.substring(8, 10) + ":" +
                rawDate.substring(10, 12);

        // bill 상태를  'approved'로 변경
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

                logHistory(
                        "callback_received",
                        "callback",
                        oldStatus,
                        updatedPayment.getStatus(),
                        bill.getAmount(),
                        "Paymint 교육비 결제 승인 콜백 처리",
                        paymentKey,
                        null
                );
            } else {
                // 교재비 bill → payment 상태에 영향 없음 (bill.status로만 관리)
                logHistory(
                        "callback_received",
                        "callback",
                        null,
                        "approved",
                        bill.getAmount(),
                        "Paymint 교재비 결제 승인 콜백 처리 (payment 영향 없음)",
                        paymentKey,
                        null
                );
            }

            log.info(
                    "✅ 콜백 처리 완료 paymentKey={}, billId={}, amount={}, type={}",
                    paymentKey,
                    dto.getBill_id(),
                    bill.getAmount(),
                    hasEduBill ? "교육비" : "교재비"
            );
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

        // 🔥 개선: 교육비 bill 파기 시에만 payment 상태 재계산
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
    public List<PaymentRespDTO.UnpaidStudentDTO> findUnpaidStudent(String centerCode, String userCode, String yy, String mm) {
        List<PaymentRespDTO.UnpaidStudentDTO> studentDTO = paymentRepository.findUnpaidStudent(centerCode, userCode, yy, mm);
        return studentDTO;
    }

    /**
     * Detail 삭제
     *
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

        // 🔥 개선: 교육비 취소 시에만 payment 상태 재계산
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

    /**
     * 수기 결제 입력 (현장 결제)
     * <p>
     * 🔥 개선: manual 입력 후 payment 상태 재계산
     * - manual은 교육비(EDU_FEE)만 해당
     */
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

        reqDTO.setStatus("partial");

        // manual 데이터 저장
        paymentRepository.insertPaymentManual(reqDTO);

        // 🔥 개선: payment 상태 재계산 (교육비 bill + manual 고려)
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

    /**
     * 카드 코드 목록 조회
     */
    public List<CardCode> findCardCode() {
        return paymentRepository.findUseCardCode();
    }

    /**
     * 월별 결제 내역 조회
     */
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

    /**
     * 청구서 재발행
     */
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