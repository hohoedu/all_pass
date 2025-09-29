package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.class_instance.TimeTable;
import com.hohoedu.all_pass.class_instance.model.base_data.AfterClass;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_after_class_notice", uniqueConstraints = {
        @UniqueConstraint(name = "uq_after_notice_key", columnNames = "after_notice_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AfterClassNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "after_notice_key", nullable = false)
    private String afterNoticeKey;

    @Column(name = "content", nullable = false, columnDefinition = "nvarchar(max)")
    private String content;

    @Column(name = "class_type", length = 20)
    private String classType;

    @Column(name = "year", length = 10)
    private String year;

    @Column(name = "month", length = 10)
    private String month;

    @Column(name = "week")
    private String week;

    @Column(name = "dayname", length = 10)
    private String dayname;

    @Column(name = "class_label")
    private String classLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "after_class_key", referencedColumnName = "after_class_key", nullable = false)
    private AfterClass afterClass;

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Builder
    public AfterClassNotice(String afterNoticeKey, String content, String classType, String year, String month, String week, String dayname, String classLabel, User user, Student student, AfterClass afterClass, TimeTable timeTable, Timestamp createdAt) {
        this.afterNoticeKey = afterNoticeKey;
        this.content = content;
        this.classType = classType;
        this.year = year;
        this.month = month;
        this.week = week;
        this.dayname = dayname;
        this.classLabel = classLabel;
        this.user = user;
        this.student = student;
        this.afterClass = afterClass;
        this.timeTable = timeTable;
        this.createdAt = createdAt;
    }
}

