package com.hohoedu.all_pass.student;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.student.model.GradeCode;
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

    @Column(name = "app_id", nullable = false, length = 20, unique = true)
    private String appId;

    @Column(name = "app_password", length = 100)
    private String appPassword;

    @Column(name = "app_token", length = 255)
    private String appToken;

    @Column(name = "profile_img")
    private String profileImg;

    @Column(nullable = false)
    private Boolean studentPrivacyAgree = Boolean.FALSE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_key", referencedColumnName = "grade_key", nullable = false)
    private GradeCode gradeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_key", referencedColumnName = "status_key")
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
    public Student(String studentId, String studentName, String birth, Boolean gender, String school,
                   String address, String addressDetail, String appId, String appPassword, String appToken, String profileImg, Boolean studentPrivacyAgree, GradeCode gradeCode, StatusCode statusCode, Center center,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.birth = birth;
        this.gender = gender;
        this.school = school;
        this.address = address;
        this.addressDetail = addressDetail;
        this.appId = appId;
        this.appPassword = appPassword;
        this.appToken = appToken;
        this.profileImg = profileImg;
        this.studentPrivacyAgree = studentPrivacyAgree;
        this.gradeCode = gradeCode;
        this.statusCode = statusCode;
        this.center = center;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
