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
@Table(name = "erp_payment_manual")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentManual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "card_amount")
    private Integer cardAmount;

    @Column(name = "cash_amount")
    private Integer cashAmount;

    @Column(name = "transfer_amount")
    private Integer transferAmount;

    @Column(name = "card_name")
    private String cardName;

    @Column
    private String paidDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code ")
    private User user;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentManual(Integer cardAmount, Integer cashAmount, Integer transferAmount, String cardName, String paidDate, Student student, Payment payment, User user) {
        this.cardAmount = cardAmount;
        this.cashAmount = cashAmount;
        this.transferAmount = transferAmount;
        this.cardName = cardName;
        this.paidDate = paidDate;
        this.student = student;
        this.payment = payment;
        this.user = user;
    }
}
