package com.hohoedu.all_pass.class_instance.model;

import java.security.Timestamp;

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
@Table(name = "counseling")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counseling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer counselingNo;

    @Column
    private String content;

    private String week;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no")
    private Student student;

    @Builder
    public Counseling(Integer counselingNo, String content, User user, Student student) {
        this.counselingNo = counselingNo;
        this.content = content;
        this.user = user;
        this.student = student;
    }

}
