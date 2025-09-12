package com.hohoedu.all_pass.class_instance.model;

import java.sql.Timestamp;

import com.hohoedu.all_pass.class_instance.TimeTable;
import lombok.AccessLevel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.hohoedu.all_pass.center.Center;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_student_attendance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "in_time", length = 10)
    private String inTime;

    @Column(name = "out_time", length = 10)
    private String outTime;

    @Column(name = "week", length = 10)
    private String week;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_key", referencedColumnName = "attendance_key", nullable = false)
    private AttendanceCode attendanceCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @Builder
    public StudentAttendance(Integer id, String inTime, String outTime, String week, Timestamp createdAt, Timestamp updatedAt, Student student, TimeTable timeTable, AttendanceCode attendanceCode, Center center) {
        this.id = id;
        this.inTime = inTime;
        this.outTime = outTime;
        this.week = week;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.student = student;
        this.timeTable = timeTable;
        this.attendanceCode = attendanceCode;
        this.center = center;
    }
}