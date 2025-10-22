package com.hohoedu.all_pass.payment.repository;

import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PaymentRepository {
    List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudents();
}
