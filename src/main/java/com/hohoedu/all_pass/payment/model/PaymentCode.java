package com.hohoedu.all_pass.payment.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_payment_code" , uniqueConstraints = {@UniqueConstraint(name = "uq_payment_key", columnNames = "payment_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    @Column(name = "payment_key", nullable = false)
    private String paymentKey; // 결제수단 코드 (card, cash, vbank 등)

    @Column(name = "payment_name")
    private String paymentName; // 결제수단 이름 (카드, 현금, 가상계좌 등)

    @Builder
    public PaymentCode(String paymentKey, String paymentName) {
        this.paymentKey = paymentKey;
        this.paymentName = paymentName;
    }
}