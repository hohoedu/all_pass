package com.hohoedu.all_pass.payment._dto.web;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.student.Student;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Data
public class PaymentRespDTO {

    @Data
    public static class AssignStudentsDTO {
        private String studentId;
        private String paymentKey;
        private String eduBillId;
        private String materialBillId;
        private String studentName;
        private String subject;
        private String hanTeacher;
        private String bookTeacher;
        private Integer hanFee;
        private Integer hanMaterialFee;
        private Integer bookFee;
        private Integer bookMaterialFee;
        private String eduStatus;
        private String materialStatus;
        private String totalStatus;
        private String totalPrice;
        private String unpaidAmount;
        private String amountDue;
        private String totalFee;
        private String totalMaterialFee;
        private String parentPhone;
        private Integer unpaidEduAmount;
        private Integer unpaidMaterialAmount;
        private String samePhoneStudents;
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
        private String destroyUrl;
        private String cancelUrl;
    }

    @Data
    public static class PayTargetDTO {
        private String studentId;
        private String studentName;
        private String parentPhone;   // 01012345678
        private String paymentKey;
        private Integer amount;
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
    public static class ClassFeeMapDTO {
        private String classKey;
        private String className;
        private String fee;
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
    }

    @Data
    public static class UnpaidStudentDTO {
        private String studentId;
        private String studentName;
        private String paymentKey;
        private String gradeName;
        private String hanTeacher;
        private String bookTeacher;
    }

    @Data
    public static class ManualPaymentRespDTO {
        private String paymentKey;
        private String studentId;
        private Integer price;
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
        private String studentId;
        private String billId;
        private String studentName;
        private String billType;
        private String paidDate;
        private String method;
        private String paidPrice;
        private String billAmount;
        private String unpaidAmount;
    }

    @Data
    public static class PaymentDetailDTO {
        private Integer amount;
    }

}
