package com.hohoedu.all_pass.student.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.center.model.Center;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.code.LevelCode;
import com.hohoedu.all_pass.student.code.StatusCode;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "student_tb")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentNo;

    @Column(nullable = false, length = 20, unique = true)
    private String studentId;

    @Column(nullable = false, length = 10)
    private String studentName;

    @Column(nullable = false, length = 20)
    private String birth;

    @Column(nullable = false)
    private boolean gender;

    @Column(nullable = false, length = 20)
    private String school;

    @Column(nullable = false, length = 20)
    private String address;

    @Column(nullable = false, length = 20)
    private String addressDetail;

    @Column(nullable = true, length = 20)
    private String entryHanDate;

    @Column(nullable = true, length = 20)
    private String entryBookDate;

    private boolean studentPrivacyAgree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_no")
    private GradeCode gradeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_no")
    private LevelCode levelCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_no")
    private StatusCode statusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_no")
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public Student(Integer studentNo, String studentId, String studentName, String birth, boolean gender, String school,
            String address,
            String addressDetail, String entryHanDate, String entryBookDate, boolean studentPrivacyAgree,
            GradeCode gradeCode, LevelCode levelCode, StatusCode statusCode, Center center, Timestamp createdAt) {
        this.studentNo = studentNo;
        this.studentId = studentId;
        this.studentName = studentName;
        this.birth = birth;
        this.gender = gender;
        this.school = school;
        this.address = address;
        this.addressDetail = addressDetail;
        this.entryHanDate = entryHanDate;
        this.entryBookDate = entryBookDate;
        this.studentPrivacyAgree = studentPrivacyAgree;
        this.gradeCode = gradeCode;
        this.levelCode = levelCode;
        this.statusCode = statusCode;
        this.center = center;
        this.createdAt = createdAt;
    }

}
