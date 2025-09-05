package com.hohoedu.all_pass.student.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;

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
@Getter
@Table(name = "erp_student_transfer_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentTransferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_user_code", referencedColumnName = "user_code", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_user_code", referencedColumnName = "user_code", nullable = false)
    private User toUser;

    @Column(name = "class_type", nullable = false, length = 10)
    private String classType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key")
    private ClassCode classCode;

    @Column(name = "transfer_reason", columnDefinition = "NVARCHAR(200)", nullable = false, length = 200)
    private String transferReason;

    @Column(name = "move_at", nullable = false, length = 10)
    private String moveAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @Builder
    public StudentTransferHistory(Student student, User fromUser, User toUser, String classType, ClassCode classCode, String transferReason, String moveAt, Timestamp createdAt) {
        this.student = student;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.classType = classType;
        this.classCode = classCode;
        this.transferReason = transferReason;
        this.moveAt = moveAt;
        this.createdAt = createdAt;
    }
}