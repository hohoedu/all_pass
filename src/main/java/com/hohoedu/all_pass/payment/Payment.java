package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass.admin.center.Center;
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
@Table(name = "erp_payment", uniqueConstraints = @UniqueConstraint(name = "uq_payment_key", columnNames = "payment_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "payment_key", nullable = false, unique = true, length = 30)
    private String paymentKey;

    @Column(length = 20)
    private String method;

    @Column
    private Integer amount; // 월 전체 학원비의 총 합

    @Column(name = "unpaidAmount")
    private Integer unpaidAmount;

    /*
     *결제 상태
     * - pending :
     * - issued : 청구서 발행 (결제 대기)
     * - approved : 결제 완료
     * - cancelled : 결제 취소
     * - expired : 청구 만료
     * - linked : 선결제 연결 완료
     */
    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "paid_date")
    private String paidDate; // (yyyy-MM-dd HH:mm)

    @Column(name = "request_date")
    private String requestDate; // (yyyy-MM-dd)

    @Column
    private String yy;

    @Column
    private String mm;

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
    public Payment(String paymentKey, String method, Integer amount, Integer unpaidAmount, String status, String paidDate, String requestDate, String yy, String mm, Student student, Center center) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.amount = amount;
        this.unpaidAmount = unpaidAmount;
        this.status = status;
        this.paidDate = paidDate;
        this.requestDate = requestDate;
        this.yy = yy;
        this.mm = mm;
        this.student = student;
        this.center = center;
    }
}