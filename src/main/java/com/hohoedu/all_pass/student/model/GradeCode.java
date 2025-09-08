package com.hohoedu.all_pass.student.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_grade_code", uniqueConstraints = @UniqueConstraint(name = "uq_grade_code_key", columnNames = "grade_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "grade_key", nullable = false, length = 20)
    private String gradeKey;

    @Column(name = "grade_name", nullable = false, length = 50)
    private String gradeName;

    @Builder
    public GradeCode(String gradeKey, String gradeName) {
        this.gradeKey = gradeKey;
        this.gradeName = gradeName;
    }
}
