package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass.admin.center.Center;
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
@Table(name = "erp_payment_refund", uniqueConstraints = {
        @UniqueConstraint(name = "uq_refund_key", columnNames = "refund_key")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "refund_key", length = 30, nullable = false, unique = true)
    private String refundKey;

    @Column(nullable = false)
    private Integer amount;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(length = 20, nullable = false)
    private String method;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "request_date")
    private String requestDate;

    @Column(name = "processed_date")
    private String processedDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key", nullable = false)
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
    public PaymentRefund(String refundKey, Integer amount, String reason, String method, String status,
                         String requestDate, String processedDate, String note,
                         Payment payment, Student student, Center center, User user) {
        this.refundKey = refundKey;
        this.amount = amount;
        this.reason = reason;
        this.method = method;
        this.status = status;
        this.requestDate = requestDate;
        this.processedDate = processedDate;
        this.note = note;
        this.payment = payment;
        this.student = student;
        this.center = center;
        this.user = user;
    }
}
