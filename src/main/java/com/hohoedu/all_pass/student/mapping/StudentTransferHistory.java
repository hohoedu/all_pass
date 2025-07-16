package com.hohoedu.all_pass.student.mapping;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.class_instance.code.ClassCode;
import com.hohoedu.all_pass.student.model.Student;
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
@Getter
@Table(name = "student_transfer_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentTransferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transferNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_no")
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_no")
    private User toUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_no")
    private ClassCode classCode;

    @Column(nullable = false, length = 20)
    private String transferReason;

    @Column(nullable = false)
    private String moveAt;

    @Column(nullable = false)
    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public StudentTransferHistory(Integer transferNo, Student student, User fromUser, User toUser, ClassCode classCode,
            String transferReason, String moveAt, Timestamp createdAt) {
        this.transferNo = transferNo;
        this.student = student;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.classCode = classCode;
        this.transferReason = transferReason;
        this.moveAt = moveAt;
        this.createdAt = createdAt;
    }

}
