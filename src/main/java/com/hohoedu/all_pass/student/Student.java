package com.hohoedu.all_pass.student;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.student.model.LevelCode;
import com.hohoedu.all_pass.student.model.StatusCode;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_student", uniqueConstraints = @UniqueConstraint(name = "uk_student_id", columnNames = "student_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;

    @Column(name = "student_name", nullable = false, length = 50)
    private String studentName;

    @Column(nullable = false)
    private String birth;

    @Column(nullable = false)
    private Boolean gender;

    @Column(nullable = false, length = 50)
    private String school;

    @Column(nullable = false, length = 100)
    private String address;

    @Column(name = "address_detail", nullable = false, length = 100)
    private String addressDetail;

    private LocalDate entryHanDate;
    private LocalDate entryBookDate;

    @Column(nullable = false, length = 20)
    private String appId;

    @Column(nullable = false)
    private Boolean studentPrivacyAgree = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_key", referencedColumnName = "grade_key", nullable = false)
    private GradeCode gradeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_key", referencedColumnName = "level_key", nullable = false)
    private LevelCode levelCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_key", referencedColumnName = "status_key", nullable = false)
    private StatusCode statusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    public Student(Integer id, String studentId, String studentName, String birth, Boolean gender, String school,
                   String address, String addressDetail, LocalDate entryHanDate, LocalDate entryBookDate, String appId,
                   Boolean studentPrivacyAgree, GradeCode gradeCode, LevelCode levelCode, StatusCode statusCode, Center center,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.birth = birth;
        this.gender = gender;
        this.school = school;
        this.address = address;
        this.addressDetail = addressDetail;
        this.entryHanDate = entryHanDate;
        this.entryBookDate = entryBookDate;
        this.appId = appId;
        this.studentPrivacyAgree = studentPrivacyAgree;
        this.gradeCode = gradeCode;
        this.levelCode = levelCode;
        this.statusCode = statusCode;
        this.center = center;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
