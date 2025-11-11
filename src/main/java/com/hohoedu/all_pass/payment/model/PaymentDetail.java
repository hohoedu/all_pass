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
@Table(name = "erp_payment_detail")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "class_type", length = 20, nullable = false)
    private String classType; // 한자 / 독서 등

    @Column(name = "item_type", length = 20, nullable = false)
    private String itemType; // EDU_FEE / BOOK_FEE 등

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_key", referencedColumnName = "payment_key", nullable = false)
    private Payment payment;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public PaymentDetail(Payment payment, String classType, String itemType, Integer amount, String note) {
        this.payment = payment;
        this.classType = classType;
        this.itemType = itemType;
        this.amount = amount;
        this.note = note;
    }
}
