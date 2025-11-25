package com.hohoedu.all_pass.payment.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.PaymentBill;
import com.hohoedu.all_pass.payment.model.PaymentCallback;
import com.hohoedu.all_pass.payment.model.PaymentDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentRepository {

    int insertPaymentHistory(PaymentReqDTO.PaymentHistoryRecordDTO dto);

    String findByStudentAndYm(@Param("studentId") String studentId, @Param("year") String year, @Param("month") String month);

    // 수강료 청구 화면 데이터 필터링
    List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudents(@Param("year") String year, @Param("month") String month, @Param("userCode") String userCode, @Param("centerCode") String centerCode);

    // 수업별 센터별 수업료 조회
    Integer findFeeByClassKey(@Param("classKey") String classKey, @Param("centerCode") String centerCode);

    // 학생 등록 후 결제 생성
    int createPayment(Payment payment);

    // 학생 등록 후 결제 상세내용 생성
    int createPaymentDetail(PaymentDetail paymentDetail);

    String findLatestPaymentKeyByStudent(@Param("studentId") String studentId, @Param("yy") String yy, @Param("mm") String mm);

    void updateAmount(@Param("paymentKey") String paymentKey);

    Payment findPaymentByKey(String paymentKey);

    void createPaymentBill(PaymentBill paymentBill);


    void updateBillStatus(@Param("billId") String billId, @Param("status") String status, @Param("paidDate") String paidDate);

    Integer sumBillAmountsByPaymentKey(String paymentKey);

    void updatePaymentAmount(String paymentKey, Integer newAmount);

    void updatePaymentStatus(String paymentKey, String newStatus, String paidDate);

    List<PaymentBill> findBillsByPaymentKey(String paymentKey);

    void createPaymentCallback(PaymentCallback paymentCallback);

    List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(@Param("studentId") String studentId, @Param("centerCode") String centerCode);

    Payment findPaymentByBillId(String billId);

    PaymentBill findPaymentBill(String billId);

    // ======================================== APP ======================================== //
    // i-with 납부내역 조회
    List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsByStudentId(@Param("studentId") String studentId, @Param("count") String count);


}
