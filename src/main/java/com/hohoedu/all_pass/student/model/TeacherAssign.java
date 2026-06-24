package com.hohoedu.all_pass.student.model;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.*;


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

    @Column
    private Integer hanMaterialFee;

    @Column
    private Integer bookMaterialFee;

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

    @Column(name = "pending_han_teacher")
    private String pendingHanTeacher;

    @Column(name = "pending_book_teacher")
    private String pendingBookTeacher;

    @Builder
    public TeacherAssign(Student student, Boolean hanState, User assignHanTeacher, ClassCode assignHanClass, String entryHanDate, Integer hanMaterialFee, Boolean bookState, User assignBookTeacher, ClassCode assignBookClass, String entryBookDate, Integer bookMaterialFee) {
        this.student = student;
        this.hanState = hanState;
        this.assignHanTeacher = assignHanTeacher;
        this.assignHanClass = assignHanClass;
        this.entryHanDate = entryHanDate;
        this.hanMaterialFee = hanMaterialFee;
        this.bookState = bookState;
        this.assignBookTeacher = assignBookTeacher;
        this.assignBookClass = assignBookClass;
        this.bookMaterialFee = bookMaterialFee;
        this.entryBookDate = entryBookDate;
    }

    public void updateHanAssign(User teacher, ClassCode classCode, String entryDate) {
        this.hanState = true;
        this.assignHanTeacher = teacher;
        this.assignHanClass = classCode;
        this.entryHanDate = entryDate;
    }

    public void updateBookAssign(User teacher, ClassCode classCode, String entryDate) {
        this.bookState = true;
        this.assignBookTeacher = teacher;
        this.assignBookClass = classCode;
        this.entryBookDate = entryDate;
    }
}

