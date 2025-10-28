package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.class_instance.TimeTable;
import com.hohoedu.all_pass.student.Student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_time_table_assign", uniqueConstraints = {@UniqueConstraint(name = "uq_assign_student_time", columnNames = {"student_id", "time_table_key"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTableAssign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "week", nullable = false, length = 10)
    private String week;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key")
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key")
    private UnitCode unitCode;

    @Builder
    public TimeTableAssign(String week, Student student, TimeTable timeTable, ClassCode classCode, UnitCode unitCode) {
        this.week = week;
        this.student = student;
        this.timeTable = timeTable;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }


}
