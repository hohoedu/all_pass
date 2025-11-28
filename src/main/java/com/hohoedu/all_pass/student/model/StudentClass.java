package com.hohoedu.all_pass.student.model;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Time;
import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_student_class")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false, referencedColumnName = "student_id")
    private Student student;

    @Column(name = "han_fee")
    private Integer hanFee;

    @Column
    private String hanStatus;

    @Column
    private String entryHanDate;

    @Column(name = "book_fee")
    private Integer bookFee;

    @Column
    private String bookStatus;

    @Column
    private String entryBookDate;

    @Column(name = "yy", nullable = false, length = 4)
    private String yy;

    @Column(name = "mm", nullable = false, length = 2)
    private String mm;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "han_user_code", referencedColumnName = "user_code")
    private User hanUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_user_code", referencedColumnName = "user_code")
    private User bookUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "han_class_key", referencedColumnName = "class_key")
    private ClassCode hanClassCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_class_key", referencedColumnName = "class_key")
    private ClassCode bookClassCode;

    @Builder
    public StudentClass(Student student, String hanStatus, String bookStatus, String entryHanDate, String entryBookDate, Integer hanFee, Integer hanMaterialFee, Integer bookFee, Integer bookMaterialFee, String yy, String mm, Timestamp createdAt, Timestamp updatedAt, User hanUser, User bookUser, ClassCode hanClassCode, ClassCode bookClassCode) {
        this.student = student;
        this.hanStatus = hanStatus;
        this.bookStatus = bookStatus;
        this.entryHanDate = entryHanDate;
        this.entryBookDate = entryBookDate;
        this.hanFee = hanFee;
        this.bookFee = bookFee;
        this.yy = yy;
        this.mm = mm;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.hanUser = hanUser;
        this.bookUser = bookUser;
        this.hanClassCode = hanClassCode;
        this.bookClassCode = bookClassCode;
    }
}
