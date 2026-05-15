package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.class_instance.TimeTable;
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
@Table(name = "erp_before_class_notice", uniqueConstraints = {
        @UniqueConstraint(name = "uq_before_class_notice_key", columnNames = "before_class_notice_key") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BeforeClassNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "content", nullable = false, columnDefinition = "nvarchar(max)")
    private String content;

    @Column(name = "class_time", nullable = false)
    private String classTime;

    @Column(name = "dayname", length = 20)
    private String dayname;

    @Column(name = "class_date")
    private String classDate;

    @Column(name = "class_type", length = 20)
    private String classType;

    @Column(name = "week")
    private String week;

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

    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Builder
    public BeforeClassNotice(String content, String classTime, String dayname, String classDate, String classType,
            String week, User user, Student student, TimeTable timeTable, Timestamp createdAt) {
        this.content = content;
        this.classTime = classTime;
        this.dayname = dayname;
        this.classDate = classDate;
        this.classType = classType;
        this.week = week;
        this.user = user;
        this.student = student;
        this.timeTable = timeTable;
        this.createdAt = createdAt;
    }
}
