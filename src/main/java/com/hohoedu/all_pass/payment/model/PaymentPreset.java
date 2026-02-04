package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_payment_preset", uniqueConstraints = {
        @UniqueConstraint(name = "uq_preset_key", columnNames = "preset_key")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "preset_key", length = 100, nullable = false, unique = true)
    private String presetKey;

    // ✨ 금액 정보
    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;  // 현재 남은 선납금

    @Column(name = "original_amount", nullable = false)
    private Integer originalAmount;  // 원래 선납한 금액

    // ✨ 사용 정보
    @Column(name = "used_months", nullable = false)
    private Integer usedMonths = 0;  // 사용한 개월 수

    // ✨ 추적 정보
    @Column(name = "original_manual_payment_id")
    private Integer originalManualPaymentId;  // 원본 수기결제 ID

    // 상태 및 방법
    @Column(length = 20, nullable = false)
    private String method;  // card, cash, transfer, mixed

    @Column(name = "card_name")
    private String cardName;

    @Column(length = 20, nullable = false)
    private String status;  // active, completed, cancelled

    // 메모 및 일자
    @Column(name = "paid_date")
    private String paidDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    // ✨ 연관 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    // ✨ 시간 정보
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Builder
    public PaymentPreset(String presetKey, Integer totalAmount, Integer originalAmount, Integer usedMonths,
                         Integer originalManualPaymentId, String cardName, String method, String status,
                         String paidDate, String note, Student student, User user, Center center) {
        this.presetKey = presetKey;
        this.totalAmount = totalAmount;
        this.originalAmount = originalAmount;
        this.usedMonths = usedMonths;
        this.originalManualPaymentId = originalManualPaymentId;
        this.cardName = cardName;
        this.method = method;
        this.status = status;
        this.paidDate = paidDate;
        this.note = note;
        this.student = student;
        this.user = user;
        this.center = center;
    }
}
