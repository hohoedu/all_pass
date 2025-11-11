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
import com.hohoedu.all_pass.payment.model.PaymentDetail;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.threeten.bp.LocalDate;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;

    // 결제 생성
    public void createPayment(ClassReqDTO.AddStudentDTO studentDTO, ClassRespDTO.ClassInfoDTO classInfoDTO, String userCode, String centerCode) {

        String paymentKey = PaymentKeyGenerator.generate(centerCode);
        Payment payment = Payment.builder()
                .paymentKey(paymentKey)
                .amount(classInfoDTO != null ? classInfoDTO.getClassFee() : null)
                .status("issued")
                .yy(studentDTO.getYy())
                .mm(studentDTO.getMm())
                .student(Student.builder().studentId(studentDTO.getStudentId()).build())
                .user(User.builder().userCode(userCode).build())
                .center(Center.builder().centerCode(centerCode).build())
                .build();

        paymentRepository.createPayment(payment);

        if (classInfoDTO != null) {
           insertPaymentDetails(paymentKey, classInfoDTO);
        }
    }

    public void insertPaymentDetails(String paymentKey, ClassRespDTO.ClassInfoDTO classInfoDTO) {
        PaymentDetail eduDetail = PaymentDetail.builder()
                .payment(Payment.builder().paymentKey(paymentKey).build())
                .amount(classInfoDTO.getClassFee())
                .classType(classInfoDTO.getClassType())
                .itemType("교육비")
                .build();
        paymentRepository.createPaymentDetail(eduDetail);

        // 교재비
        PaymentDetail bookDetail = PaymentDetail.builder()
                .payment(Payment.builder().paymentKey(paymentKey).build())
                .amount(15000)
                .classType(classInfoDTO.getClassType())
                .itemType("교재비")
                .build();
        paymentRepository.createPaymentDetail(bookDetail);
    }

    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode) {

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode);

        return students;
    }

    public Integer findFeeByClassKey(String classKey, String centerCode) {
        Integer fee = paymentRepository.findFeeByClassKey(classKey, centerCode);
        return fee;

    }

    public List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(String centerCode) {
        List<PaymentRespDTO.ClassFeeMapDTO> classFeeMaps = paymentRepository.findClassFeeMapByCenterCode(centerCode);
        return classFeeMaps;
    }


    public List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsBytudentId(PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
        return paymentRepository.findPaymentDetailsByStudentId(reqDTO.getStudentId(), reqDTO.getCount());
    }
}
