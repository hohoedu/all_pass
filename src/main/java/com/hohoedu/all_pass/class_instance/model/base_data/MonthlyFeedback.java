package com.hohoedu.all_pass.class_instance.model.base_data;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_monthly_feedback", uniqueConstraints = {
        @UniqueConstraint(name = "uq_monthly_feedback_key", columnNames = "monthly_feedback_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "monthly_feedback_key", nullable = false)
    private String monthlyFeedbackKey;

    @Column(name = "yy")
    private String yy;

    @Column(name = "number")
    private String number;

    @Column(name = "difficultly", nullable = false)
    private String difficultly;

    @Column(name = "competency", nullable = false)
    private String competency;

    @Column(name = "correct_ment", columnDefinition = "nvarchar(300)", nullable = false)
    private String correctMent;

    @Column(name = "wrong_ment", columnDefinition = "nvarchar(300)", nullable = false)
    private String wrongMent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @Builder
    public MonthlyFeedback(String monthlyFeedbackKey, String yy, String number, String difficultly, String competency, String correctMent, String wrongMent, ClassCode classCode, UnitCode unitCode) {
        this.monthlyFeedbackKey = monthlyFeedbackKey;
        this.yy = yy;
        this.number = number;
        this.difficultly = difficultly;
        this.competency = competency;
        this.correctMent = correctMent;
        this.wrongMent = wrongMent;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }
}
