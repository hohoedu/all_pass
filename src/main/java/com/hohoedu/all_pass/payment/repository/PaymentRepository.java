package com.hohoedu.all_pass.payment.repository;

import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.payment._dto.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentRepository {
    List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudents(
            @Param("year") String year,
            @Param("month") String month,
            @Param("userCode") String userCode);

    Integer findFeeByClassKey(@Param("classKey") String classKey, @Param("centerCode") String centerCode);

    void insertPayment(PaymentReqDTO.PayHistoryDTO payment);
}
