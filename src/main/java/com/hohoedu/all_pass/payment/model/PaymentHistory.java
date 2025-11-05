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
@Table(name = "erp_payment_history") // 결제 내역 테이블
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    @Column(name = "event_type")
    private String eventType; // 결제 상태 (issued, approved, cancelled, refunded 등)

    @Column(name = "event_source")
    private String eventSource; // 직접입력, 콜백 데이터,

    @Column
    private String amount; // 결제 금액

    @Column
    private String description; // 비고

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code") // 처리자 FK
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code") // 청구서 FK
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt; // 생성일시

    @Builder
    public PaymentHistory(String eventType, String eventSource, String amount, String description, User user, Student student, PaymentCode paymentCode, Center center, Payment payment) {
        this.eventType = eventType;
        this.eventSource = eventSource;
        this.amount = amount;
        this.description = description;
        this.user = user;
        this.student = student;
        this.paymentCode = paymentCode;
        this.center = center;
        this.payment = payment;
    }
}