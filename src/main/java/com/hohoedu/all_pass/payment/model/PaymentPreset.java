package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass._core.utils.PaymentKeyGenerator;
import com.hohoedu.all_pass.center.Center;
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
@Table(name = "erp_payment_preset", uniqueConstraints = {@UniqueConstraint(name = "uq_preset_key", columnNames = "preset_key")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "preset_key", length = 30, nullable = false, unique = true)
    private String presetKey;

    @Column(name = "target_year", length = 4, nullable = false)
    private String targetYear;

    @Column(name = "target_month", length = 2, nullable = false)
    private String targetMonth;

    @Column(name = "total_amount", nullable = false)
    private String totalAmount;

    @Column(name = "monthly_amount", nullable = false)
    private String monthlyAmount;

    @Column(name = "months_count", nullable = false)
    private int monthsCount;

    @Column(name = "used_months", nullable = false)
    private int usedMonths = 0;

    @Column(length = 20, nullable = false)
    private String method;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "paid_date")
    private String paidDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @PrePersist
    public void generatePresetKey() {
        if (this.presetKey == null && this.center != null) {
            this.presetKey = PaymentKeyGenerator.generate(center.getCenterCode());
        }
    }

    public void useOneMonth() {
        this.usedMonths++;
        if (this.usedMonths >= this.monthsCount) {
            this.status = "done";
        } else {
            this.status = "linked";
        }
    }


    @Builder
    public PaymentPreset(String targetYear, String targetMonth, String totalAmount, String monthlyAmount,
                         int monthsCount, String paidDate, String method, String status, String note,
                         Student student, User user, Center center) {
        this.targetYear = targetYear;
        this.targetMonth = targetMonth;
        this.totalAmount = totalAmount;
        this.monthlyAmount = monthlyAmount;
        this.monthsCount = monthsCount;
        this.paidDate = paidDate;
        this.method = method;
        this.status = status;
        this.note = note;
        this.student = student;
        this.user = user;
        this.center = center;
    }
}