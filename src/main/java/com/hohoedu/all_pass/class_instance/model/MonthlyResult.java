package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.class_instance.TimeTable;
import com.hohoedu.all_pass.student.Student;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_monthly_result")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "top_comment", columnDefinition = "NVARCHAR(500)")
    private String topComment; // class_contents

    @Column(name = "bottom_comment", columnDefinition = "NVARCHAR(500)")
    private String bottomComment; // review

    @Column(name = "feedback", columnDefinition = "NVARCHAR(500)")
    private String feedback; // partnote

    @Column(name = "is_send")
    private boolean isSend;

    @Column(name = "yy")
    private String yy;

    @Column(name = "mm")
    private String mm;

    @Column(name = "send_at")
    private Timestamp sendAt;

    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key", nullable = false)
    private TimeTable timeTable;

    @Builder
    public MonthlyResult(Integer id, String topComment, String bottomComment, String feedback, boolean isSend, String yy, String mm, Student student, TimeTable timeTable) {
        this.id = id;
        this.topComment = topComment;
        this.bottomComment = bottomComment;
        this.feedback = feedback;
        this.isSend = isSend;
        this.yy = yy;
        this.mm = mm;
        this.student = student;
        this.timeTable = timeTable;
    }
}
