package com.hohoedu.all_pass.student.model;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

//@Entity
//@Getter
//@Table(name = "erp_grade_code", uniqueConstraints = @UniqueConstraint(name = "uq_grade_code_key", columnNames = "grade_key"))
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
@Table(name = "ERP_TEACHER_ASSIGN")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeacherAssign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id")
    private Student student;

    @Column
    private Boolean hanState;

    @Column
    private Boolean bookState;

    @Column
    private String entryHanDate;

    @Column
    private String entryBookDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_han_teacher", referencedColumnName = "user_code")
    private User assignHanTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_han_class", referencedColumnName = "class_key")
    private ClassCode assignHanClass;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_book_teacher", referencedColumnName = "user_code")
    private User assignBookTeacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_book_class", referencedColumnName = "class_key")
    private ClassCode assignBookClass;


    @Builder
    public TeacherAssign(Student student, Boolean hanState, User assignHanTeacher, ClassCode assignHanClass, String entryHanDate, Boolean bookState, User assignBookTeacher, ClassCode assignBookClass, String entryBookDate) {
        this.student = student;
        this.hanState = hanState;
        this.assignHanTeacher = assignHanTeacher;
        this.assignHanClass = assignHanClass;
        this.entryHanDate = entryHanDate;
        this.bookState = bookState;
        this.assignBookTeacher = assignBookTeacher;
        this.assignBookClass = assignBookClass;
        this.entryBookDate = entryBookDate;
    }
}

