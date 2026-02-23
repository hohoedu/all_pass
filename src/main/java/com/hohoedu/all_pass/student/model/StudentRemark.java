package com.hohoedu.all_pass.student.model;

import com.hohoedu.all_pass.class_instance.TimeTable;
import com.hohoedu.all_pass.student.Student;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Getter
@Entity
@Table(name = "erp_student_remark", uniqueConstraints = {@UniqueConstraint(name = "uq_student_remark_one_row", columnNames = {"student_id", "yy", "mm", "week", "remark_key"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @Column(name = "yy", nullable = false, length = 10)
    private String yy;

    @Column(name = "mm", nullable = false, length = 10)
    private String mm;

    @Column(name = "week", nullable = false, length = 10)
    private String week;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remark_key", referencedColumnName = "remark_key", nullable = false)
    private RemarkCode remarkCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @Builder
    public StudentRemark(Student student, TimeTable timeTable, String yy, String mm, String week, RemarkCode remarkCode) {
        this.student = student;
        this.timeTable = timeTable;
        this.yy = yy;
        this.mm = mm;
        this.week = week;
        this.remarkCode = remarkCode;
    }
}