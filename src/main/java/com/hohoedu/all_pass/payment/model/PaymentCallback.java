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
@Table(name = "erp_payment_callback")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "bill_id")
    private String billId;

    @Column(name = "appr_cat_id")
    private String apprCatId;



    // 결제수단
    @Column(name = "appr_pay_type")
    private String apprPayType;

    // 결제 카드 종류
    @Column(name = "appr_card_type")
    private String apprCardType;

    // 승인 일시
    @Column(name = "appr_date")
    private String apprDate;

    // 결제은행 / 카드명
    @Column(name = "appr_issuer")
    private String apprIssuer;

    // 은행 코드
    @Column(name = "appr_issuer_code")
    private String apprIssuerCode;

    // 결제 카드 번호
    @Column(name = "appr_issuer_num")
    private String apprIssuerNum;

    // 승인번호
    @Column(name = "appr_num")
    private String apprNum;

    // 승인 금액
    @Column(name = "appr_price")
    private String apprPrice;

    // 결제 상태
    @Column(name = "appr_state", nullable = false)
    private String apprState;

    // 결제 할부 개월 수
    @Column(name = "appr_monthly")
    private String apprMonthly;

    // 매입사 코드
    @Column(name = "appr_acquirer_code")
    private String apprAcquirerCode;

    // 매입사 명
    @Column(name = "appr_acquirer_name")
    private String apprAcquirerName;

    // 원거래 일시
    @Column(name = "appr_origin_date")
    private String apprOriginDate;

    // 원거래 승인버호
    @Column(name = "appr_origin_num")
    private String apprOriginNum;

    @Column(name = "yy")
    private String yy;

    @Column(name = "mm")
    private String mm;

    @Column(name = "center_code")
    private String centerCode;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentCallback(String apiKey, String billId, String apprCatId, String apprPayType, String apprCardType, String apprDate, String apprIssuer, String apprIssuerCode, String apprIssuerNum, String apprNum, String apprPrice, String apprState, String apprMonthly, String apprAcquirerCode, String apprAcquirerName, String apprOriginDate, String apprOriginNum, String yy, String mm, String centerCode) {
        this.apiKey = apiKey;
        this.billId = billId;
        this.apprCatId = apprCatId;
        this.apprPayType = apprPayType;
        this.apprCardType = apprCardType;
        this.apprDate = apprDate;
        this.apprIssuer = apprIssuer;
        this.apprIssuerCode = apprIssuerCode;
        this.apprIssuerNum = apprIssuerNum;
        this.apprNum = apprNum;
        this.apprPrice = apprPrice;
        this.apprState = apprState;
        this.apprMonthly = apprMonthly;
        this.apprAcquirerCode = apprAcquirerCode;
        this.apprAcquirerName = apprAcquirerName;
        this.apprOriginDate = apprOriginDate;
        this.apprOriginNum = apprOriginNum;
        this.yy = yy;
        this.mm = mm;
        this.centerCode = centerCode;
    }
}