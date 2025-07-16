package com.hohoedu.all_pass.class_instance.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.class_instance.code.ClassCode;
import com.hohoedu.all_pass.class_instance.code.UnitCode;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.user.model.User;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "time_table")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer timeTableNo;
    @Column
    private String yy;
    @Column
    private String mm;
    @Column
    private String dayname;
    @Column
    private String periodNo;
    @Column
    private String startTime;
    @Column
    private String endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_no")
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_no")
    private UnitCode unitCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_no")
    private GradeCode grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User user;

    @CreationTimestamp
    private Timestamp createdAt;
    
     @CreationTimestamp
    private Timestamp updatedAt;

    @Builder
    public TimeTable(Integer timeTableNo, String yy, String mm, String dayname, String periodNo, String startTime,
            String endTime, ClassCode classCode, UnitCode unitCode, GradeCode grade, User user, Timestamp createdAt, Timestamp updatedAt) {
        this.timeTableNo = timeTableNo;
        this.yy = yy;
        this.mm = mm;
        this.dayname = dayname;
        this.periodNo = periodNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.classCode = classCode;
        this.unitCode = unitCode;
        this.grade = grade;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
