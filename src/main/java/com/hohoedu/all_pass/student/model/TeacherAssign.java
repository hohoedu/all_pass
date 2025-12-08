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

    @Column(name = "han_material_fee")
    private Integer hanMaterialFee;

    @Column(name = "book_material_fee")
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


    @Builder
    public TeacherAssign(Student student, Boolean hanState, Integer hanMaterialFee, User assignHanTeacher, ClassCode assignHanClass, String entryHanDate, Boolean bookState, Integer bookMaterialFee, User assignBookTeacher, ClassCode assignBookClass, String entryBookDate) {
        this.student = student;
        this.hanState = hanState;
        this.hanMaterialFee = hanMaterialFee;
        this.assignHanTeacher = assignHanTeacher;
        this.assignHanClass = assignHanClass;
        this.entryHanDate = entryHanDate;
        this.bookState = bookState;
        this.bookMaterialFee = bookMaterialFee;
        this.assignBookTeacher = assignBookTeacher;
        this.assignBookClass = assignBookClass;
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

