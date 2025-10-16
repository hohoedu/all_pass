package com.hohoedu.all_pass.student.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_status_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reason", length = 200)
    private String reason;

    @CreationTimestamp
    @Column(name = "updated_at", nullable = false, updatable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "status_key", referencedColumnName = "status_key", nullable = false)
    private StatusCode statusCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @Builder
    public StatusHistory(String reason, Student student, StatusCode statusCode, User user) {
        this.reason = reason;
        this.student = student;
        this.statusCode = statusCode;
        this.user = user;
    }

}