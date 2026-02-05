package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_manual_group")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentManualGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String manualGroupKey;

    private Integer totalAmount;

    private String cardName;

    private String method;

    private String paidDate;

    private String yy;

    private String mm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code")
    private User user;

    @CreationTimestamp
    private Timestamp createdAt;
}
