package com.hohoedu.all_pass.consult;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.consult.model.InflowRoute;
import com.hohoedu.all_pass.consult.model.ProgressCode;
import com.hohoedu.all_pass.student.model.GradeCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "consult")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Consult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer consultNo;

    @Column(nullable = false, length = 10)
    private String studentName;

    @Column(nullable = false, length = 10)
    private String consultDate;

    @Column(nullable = false, length = 20)
    private String school;

    @Column(nullable = false, length = 13)
    private String phone;

    @Column(nullable = false, length = 100)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_no")
    private GradeCode gradeCode; // 코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_no")
    private ProgressCode progressCode; // 코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inflow_route_no")
    private InflowRoute inflowRoute; // 코드

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public Consult(Integer consultNo, String studentName, String consultDate, String school, String phone,
            String content, GradeCode gradeCode, ProgressCode progressCode, InflowRoute inflowRoute,
            Timestamp createdAt) {
        this.consultNo = consultNo;
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
