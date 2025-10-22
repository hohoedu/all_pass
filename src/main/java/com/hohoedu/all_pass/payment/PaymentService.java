package com.hohoedu.all_pass.payment;

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

    public List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudent() {

        List<PaymentRespDTO.AssignStudentsDTO> students = paymentRepository.findByAssignStudents();

        return students;
    }
}
