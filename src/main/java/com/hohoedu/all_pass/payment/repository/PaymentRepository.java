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

    void insertPayment(PaymentReqDTO.PaymentDTO payment);

    void insertPaymentHistory(PaymentReqDTO.PaymentHistoryDTO payment);

    List<PaymentRespDTO.ClassFeeMapDTO> findClassFeeMapByCenterCode(@Param("centerCode") String centerCode);

    // 학원별 수강료 수정
    int updateClassFeeMap(@Param("list") List<ManageReqDTO.InsertClassFeeDTO.ClassFeeMapDTO> feeMapList);

    // 결제 완료 후 콜백 저장
    int insertPaymentCallback(PaymentReqDTO.PayCallbackDTO payCallbackDTO);

    // 결제 완료 후 상태 업데이트
    int updatePayment(@Param("billId") String billId, @Param("approvedAt") String approvedAt);

    // 학생별 상세 납부내역 조회
    List<PaymentRespDTO.PaymentDetailDTO> findPaymentByStudentId(@Param("studentId") String studentId);

    // 학생, 연월 별 bill_id 조회
    List<PaymentRespDTO.PaymentBillIdDTO> findBillIdByStudentId(
            @Param("studentId") List<String> studentId,
            @Param("year") String year,
            @Param("month") String month);

    // ======================================== APP ======================================== //
    // i-with 납부내역 조회
    List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsByStudentId(
            @Param("studentId") String studentId,
            @Param("count") String count);
}
