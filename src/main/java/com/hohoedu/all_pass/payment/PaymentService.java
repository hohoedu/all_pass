package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppReqDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.PaymentHistory;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DateConfig dateConfig;

    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode) {

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode);

        return students;
    }

    public Integer findFeeByClassKey(String classKey, String centerCode) {
        Integer fee = paymentRepository.findFeeByClassKey(classKey, centerCode);
        return fee;

    }

    public void insertPayment(PaymentReqDTO.PaymentDTO payment, UserRespDTO.LoginRespDTO user) {
        if ("edu".equals(payment.getStatusType())) {
            payment.setEduStatus("issued");
        }
        if ("material".equals(payment.getStatusType())) {
            payment.setMaterialStatus("issued");
        }

        payment.setUserCode(user.getUserCode());
        payment.setCenterCode(user.getCenterCode());
        String date = dateConfig.currentYearMonth().get("today");
        String description = date + " " + payment.getStudentName() + " 학생 청구서 발행";

        PaymentReqDTO.PaymentHistoryDTO paymentHistory =
                PaymentReqDTO.PaymentHistoryDTO
                        .builder()
                        .billId(payment.getBillId())
                        .eventSource("issued")
                        .eventType("system")
                        .amount(payment.getAmount())
                        .paymentKey(null)
                        .description(description)
                        .studentId(payment.getStudentId())
                        .userCode(user.getUserCode())
                        .centerCode(user.getCenterCode())
                        .build();
        System.out.println("payment inserted = " + paymentHistory);
        paymentRepository.insertPayment(payment);
        paymentRepository.insertPaymentHistory(paymentHistory);
    }

    public List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(String centerCode) {
        List<PaymentRespDTO.ClassFeeMapDTO> classFeeMaps = paymentRepository.findClassFeeMapByCenterCode(centerCode);
        return classFeeMaps;
    }

    // 결제 후 콜백 저장
    public Integer insertPaymentCallback(PaymentReqDTO.PayCallbackDTO payCallbackDTO) {

        paymentRepository.insertPaymentCallback(payCallbackDTO);
        String approvedAt = payCallbackDTO.getAppr_dt()
                .replaceFirst("(\\d{4})(\\d{2})(\\d{2})(\\d{2})(\\d{2}).*", "$1-$2-$3 $4:$5");
        paymentRepository.updatePayment(payCallbackDTO.getBill_id(), approvedAt);

//        // 로그 저장
//        paymentRepository.insertPaymentHistory();

        return null;
    }

    public List<PaymentRespDTO.PaymentDetailDTO> findPaymentByStudentId(String studentId) {
        return paymentRepository.findPaymentByStudentId(studentId);
    }

    public List<PaymentRespDTO.PaymentBillIdDTO> findPaymentBillIdByStudentId(PaymentReqDTO.BillIdSerchDTO billIdSerchDTO) {

        List<String> studentIds = billIdSerchDTO.getStudents()
                .stream()
                .map(PaymentReqDTO.BillIdSerchDTO.Student::getStudentId)
                .toList();

        String year = billIdSerchDTO.getYy();
        String month = billIdSerchDTO.getMm();

        List<PaymentRespDTO.PaymentBillIdDTO> response = paymentRepository.findBillIdByStudentId(studentIds, year, month);
        System.out.println(response);

        return response;
    }

    public List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsBytudentId(PaymentAppReqDTO.PaymentDetailsReqDTO reqDTO) {
        return paymentRepository.findPaymentDetailsByStudentId(reqDTO.getStudentId(), reqDTO.getCount());
    }
}
