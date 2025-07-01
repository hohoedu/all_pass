package com.hohoedu.all_pass.student.mapping;

import com.hohoedu.all_pass.infrastructure.code.ClassCode;
import com.hohoedu.all_pass.student.model.Student;

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
@Table(name = "student_class")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer studentClassNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_no")
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_code_no")
    private ClassCode classCode;

    @Builder
    public StudentClass(Integer studentClassNo, Student student, ClassCode classCode) {
        this.studentClassNo = studentClassNo;
        this.student = student;
        this.classCode = classCode;
    }

}
