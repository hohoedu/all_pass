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

import java.util.List;


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

    // 결제 생성
    public void createPayment(ClassReqDTO.AddStudentDTO studentDTO, ClassRespDTO.ClassInfoDTO classInfoDTO, String userCode, String centerCode) {

        String paymentKey;

        String existingPaymentKey = paymentRepository.findByStudentAndYm(studentDTO.getStudentId(), studentDTO.getYy(), studentDTO.getMm());

        if (existingPaymentKey != null) {

            paymentKey = existingPaymentKey;

            if (classInfoDTO != null) {
                insertPaymentDetails(paymentKey, classInfoDTO);
                paymentRepository.updateAmount(paymentKey,
                        classInfoDTO.getClassFee() + 20000);
            }
            return;
        }

        paymentKey = PaymentKeyGenerator.generate(centerCode);

        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .amount(classInfoDTO != null ? classInfoDTO.getClassFee() : null)
                .status("pending")
                .yy(studentDTO.getYy())
                .mm(studentDTO.getMm())
                .student(Student.builder().studentId(studentDTO.getStudentId()).build())
                .center(Center.builder().centerCode(centerCode).build())
                .build();

        paymentRepository.createPayment(payment);

        String eventType = "payment_created";
        String eventSource = "system";
        String oldStatus = null;
        String newStatus = payment.getStatus();
        Integer amount = payment.getAmount();
        String description = "결제 생성";

        logHistory(eventType, eventSource, oldStatus, newStatus, amount, description, paymentKey, userCode);

        if (classInfoDTO != null) {
            insertPaymentDetails(paymentKey, classInfoDTO);
            paymentRepository.updateAmount(paymentKey,
                    classInfoDTO.getClassFee() + 20000);
        }
    }

    // 결제 상세 내역 저장
    public void insertPaymentDetails(String paymentKey, ClassRespDTO.ClassInfoDTO classInfoDTO) {
        PaymentDetail eduDetail = PaymentDetail.builder()
                .payment(Payment.builder().paymentKey(paymentKey).build())
                .amount(classInfoDTO.getClassFee())
                .classType(classInfoDTO.getClassType())
                .itemType("edu")
                .user(User.builder().userCode(classInfoDTO.getUserCode()).build())
                .build();
        paymentRepository.createPaymentDetail(eduDetail);

        PaymentDetail bookDetail = PaymentDetail.builder()
                .payment(Payment.builder().paymentKey(paymentKey).build())
                .amount(15000)
                .classType(classInfoDTO.getClassType())
                .itemType("book")
                .user(User.builder().userCode(classInfoDTO.getUserCode()).build())
                .build();
        paymentRepository.createPaymentDetail(bookDetail);
    }

    // 수업료 청구 화면 필터링
    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode) {

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode);

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


        paymentRepository.updatePayment(dto.getPaymentKey(), dto.getYy(), dto.getMm(), status, today);

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
    public List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(String studentId) {

        return paymentRepository.findPaymentByStudentId(studentId);
    }

    public void insertPaymentCallback(PaymentReqDTO.PayCallbackDTO dto) {

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

        try {

            paymentRepository.createPaymentCallback(paymentCallback);

            Payment payment = paymentRepository.findPaymentByBillId(dto.getBill_id());
            PaymentBill paymentBill = paymentRepository.findPaymentBill(dto.getBill_id());

            if (payment == null || paymentBill == null) {
                log.error("❌ Callback 처리 실패: payment 또는 bill 정보를 찾을 수 없음. bill_id=" + dto.getBill_id());
                return;
            }

            String eventType = "callback_received";
            String eventSource = "callback";
            String oldStatus = payment.getStatus();
            String newStatus = "approved";
            Integer amount = paymentBill.getAmount();
            String description = "Paymint 결제승인 콜백 처리";
            String paymentKey = payment.getPaymentKey();

            logHistory(eventType, eventSource, oldStatus, newStatus, amount, description, paymentKey, null);

            log.info("✅ 결제 콜백 처리 완료 (bill_id: {})", dto.getBill_id());

        } catch (Exception e) {
            log.error("❌ 결제 콜백 처리 중 오류: {}", e.getMessage(), e);
        }
    }
}
