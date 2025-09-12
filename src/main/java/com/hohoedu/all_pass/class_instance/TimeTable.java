package com.hohoedu.all_pass.class_instance;

import java.sql.Timestamp;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_time_table", uniqueConstraints = {@UniqueConstraint(name = "uq_time_table_key", columnNames = "time_table_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "yy", nullable = false, length = 10)
    private String yy;

    @Column(name = "mm", nullable = false, length = 10)
    private String mm;

    @Column(name = "dayname", nullable = false, length = 10)
    private String dayname;

    @Column(name = "period_no", nullable = false)
    private String periodNo;

    @Column(name = "start_time", nullable = false, length = 5)
    private String startTime;

    @Column(name = "end_time", nullable = false, length = 5)
    private String endTime;

    @Column(name = "time_table_key", nullable = false, length = 50)
    private String timeTableKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_key", referencedColumnName = "grade_key", nullable = false)
    private GradeCode grade;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @CreationTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Builder
    public TimeTable(String yy, String mm, String dayname, String periodNo,
                     String startTime, String endTime, String timeTableKey,
                     ClassCode classCode, UnitCode unitCode, GradeCode grade, User user) {
        this.yy = yy;
        this.mm = mm;
        this.dayname = dayname;
        this.periodNo = periodNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeTableKey = timeTableKey;
        this.classCode = classCode;
        this.unitCode = unitCode;
        this.grade = grade;
        this.user = user;
    }
}
