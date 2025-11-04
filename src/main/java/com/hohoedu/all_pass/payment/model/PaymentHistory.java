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
@Table(name = "erp_payment_history") // 결제 내역 테이블
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    @Column (name = "pat_status")
    private String payStatus; // 결제 상태 (approved, cancelled 등)

    @Column
    private String amount; // 결제 금액

    @Column (name = "appr_num")
    private String apprNum; // 승인번호 (카드 결제 시)

    @Column (name = "appr_date")
    private String apprDate; // 승인일시 yy-mm-dd hh:mm

    @Column
    private String memo; // 비고

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code") // 결제 처리자 FK
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id") // 결제 대상 학생 FK
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key") // 결제수단 코드 (FK)
    private PaymentCode paymentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id") // 청구서 FK
    private Payment payment;

    @CreationTimestamp
    private Timestamp createdAt; // 생성일시

    @Builder
    public PaymentHistory(String payStatus, String amount, String apprNum, String apprDate, String memo, User user, Student student, PaymentCode paymentCode, Payment payment, Timestamp createdAt) {
        this.payStatus = payStatus;
        this.amount = amount;
        this.apprNum = apprNum;
        this.apprDate = apprDate;
        this.memo = memo;
        this.user = user;
        this.student = student;
        this.paymentCode = paymentCode;
        this.payment = payment;
        this.createdAt = createdAt;
    }
}