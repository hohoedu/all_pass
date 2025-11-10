package com.hohoedu.all_pass.payment.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
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

    List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(@Param("centerCode") String centerCode);

    // 학원별 수강료 수정
    int updateClassFeeMap(@Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList);


    // ======================================== APP ======================================== //
    // i-with 납부내역 조회
    List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsByStudentId(
            @Param("studentId") String studentId,
            @Param("count") String count);

}
