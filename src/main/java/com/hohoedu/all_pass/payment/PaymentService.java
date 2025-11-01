package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass.payment._dto.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent(String year, String month, String userCode) {

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents(year, month, userCode);

        return students;
    }

    public Integer findFeeByClassKey(String classKey, String centerCode) {
        Integer fee = paymentRepository.findFeeByClassKey(classKey, centerCode);
        return fee;

    }

    public void insertPayment(PaymentReqDTO.PayHistoryDTO payment) {

        paymentRepository.insertPayment(payment);
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

        return null;
    }

}
