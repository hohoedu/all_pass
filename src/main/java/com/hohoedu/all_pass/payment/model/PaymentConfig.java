package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass.admin.center.Center;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_config")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "pre_bill_id", nullable = false)
    private String preBillId;

    @Column(name = "api_key", nullable = false)
    private String apiKey;

    @Column(name = "member_id", nullable = false)
    private String memberId;

    @Column(name = "merchant_id", nullable = false)
    private String merchantId;

    @Column(name = "callback_url")
    private String callbackUrl;

    @Column(name = "send_url")
    private String sendUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center centerCode;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentConfig(String preBillId, String apiKey, String memberId, String merchantId, String callbackUrl, String sendUrl, Center centerCode) {
        this.preBillId = preBillId;
        this.apiKey = apiKey;
        this.memberId = memberId;
        this.merchantId = merchantId;
        this.callbackUrl = callbackUrl;
        this.sendUrl = sendUrl;
        this.centerCode = centerCode;
    }
}
