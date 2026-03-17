package com.hohoedu.all_pass.payment.repository;

import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.payment._dto.app.PaymentAppRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentReqDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.*;
import com.popbill.api.cashbill.Cashbill;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.hibernate.annotations.Parent;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PaymentRepository {

    List<PaymentRespDTO.MainPaymentSummaryDTO> findPaymentSummary(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month,
            @Param("startYear") String startYear,
            @Param("startMonth") int startMonth);

    List<PaymentRespDTO.MainPaymentSummaryDTO> findPaymentSummaryByPeriod(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month);

    int updateExpiredBillsToDestroyed();

    int sumManualAmountByPaymentKey(@Param("paymentKey") String paymentKey);

    int insertPaymentHistory(PaymentReqDTO.PaymentHistoryRecordDTO dto);

    int existsBill(
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("billType") String billType);

    int sumBilledAmountByPaymentKey(
            @Param("paymentKey") String paymentKey,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("billType") String billType,
            @Param("validStatuses") List<String> validStatuses
    );

    PaymentRespDTO.BillRespDTO existsBillByPaymentKey(
            @Param("studentId") String studentId,
            @Param("paymentKey") String paymentKey,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    int existManualByPaymentKey(
            @Param("studentId") String studentId,
            @Param("paymentKey") String paymentKey,
            @Param("yy") String yy,
            @Param("mm") String mm
    );

    int insertPaymentManual(PaymentReqDTO.ManualPaymentReqDTO dto);

    int insertPaymentManualGroup(PaymentReqDTO.InsertManualGroupDTO dto);

    void insertPaymentPreset(PaymentReqDTO.InsertPresetDTO dto);

    // 활성화된 선납금 조회
    PaymentPreset findActivePresetByStudentId(String studentId);

    // preset에서 자동 결제 시 manual_payment 생성
    Integer insertPaymentManualFromPreset(
            @Param("dto") PaymentReqDTO.ManualPaymentReqDTO dto,
            @Param("presetId") Integer presetId,
            @Param("amount") Integer amount,
            @Param("method") String method,
            @Param("cardName") String cardName

    );

    // preset 사용 후 업데이트
    void updatePresetAfterUse(
            @Param("presetId") Integer presetId,
            @Param("newTotalAmount") Integer newTotalAmount,
            @Param("newUsedMonths") Integer newUsedMonths,
            @Param("newStatus") String newStatus
    );

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

    Payment findByStudentAndYm(@Param("studentId") String studentId, @Param("year") String year, @Param("month") String month);

    Integer findPaymentDetailEduFee(@Param("paymentKey") String paymentKey, @Param("studentId") String studentId);

    String findPaymentKeyByStudentAndYm(@Param("studentId") String studentId, @Param("year") String year, @Param("month") String month);

    // 수강료 청구 화면 데이터 필터링
    List<PaymentRespDTO.AssignStudentsDTO> findByAssignStudents(
            @Param("year") String year,
            @Param("month") String month,
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode,
            @Param("itemType") String itemType);

    // 수업별 센터별 수업료 조회
    Integer findFeeByClassKey(@Param("classKey") String classKey, @Param("centerCode") String centerCode);

    // 학생 등록 후 결제 생성
    int createPayment(Payment payment);

    // 학생 등록 후 결제 상세내용 생성
    int createPaymentDetail(PaymentDetail paymentDetail);

    String findLatestPaymentKeyByStudentId(@Param("studentId") String studentId);

    void updateAmount(@Param("paymentKey") String paymentKey);

    int updatePaymentDetailBookFee(
            @Param("studentId") String studentId,
            @Param("classType") String classType,
            @Param("materialFee") Integer materialFee,
            @Param("paymentKey") String paymentKey);

    Payment findPaymentByKey(String paymentKey);

    boolean existsApprovedBill(
            @Param("paymentKey") String paymentKey,
            @Param("billType") String billType);

    int countExistingBillsByStudent(
            @Param("paymentKey") String paymentKey,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("billType") String billType,
            @Param("validStatuses") List<String> validStatuses
    );

    void createPaymentBill(PaymentBill paymentBill);

    void updatePaymentStatusOnIssue(@Param("paymentKey") String paymentKey);

    void updateBillStatus(
            @Param("billId") String billId,
            @Param("status") String status);

    void updatePaymentStatus(
            @Param("paymentKey") String paymentKey,
            @Param("newStatus") String newStatus,
            @Param("paidDate") String paidDate,
            @Param("unpaidAmount") Integer unpaidAmount,
            @Param("method") String method);

    void updatePaymentCancel(
            @Param("paymentKey") String paymentKey,
            @Param("newStatus") String newStatus,
            @Param("paidDate") String paidDate,
            @Param("unpaidAmount") Integer unpaidAmount
    );

    List<PaymentBill> findBillsByPaymentKey(@Param("paymentKey") String paymentKey);

    List<PaymentBill> findBillsByPaymentKeyAndType(
            @Param("paymentKey") String paymentKey);

    void createPaymentCallback(PaymentCallback paymentCallback);

    List<PaymentRespDTO.PaymentModalDTO> findPaymentByStudentId(PaymentReqDTO.PersonalDTO dto);

    Payment findPaymentByBillId(String billId);

    List<PaymentRespDTO.PaymentAllBillDTO> findPaymentBill(String billId);

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

    List<PaymentRespDTO.DetailPaymentBillDTO> findDetailPaymentBillByStudentId(
            @Param("studentId") String studentId
    );

    String findCancelBillIdByPaymentKey(
            @Param("paymentKey") String paymentKey,
            @Param("type") String type);

    void updateBillStatusByBillIdAndStatus(
            @Param("billId") String billId,
            @Param("fromStatus") String fromStatus,
            @Param("toStatus") String toStatus
    );

    List<PaymentRespDTO.PaymentBillDTO> findBillsByBillIdAndType(
            @Param("billId") String billId,
            @Param("billType") String billType
    );


    PaymentRespDTO.PaymentDetailDTO findEduPaymentDetailByPaymentKey(
            @Param("paymentKey") String paymentKey);

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

    List<String> findPaymentDetailByPaymentKey(@Param("paymentKey") String paymentKey);

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

    String findBookBillStatus(
            @Param("paymentKey") String paymentKey,
            @Param("studentId") String studentId,
            @Param("yy") String yy,
            @Param("mm") String mm);

    List<CardCode> findUseCardCode();

    List<PaymentRespDTO.MonthlyPaymentDTO> findMonthlyPayments(
            @Param("centerCode") String centerCode,
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm
    );


    List<PaymentRespDTO.BillDetailDTO> findBillsByBillIds(@Param("list") List<String> billIds);

    List<PaymentRespDTO.ExpiredBillDTO> findExpriedBill(@Param("ymd") String ymd);

    int updateStatusToDestroyed(@Param("billId") String billId, @Param("ymd") String ymd);

    List<PaymentRespDTO.CashbillStudentRespDTO> findCashbillStudents(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("centerCode") String centerCode);

    void insertCashbill(PaymentCashbill cashbill);

    void updateManualGroupCashbillId(@Param("manualKey") String manualKey, @Param("cashbillId") String cashbillId);

    int updateCashbillInfo(@Param("studentId") String studentId,
                           @Param("paymentKey") String paymentKey,
                           @Param("billId") String billId);

    List<PaymentRespDTO.ClaimDto> getPaymentInfo(PaymentReqDTO.ClaimFilterDTO dto);

    List<PaymentRespDTO.CashBillHistoryDTO> findCashbillHistory(
            @Param("centerCode") String centerCode
    );

    PaymentCashbill findCashbillByBillId(@Param("billId") String billId);

    void updateCashbillStatus(
            @Param("billId") String billId,
            @Param("status") String status,
            @Param("reason") String reason
    );

    List<PaymentRespDTO.UnpaidStudentRespDTO> findUnpaidStudentForAlimtalk(
            @Param("centerCode") String centerCode,
            @Param("year") String year,
            @Param("month") String month
    );

    PaymentManual findManualById(Integer id);

    // 같은 manualKey 매뉴얼 전체 조회
    List<PaymentManual> findAllByManualKey(String manualKey);

    // 매뉴얼 수정
    void updateManual(@Param("id") Integer id, @Param("dto") PaymentReqDTO.UpdatePaymentDTO dto);

    // 매뉴얼 그룹 수정
    void updateManualGroup(@Param("manualKey") String manualKey,
                           @Param("paidDate") String paidDate,
                           @Param("totalAmount") Long totalAmount);

    // 매뉴얼 전체 삭제 (같은 manualKey)
    void deleteAllByManualKey(String manualKey);

    // 매뉴얼 그룹 삭제
    void deleteManualGroup(String manualKey);

    List<PaymentRespDTO.CashbillPrintDTO> findCashbillPrint(@Param("ym") String ym, @Param("centerCode") String centerCode);
}
