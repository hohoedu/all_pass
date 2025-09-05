package com.hohoedu.all_pass.class_instance.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import com.hohoedu.all_pass.student.Student;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "erp_monthly_score", uniqueConstraints = {@UniqueConstraint(name = "uq_monthly_score_one_row", columnNames = {"student_id", "yy", "mm", "time_table_key"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private boolean question1;

    @Column(nullable = false)
    private boolean question2;

    @Column(nullable = false)
    private boolean question3;

    @Column(nullable = false)
    private boolean question4;

    @Column(nullable = false)
    private boolean question5;

    @Column(nullable = false)
    private boolean question6;

    @Column(nullable = false)
    private boolean question7;

    @Column(nullable = false)
    private boolean question8;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @Column(name = "yy", nullable = false, length = 10)
    private String yy;

    @Column(name = "mm", nullable = false, length = 10)
    private String mm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public MonthlyScore(boolean question1, boolean question2, boolean question3, boolean question4,
                        boolean question5, boolean question6, boolean question7, boolean question8,
                        Student student, String yy, String mm, TimeTable timeTable) {
        this.question1 = question1;
        this.question2 = question2;
        this.question3 = question3;
        this.question4 = question4;
        this.question5 = question5;
        this.question6 = question6;
        this.question7 = question7;
        this.question8 = question8;
        this.student = student;
        this.yy = yy;
        this.mm = mm;
        this.timeTable = timeTable;
    }
}