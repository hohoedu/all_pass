package com.hohoedu.all_pass.payment;

import com.hohoedu.all_pass.center.Center;
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
@Table(name = "erp_payment", uniqueConstraints = @UniqueConstraint(name = "uq_bill_id", columnNames = "bill_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    @Column(name = "bill_id", nullable = false)
    private String billId; //결제선생 bill_id

    @Column(name = "product_name")
    private String productName; // 청구 사유

    @Column
    private String amount; // 결제 가격

    @Column(name = "payment_status")
    private String paymentStatus; // 결제 상태

    @Column
    private String message; // 안내 메시지

    @Column(name = "request_date")
    private String requestDate; // 청구 날짜 yy-mm-dd

    @Column(name = "approved_date")
    private String approvedDate; // 결제 날짜 yy-mm-dd hh:mm

    @Column(name = "canceled_date")
    private String canceledDate; // 취소 날짜 yy-mm-dd hh:mm

    @Column(name = "expired_date")
    private String expiredDate; // 청구서 파기 날짜 yy-mm-dd

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id") // 학생 id (FK)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code") // 선생님 코드 (FK)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code") // 지점 코드 (FK)
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public Payment(String billId, String productName, String amount, String paymentStatus, String message, String requestDate, String approvedDate, String canceledDate, String expiredDate, Student student, User user, Center center, Timestamp createdAt) {
        this.billId = billId;
        this.productName = productName;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.message = message;
        this.requestDate = requestDate;
        this.approvedDate = approvedDate;
        this.canceledDate = canceledDate;
        this.expiredDate = expiredDate;
        this.student = student;
        this.user = user;
        this.center = center;
        this.createdAt = createdAt;
    }
}
