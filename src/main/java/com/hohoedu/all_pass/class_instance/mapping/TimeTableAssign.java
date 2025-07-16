package com.hohoedu.all_pass.class_instance.mapping;

import com.hohoedu.all_pass.class_instance.model.TimeTable;
import com.hohoedu.all_pass.student.model.Student;

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

@Entity
@Table(name = "time_table_assign", uniqueConstraints = {
        @UniqueConstraint(name = "uk_time_table_assign_student_time", columnNames = { "student_no", "time_table_no" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTableAssign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Integer timeTableAssignNo;

    @Column
    private String week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_no")
    private TimeTable timeTable;

    @Builder
    public TimeTableAssign(Integer timeTableAssignNo, String week, Student student) {
        this.timeTableAssignNo = timeTableAssignNo;
        this.week = week;
        this.student = student;
    }

}
