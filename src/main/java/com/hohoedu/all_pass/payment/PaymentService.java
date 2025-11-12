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
import com.hohoedu.all_pass.payment.model.PaymentDetail;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.bp.LocalDate;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;

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
    public void insertPaymentBill(PaymentReqDTO.InsertBillDTO dto) {
        String today = dateConfig.currentYearMonth().get("today");
        log.info(today);
        PaymentBill paymentBill = PaymentBill.builder()
                .billId(dto.getBillId())
                .payment(Payment.builder().paymentKey(dto.getPaymentKey()).build())
                .amount(dto.getAmount())
                .status("issued")
                .expireDate(dto.getExpireDate())
                .issuedDate(today)
                .billType(dto.getBillType())
                .student(Student.builder().studentId(dto.getStudentId()).build())
                .center(Center.builder().centerCode(dto.getCenterCode()).build())
                .yy(dto.getYy())
                .mm(dto.getMm())
                .build();

        paymentRepository.insertPaymentBill(paymentBill);
    }

    // 모달 데이터 조회
    public List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(String studentId) {

        return paymentRepository.findPaymentByStudentId(studentId);
    }
}
