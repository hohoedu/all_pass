package com.hohoedu.all_pass.payment.model;


import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_bill", uniqueConstraints = {
        @UniqueConstraint(name = "uq_bill_id", columnNames = "bill_id")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bill_id", nullable = false, unique = true, length = 50)
    private String billId; // 결제선생 bill_id (외부 시스템 고유 ID)

    @Column(name = "amount", nullable = false)
    private String amount; // 청구 금액

    @Column(name = "expire_date")
    private String expireDate; // 청구 만료일

    @Column(name = "issued_date")
    private String issuedDate; // 청구 생성일

    @Column(name = "status", length = 20)
    private String status; // 청구 상태 (issued, approved, cancelled, expired 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key", nullable = false)
    private Payment payment; // 내부 결제 기준키 연결

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code")
    private User user;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentBill(String billId, String amount, String expireDate, String issuedDate,
                       String status, Payment payment, Student student, Center center, User user) {
        this.billId = billId;
        this.amount = amount;
        this.expireDate = expireDate;
        this.issuedDate = issuedDate;
        this.status = status;
        this.payment = payment;
        this.student = student;
        this.center = center;
        this.user = user;
    }
}