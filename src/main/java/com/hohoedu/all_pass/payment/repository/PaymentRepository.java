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
import org.hibernate.annotations.Parent;
import org.springframework.security.core.parameters.P;

import java.util.List;

@Mapper
public interface PaymentRepository {

    int insertPaymentHistory(PaymentReqDTO.PaymentHistoryRecordDTO dto);

    int existsBill(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("billType") String billType);

    int existsBillByStudentIds(
            @Param("studentIds") List<String> studentIds,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("billType") String billType
    );

    PaymentRespDTO.PaymentConfigDTO findPayConfigByCenterCode(@Param("centerCode") String centerCode);

    List<String> findParentPhonesByStudentIds(List<String> studentIds);

    List<PaymentRespDTO.PayTargetDTO> findTargetsByParentPhones(
            @Param("parentPhones") List<String> parentPhones,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("type") String type
    );

    List<PaymentRespDTO.PayTargetDTO> findTargetsByStudentIds(
            @Param("studentIds") List<String> studentIds,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("type") String type
    );

//    int insertPaymentBill(
//            @Param("bill") PaymentReqDTO.InsertBillDTO bill,
//            @Param("userCode") String userCode
//    );

//    int insertPaymentBillMap(
//            @Param("billId") String billId,
//            @Param("studentId") String studentId,
//            @Param("paymentKey") String paymentKey,
//            @Param("amount") Integer amount
//    );


    Payment findByStudentAndYm(@Param("studentId") String studentId, @Param("year") String year, @Param("month") String month);

    String findPaymentKeyByStudentAndYm(@Param("studentId") String studentId, @Param("year") String year, @Param("month") String month);

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

    void updatePaymentStatusOnIssue(@Param("paymentKey") String paymentKey);

    void updateBillStatus(@Param("billId") String billId, @Param("status") String status);

    void updatePaymentStatus(@Param("paymentKey") String paymentKey, @Param("newStatus") String newStatus, @Param("paidDate") String paidDate, @Param("unpaidAmount") Integer unpaidAmount);

    List<PaymentBill> findBillsByPaymentKey(@Param("paymentKey") String paymentKey);

    List<PaymentBill> findBillsByPaymentKeyAndType(
            @Param("paymentKey") String paymentKey);

    void createPaymentCallback(PaymentCallback paymentCallback);

    List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(PaymentReqDTO.PersonalDTO dto);

    Payment findPaymentByBillId(String billId);

    List<PaymentBill> findPaymentBill(String billId);

    List<PaymentRespDTO.UnpaidStudentDTO> findUnpaidStudent(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    String findPaymentKeyByStudentId(String studentId, String timeTableKey);

    void deletePaymentDetail(String paymentKey, String timeTableKey);

    int insertPaymentRefund();

    String findBillIdByPaymentKey(
            @Param("paymentKey") String paymentKey,
            @Param("type") String type);

    void updateBillStatusByBillIdAndStatus(
            @Param ("billId") String billId,
            @Param ("fromStatus") String fromStatus,
            @Param ("toStatus") String toStatus
    );

    List<PaymentRespDTO.PaymentBillDTO> findBillsByBillIdAndType(
        @Param("billId") String billId,
        @Param("billType") String billType
    );

    List<String> findPaymentKeys(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm);

    int updateEduFeeDetailByPaymentKey(
            @Param("paymentKey") String paymentKey,
            @Param("hanEduFee") Integer hanEduFee,
            @Param("hanMaterialFee") Integer hanMaterialFee,
            @Param("bookEduFee") Integer bookEduFee,
            @Param("bookMaterialFee") Integer bookMaterialFee);

    List<String> findPaymentDetailsByPaymentKey(@Param("paymentKey") String paymentKey);

    int updateAmountAndUnpaidAmountByPaymentKey(
            @Param("paymentKey") String paymentKey,
            @Param("amount") int amount,
            @Param("unpaidAmount") int unpaidAmount);

    int updateTeacherAssiginMaterialFee(
            @Param("studentId") String studentId,
            @Param("hanMaterialFee") Integer hanMaterialFee,
            @Param("bookMaterialFee") Integer bookMaterialFee);

    // ======================================== APP ======================================== //
    // i-with 납부내역 조회
    List<PaymentAppRespDTO.PaymentDetailRespDTO> findPaymentDetailsByStudentId(@Param("studentId") String studentId, @Param("count") String count);


}
