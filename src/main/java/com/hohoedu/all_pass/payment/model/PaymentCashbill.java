package com.hohoedu.all_pass.payment.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_cashbill")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCashbill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "bill_id", nullable = false, unique = true, length = 100)
    private String billId;

    @Column(name = "payment_key", nullable = false, length = 100)
    private String paymentKey;

    @Column(name = "student_id", nullable = false)
    private String studentId;

    @Column(name = "receipt_type", nullable = false, length = 20)
    private String receiptType; // personal, business, self

    @Column(name = "receipt_number", nullable = false, length = 50)
    private String receiptNumber; // 휴대폰번호 or 사업자번호

    @Column(name = "trader", nullable = false, length = 10)
    private String trader; // 0, 1, 2

    @Column(name = "price", nullable = false, length = 20)
    private String price;

    @Column(name = "supply_price", nullable = false, length = 20)
    private String supplyPrice;

    @Column(name = "tax", nullable = false, length = 20)
    private String tax;

    @Column(name = "tax_type", nullable = false, length = 20)
    private String taxType; // taxable, tax-free

    @Column(name = "appr_cash_num", length = 50)
    private String apprCashNum; // 승인번호 (결제선생 응답)

    @Column(name = "hash_value", nullable = false, length = 255)
    private String hashValue;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ISSUED, CANCELLED

    @Column(name = "issue_date", nullable = false)
    private String issueDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Column(name = "cancelled_at")
    private Timestamp cancelledAt;

    @Column(name = "cancel_reason", length = 200)
    private String cancelReason;

    @Builder
    public PaymentCashbill(String billId, String paymentKey, String studentId, String receiptType, String receiptNumber, String trader, String price, String supplyPrice, String tax, String taxType, String apprCashNum, String hashValue, String status, String issueDate, Timestamp createdAt, Timestamp cancelledAt, String cancelReason) {
        this.billId = billId;
        this.paymentKey = paymentKey;
        this.studentId = studentId;
        this.receiptType = receiptType;
        this.receiptNumber = receiptNumber;
        this.trader = trader;
        this.price = price;
        this.supplyPrice = supplyPrice;
        this.tax = tax;
        this.taxType = taxType;
        this.apprCashNum = apprCashNum;
        this.hashValue = hashValue;
        this.status = status;
        this.issueDate = issueDate;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
        this.cancelReason = cancelReason;
    }
}