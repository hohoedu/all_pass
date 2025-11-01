package com.hohoedu.all_pass.payment.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
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

    List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(
            @Param("centerCode") String centerCode);

    int updateClassFeeMap(
            @Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList
    );

    int insertPaymentCallback(PaymentReqDTO.PayCallbackDTO payCallbackDTO);

    String findPaymentByBillId(@Param("billId") String billId);

    int updatePayment(@Param("billId") String billId,@Param("approvedAt") String approvedAt);
}
