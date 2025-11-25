package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.PaymentKeyGenerator;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;
    private final Map<String, String> currentYearMonth;

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

    // 결제 생성
    public String createPayment(String studentId, String yy, String mm, String centerCode, String userCode) {

        String paymentKey = paymentRepository.findByStudentAndYm(studentId, yy, mm);

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
                .build();
        paymentRepository.createPaymentDetail(eduDetail);

        // 2) 교재비 (DTO에서 받아야 함)
        PaymentDetail bookDetail = PaymentDetail.builder()
                .payment(payment)
                .user(creator)
                .itemType("BOOK_FEE")
                .classType(classInfoDTO.getClassType())
                .amount(15000)
                .note("교재비")
                .build();
        paymentRepository.createPaymentDetail(bookDetail);

        // 3) amount 업데이트
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

    // 결제선생 청구서 저장
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


//        paymentRepository.updatePaymentByIssued(dto.getPaymentKey(), dto.getYy(), dto.getMm(), status, today);

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
    public List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(String studentId, String centerCode) {

        return paymentRepository.findPaymentByStudentId(studentId, centerCode);
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

        paymentRepository.updateBillStatus(dto.getBill_id(), "approved", paidDate);

        Integer newAmount = paymentRepository.sumBillAmountsByPaymentKey(payment.getPaymentKey());
        paymentRepository.updatePaymentAmount(payment.getPaymentKey(), newAmount);

        String newStatus = calculatePaymentStatus(payment.getPaymentKey());
        paymentRepository.updatePaymentStatus(payment.getPaymentKey(), newStatus, paidDate);

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
}