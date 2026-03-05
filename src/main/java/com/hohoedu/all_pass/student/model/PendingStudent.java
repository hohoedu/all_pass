package com.hohoedu.all_pass.student.model;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "erp_pending_student")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PendingStudent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 10)
    private String name;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "grade_key", length = 20, columnDefinition = "varchar(20)")
    private String gradeKey;

    @Column(name = "send_key", length = 100, nullable = false, unique = true)
    private String sendKey;


    @Column(name = "status", nullable = false, length = 20)
    private String status;
    @Column(name = "user_code")
    private String userCode;

    @Column(name = "center_code")
    private String centerCode;

    @Column(name = "sub_hoho")
    private Boolean subHoho;

    @Column(name = "sub_han")
    private Boolean subHan;

    @Column(name = "sub_book")
    private Boolean subBook;

    @Column(name = "invite_sent_at", nullable = false, updatable = false,
            columnDefinition = "datetime2(7)")
    private Timestamp inviteSentAt;

    @Column(name = "registered_at", columnDefinition = "datetime2(7)")
    private Timestamp registeredAt;

    @Column(name = "assigned_at", columnDefinition = "datetime2(7)")
    private Timestamp assignedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at", columnDefinition = "datetime2(7)")
    private Timestamp deletedAt;

    @Column(name = "student_id")
    private String studentId;


    @Builder
    public PendingStudent(String name, String phone, String gradeKey, String sendKey, String status, String userCode, String centerCode, Boolean subHoho, Boolean subHan, Boolean subBook, Timestamp inviteSentAt, Timestamp registeredAt, Timestamp assignedAt, boolean isDeleted, Timestamp deletedAt, String studentId) {
        this.name = name;
        this.phone = phone;
        this.gradeKey = gradeKey;
        this.sendKey = sendKey;
        this.status = status;
        this.userCode = userCode;
        this.centerCode = centerCode;
        this.subHoho = subHoho;
        this.subHan = subHan;
        this.subBook = subBook;
        this.inviteSentAt = inviteSentAt;
        this.registeredAt = registeredAt;
        this.assignedAt = assignedAt;
        this.isDeleted = isDeleted;
        this.deletedAt = deletedAt;
        this.studentId = studentId;
    }
}