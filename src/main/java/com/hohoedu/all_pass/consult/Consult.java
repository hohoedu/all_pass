package com.hohoedu.all_pass.consult;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.consult.model.InflowRoute;
import com.hohoedu.all_pass.consult.model.ProgressCode;
import com.hohoedu.all_pass.student.model.GradeCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_consult")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_name", nullable = false, length = 20)
    private String studentName;

    @Column(name = "consult_date", nullable = false, length = 20)
    private String consultDate;

    @Column(nullable = false, length = 20)
    private String school;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_key", referencedColumnName = "grade_key", nullable = false)
    private GradeCode gradeCode; // 코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_key", referencedColumnName = "progress_key")
    private ProgressCode progressCode; // 코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inflow_route_key", referencedColumnName = "inflow_route_key", nullable = false)
    private InflowRoute inflowRoute; // 코드

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder

    public Consult(Integer id, String studentName, String consultDate, String school, String phone, String content, GradeCode gradeCode, ProgressCode progressCode, InflowRoute inflowRoute, LocalDateTime createdAt) {
        this.id = id;
        this.studentName = studentName;
        this.consultDate = consultDate;
        this.school = school;
        this.phone = phone;
        this.content = content;
        this.gradeCode = gradeCode;
        this.progressCode = progressCode;
        this.inflowRoute = inflowRoute;
        this.createdAt = createdAt;
    }
}
