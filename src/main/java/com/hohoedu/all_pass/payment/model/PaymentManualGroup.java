package com.hohoedu.all_pass.payment.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Column(name = "manual_key", unique = true, nullable = false)
    private String manualKey;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "card_name")
    private String cardName;

    @Column(name = "method")
    private String method;

    @Column(name = "paid_date")
    private String paidDate;

    @Column
    private String yy;

    @Column
    private String mm;

    @Column(name = "student_id")
    private String studentId;

    @Column(name = "center_code")
    private String centerCode;


    @Column(name = "user_code")
    private String userCode;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Builder
    public PaymentManualGroup(String manualKey, Integer totalAmount, String cardName, String method, String paidDate,
                              String yy, String mm, String studentId, String centerCode, String userCode) {
        this.manualKey = manualKey;
        this.totalAmount = totalAmount;
        this.cardName = cardName;
        this.method = method;
        this.paidDate = paidDate;
        this.yy = yy;
        this.mm = mm;
        this.studentId = studentId;
        this.centerCode = centerCode;
        this.userCode = userCode;
    }
}