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
@Table(name = "erp_payment_callback") // 결제 콜백 테이블
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCallback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    @Column(name = "api_key")
    private String apiKey; // 결제선생 API Key

    @Column(name = "appr_pay_type")
    private String apprPayType; // 결제유형 (card, vbank 등)

    @Column(name = "appr_card_type")
    private String apprCardType; // 카드종류 (VISA, BC 등)

    @Column(name = "appr_date")
    private String apprDate; // 승인일자

    @Column(name = "appr_price")
    private String apprPrice; // 승인금액

    @Column(name = "appr_issuer")
    private String apprIssuer; // 카드사명

    @Column(name = "appr_num")
    private String apprNum; // 승인번호

    @Column(name = "appr_state")
    private String apprState; // 상태 (1: 결제완료, 9: 취소 등)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", referencedColumnName = "bill_id") // 청구서 FK
    private Payment payment;

    @CreationTimestamp
    private Timestamp created; // 수신일시

    @Builder
    public PaymentCallback(String apiKey, String apprPayType, String apprCardType, String apprDate, String apprPrice, String apprIssuer, String apprNum, String apprState, Payment payment, Timestamp created) {
        this.apiKey = apiKey;
        this.apprPayType = apprPayType;
        this.apprCardType = apprCardType;
        this.apprDate = apprDate;
        this.apprPrice = apprPrice;
        this.apprIssuer = apprIssuer;
        this.apprNum = apprNum;
        this.apprState = apprState;
        this.payment = payment;
        this.created = created;
    }
}