package com.hohoedu.all_pass.payment.model;

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
@Table(name = "erp_payment_refund") // 결제 환불 테이블
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    @Column(name = "refund_amount")
    private String refundAmount; // 환불 금액

    @Column(name = "refund_reason")
    private String refundReason; // 환불 사유

    @Column(name = "refund_date")
    private String refundDate; // 환불일자

    @Column(name = "refund_method")
    private String refundMethod; // 환불수단 (cash, transfer, card_cancel 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code") // 환불 처리자 FK
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id") // 환불 대상 학생 FK
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id") // 청구서 FK
    private Payment payment;

    @CreationTimestamp
    private Timestamp createdAt; // 생성일시

    @Builder
    public PaymentRefund(String refundAmount, String refundReason, String refundDate, String refundMethod, User user, Student student, Payment payment, Timestamp createdAt) {
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundDate = refundDate;
        this.refundMethod = refundMethod;
        this.user = user;
        this.student = student;
        this.payment = payment;
        this.createdAt = createdAt;
    }
}
