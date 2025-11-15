package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.center.Center;
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
@Table(name = "erp_infant_send_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InfantSendHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "send_id")
    private Integer id;

    @Column(name = "classType")
    private String classType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_table_key", referencedColumnName = "time_table_key")
    private TimeTable timeTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code")
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user", referencedColumnName = "user_code")
    private User senderUser;


    @Column(name = "created_at", updatable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Builder
    public InfantSendHistory(String classType, Student student, TimeTable timeTable, Center center, User senderUser, Timestamp createdAt, Timestamp updatedAt) {
        this.classType = classType;
        this.student = student;
        this.timeTable = timeTable;
        this.center = center;
        this.senderUser = senderUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
