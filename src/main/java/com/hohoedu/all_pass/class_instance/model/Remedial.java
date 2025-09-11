package com.hohoedu.all_pass.class_instance.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Entity
@Table(name = "erp_remedial", uniqueConstraints = {@UniqueConstraint(name = "uq_remedial_key", columnNames = "remedial_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Remedial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "remedial_key", nullable = false, length = 50)
    private String remedialKey;

    @Column(name = "remedial_subject", nullable = false, length = 100)
    private String remedialSubject;

    @Column(name = "absence_date", nullable = false)
    private String absenceDate;

    @Column(name = "remedial_date")
    private String remedialDate;

    @Column(name = "action", nullable = false)
    private boolean action;

    @Column(name = "week", nullable = false)
    private String week;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @Builder
    public Remedial(String remedialKey, String remedialSubject, String absenceDate, String remedialDate,
                    boolean action, String week, Student student, User user, TimeTable timeTable) {
        this.remedialKey = remedialKey;
        this.remedialSubject = remedialSubject;
        this.absenceDate = absenceDate;
        this.remedialDate = remedialDate;
        this.action = action;
        this.week = week;
        this.student = student;
        this.user = user;
        this.timeTable = timeTable;
    }
}