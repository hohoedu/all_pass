package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass.payment.Payment;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_callback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "bill_id", nullable = false, length = 50)
    private String billId;

    @Column(name = "appr_state", nullable = false)
    private String apprState;

    @Column(name = "appr_date")
    private String apprDate;

    @Column(name = "appr_price")
    private String apprPrice;

    @Column(name = "appr_pay_type")
    private String apprPayType;

    @Column(name = "appr_card_type")
    private String apprCardType;

    @Column(name = "appr_issuer")
    private String apprIssuer;

    @Column(name = "appr_num")
    private String apprNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key", nullable = false)
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id", insertable = false, updatable = false)
    private PaymentBill paymentBill;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentCallback(String billId, String apprState, String apprDate, String apprPrice,
                           String apprPayType, String apprCardType, String apprIssuer,
                           String apprNum, Payment payment) {
        this.billId = billId;
        this.apprState = apprState;
        this.apprDate = apprDate;
        this.apprPrice = apprPrice;
        this.apprPayType = apprPayType;
        this.apprCardType = apprCardType;
        this.apprIssuer = apprIssuer;
        this.apprNum = apprNum;
        this.payment = payment;
    }
}