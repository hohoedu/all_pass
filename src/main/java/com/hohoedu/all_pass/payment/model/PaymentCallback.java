package com.hohoedu.all_pass.payment.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;
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

    @Column(name = "api_key")
    private String apiKey;

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

    @Column(name = "bill_id")
    private String billId;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentCallback(String apiKey, String apprState, String apprDate, String apprPrice, String apprPayType,
                           String apprCardType, String apprIssuer, String apprNum, String billId) {
        this.apiKey = apiKey;
        this.apprState = apprState;
        this.apprDate = apprDate;
        this.apprPrice = apprPrice;
        this.apprPayType = apprPayType;
        this.apprCardType = apprCardType;
        this.apprIssuer = apprIssuer;
        this.apprNum = apprNum;
        this.billId = billId;
    }
}