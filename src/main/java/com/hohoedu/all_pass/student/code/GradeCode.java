package com.hohoedu.all_pass.student.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "grade_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GradeCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer gradeNo;

    @Column(nullable = false, length = 20)
    private String grade;

    @Builder
    public GradeCode(Integer gradeNo, String grade) {
        this.gradeNo = gradeNo;
        this.grade = grade;

    }

    

}
