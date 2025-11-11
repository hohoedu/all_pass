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
@Table(name = "erp_payment_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;
    // ex) issued(청구), approved(결제승인), cancelled(결제취소),
    //     manual(수기입력), preset_linked(선결제연결), refunded(환불)

    @Column(name = "event_source", nullable = false, length = 30)
    private String eventSource;
    // ex) system(자동처리), user(직접입력), callback(PG 콜백)

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "amount")
    private Integer amount;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", referencedColumnName = "id", nullable = false)
    private Payment payment;

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
    public PaymentHistory(String eventType, String eventSource, String status,
                          Integer amount, String description, Payment payment,
                          Student student, Center center, User user) {
        this.eventType = eventType;
        this.eventSource = eventSource;
        this.status = status;
        this.amount = amount;
        this.description = description;
        this.payment = payment;
        this.student = student;
        this.center = center;
        this.user = user;
    }
}