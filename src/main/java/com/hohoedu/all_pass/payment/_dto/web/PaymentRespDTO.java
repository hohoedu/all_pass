package com.hohoedu.all_pass.payment._dto.web;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
public class PaymentRespDTO {

    @Data
    public static class MainPaymentSummaryDTO {
        private String studentId;
        private String studentName;
        private Long billPrice;
        private String className;
        private String classType;
        private String itemType;
        private String billExpireAt;
        private String yy;
        private String mm;
        private Integer unpaidCount;
        private Integer totalCount;
        private Double unpaidRate;
    }

    @Data
    public static class AssignStudentsDTO {
        private String studentId;
        private String paymentKey;
        private String billId;
        private String studentName;
        private String billPrice;           // 청구 금액
        private String subject;             // 선택된 선생님 수업
        private String otherTeacher;        // 선택되지 않은 선생님 이름
        private String otherSubject;        // 선택되지 않은 수업
        private String otherSubjectType;    // 선택되지 않은 수업 종류

//        private Integer hanFee;
//        private Integer hanMaterialFee;
//        private Integer bookFee;
//        private Integer bookMaterialFee;
//        private String eduStatus;
//        private String materialStatus;


        private String paidAmount;          // 결제 금액
        private String unpaidAmount;        // 미납 금액
        private String issuanceStatus;      // 발행 여부
        private String payStatus;         // 결제 여부

        private String parentPhone;         // 부모님 핸드폰 번호
        private String samePhoneStudents;   // 전화번호 같은 형제들

        private Integer isPriceModified;    // 금액 수정 여부 (0: 일치, 1: 불일치)
        private Integer standardFee;
    }

    @Data
    public static class PaymentConfigDTO {
        private String centerCode;
        private String apiKey;
        private String memberId;
        private String merchantId;
        private String preBillId;
        private String callbackUrl;
        private String sendUrl;
        private String resendUrl;
        private String destroyUrl;
        private String cancelUrl;
        private String cashbillIssueUrl;
        private String cashbillCancelUrl;
    }

    @Data
    public static class PayTargetDTO {
        private String studentId;
        private String studentName;
        private String billingPhone;
        private String parentPhone;   // 01012345678
        private String paymentKey;
        private Integer amount;

        private boolean additionalCharge;
    }

    @Data
    public static class PaymentBillDTO {
        private String billId;
        private String paymentKey;
        private Integer price;
        private String status;
    }

    @Data
    public static class PaymentAllBillDTO {
        private Integer id;
        private String billId;
        private Integer amount;
        private String expireDate;
        private String issuedDate;
        private String status;
        private String billType;
        private String yy;
        private String mm;
        private String paymentKey;
        private String studentId;
        private String centerCode;
        private Timestamp createdAt;
        private Timestamp updatedAt;
    }

    @Data
    public static class DetailPaymentBillDTO {
        private String billId;
        private String paymentKey;
        private String studentId;
        private String studentName;
        private String type;
        private String billType;
        private Integer amount;
        private String expireDate;
        private String paidDate;
        private String cardName;
        private String status;
        private String ym;
        private String createdAt;

    }

    @Data
    public static class PaySendRespDTO {
        private String billId;
        private String paymintCode;     // Paymint 응답코드
        private String paymintMsg;      // Paymint 메시지
        private boolean dbSaved;
    }

    @Data
    public static class PaymintRespDTO {
        private String code;
        private String msg;
    }

    @Data
    public static class CashBillRespDTO {
        private String bill_id;
        private String trader;
        private String appr_cash_num;
        private String code;
        private String msg;
    }

    @Data
    public static class ClassFeeMapDTO {
        private String classKey;
        private String className;
        private String fee;
        private String category;
        private String classType;
        private String unitKey;
        private String unitName;
    }

    @Data
    public static class PaymentModalDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String classDate;
        private String subject;
        private String hanTeacher;
        private String bookTeacher;
        private String billType;
        private Integer amount;
        private String status;
        private String paidDate;
        private Integer hanMaterialFee;
        private Integer bookMaterialFee;
        private Integer hanFee;
        private Integer bookFee;
        private String billingPhone;
    }

    @Data
    public static class UnpaidStudentDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String gradeName;
        private String hanTeacher;
        private String bookTeacher;
        private Integer amount;
        private Integer eduFeeTotal;      // 교육비 detail 총액
        private Integer eduBillPaid;      // 교육비 bill 결제 금액 (approved)
        private Integer eduManualPaid;    // 교육비 manual 결제 금액
        private Integer eduUnpaidAmount;  // 교육비 미납 금액 = total - bill - manual
        private String paymentStatus;
        private String phoneNumber;
    }

    @Data
    public static class ManualPaymentRespDTO {
        private String paymentKey;
        private Integer price;              // 총 결제 금액
        private Integer actualBillPayment; // 이번 달 청구서에 적용된 금액
        private Integer prepaidAmount;     // 선납금
        private String studentId;
        private String message;
    }

    @Data
    public static class BillRespDTO {
        private String billId;
        private String paymentKey;
        private String studentId;
        private int count;
    }

    @Data
    public static class MonthlyPaymentDTO {
        private String id;
        private String studentName;
        private String paidDate;
        private String paymentKey;
        private Integer cash;
        private Integer card;
        private Integer transfer;
        private String apprCashNum;
    }

    @Data
    public static class PaymentDetailDTO {
        private Integer amount;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillDetailDTO {
        private String billId;
        private String paymentKey;
        private String studentId;
        private String studentName;
        private Integer amount;
        private String billType;
        private String phone;
        private String centerCode;
        private String expireDate;
        private String yy;
        private String mm;
        private String status;
        private String message;
    }

    @Data
    public static class MaterialFeeDTO {
        private String studentId;
        private String paymentKey;
        private Integer hanMaterialFee;
        private Integer bookMaterialFee;
    }

    @Data
    public static class ExpiredBillDTO {
        private String studentId;
        private String paymentKey;
        private String billId;
    }

    @Data
    public static class CashbillStudentRespDTO {
        private String studentId;
        private String paymentKey;
        private String studentName;
        private String gradeName;
        private String hanTeacher;
        private String bookTeacher;
        private String amount;
        private String phoneNumber;
    }

    @Data
    public static class ClaimDto {
        private String studentName;
        private String subject;
        private String teacherName;
        private String billPrice;
        private String paidAmount;
        private String unpaidAmount;
        private String payStatus;
    }

    @Data
    public static class CashBillHistoryDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String billId;
        private String apprNum;
        private String issueDate;
        private String receiptType;
        private String price;
        private String taxType;
        private String supplyPrice;
        private String taxPrice;
        private String status;
    }

    @Data
    public static class CashbillPrintDTO {
        private String billId;
        private String studentInfo;   // 학생정보
        private String apprDate;      // 거래일자
        private String receiptNum;    // 식별번호
        private String apprCashNum;   // 승인번호
        private Integer supplyPrice;  // 공급가액
        private Integer taxPrice;     // 부가세
        private Integer totalPrice;   // 합계
        private String centerName;       // 가맹점명
        private String directorName;  // 대표자
        private String bizNo;         // 사업자등록번호
        private String centerTel;     // 전화번호
        private String centerAddress; // 주소
    }

    @Data
    public static class CashbillCancelRespDTO {
        private Boolean success;
        private int failCount;
        private String message;
    }

    @Data
    public static class UnpaidStudentRespDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String parentPhone;
        private String totalUnpaidAmount;
    }
}
