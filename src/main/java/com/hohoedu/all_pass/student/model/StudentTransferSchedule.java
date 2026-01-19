package com.hohoedu.all_pass.student.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_student_transfer_schedule")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentTransferSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String studentId;

    private String classType;

    private String fromUser;
    private String toUser;

    private String moveAt;
    private String status;

    private String reason;

    private String createdBy;

    private Timestamp createdAt;
    private Timestamp appliedAt;

    @Builder
    public StudentTransferSchedule(String studentId, String classType, String fromUser, String toUser, String moveAt, String status, String reason, String createdBy, Timestamp createdAt, Timestamp appliedAt) {
        this.studentId = studentId;
        this.classType = classType;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.moveAt = moveAt;
        this.status = status;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.appliedAt = appliedAt;
    }
}
