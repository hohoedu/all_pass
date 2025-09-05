package com.hohoedu.all_pass.student.model;

import com.hohoedu.all_pass.student.Student;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_student_remark", uniqueConstraints = {@UniqueConstraint(name = "uq_student_remark_one_row", columnNames = {"student_id", "ym", "week", "remark_key"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ym", nullable = false, length = 10)
    private String ym;

    @Column(name = "week", nullable = false, length = 10)
    private String week;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remark_key", referencedColumnName = "remark_key", nullable = false)
    private RemarkCode remarkCode;

    @Builder
    public StudentRemark(String ym, String week, Student student, RemarkCode remarkCode) {
        this.ym = ym;
        this.week = week;
        this.student = student;
        this.remarkCode = remarkCode;
    }
}