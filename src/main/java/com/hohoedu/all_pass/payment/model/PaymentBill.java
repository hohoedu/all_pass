package com.hohoedu.all_pass.payment.model;


import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_bill")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bill_id", nullable = false, length = 50)
    private String billId;

    @Column(name = "amount", nullable = false)
    private Integer amount; // 결제해야하는 금액

    @Column(name = "expire_date")
    private String expireDate; // (yyyy-MM-dd)

    @Column(name = "issued_date")
    private String issuedDate; // (yyyy-MM-dd)

    @Column(name = "status", length = 20)
    private String status; // 청구 상태 (issued, approved, cancelled, expired 등)

    @Column(name = "bill_type", nullable = false)
    private String billType;

    @Column
    private String yy;

    @Column
    private String mm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Builder
    public PaymentBill(String billId, Integer amount, String expireDate, String issuedDate,
                       String status, String billType, String yy, String mm, Payment payment, Student student, Center center) {
        this.billId = billId;
        this.amount = amount;
        this.expireDate = expireDate;
        this.issuedDate = issuedDate;
        this.status = status;
        this.billType = billType;
        this.yy = yy;
        this.mm = mm;
        this.payment = payment;
        this.student = student;
        this.center = center;
    }
}