package com.hohoedu.all_pass.student.mapping;

import com.hohoedu.all_pass.class_instance.model.ClassInstance;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_instance_no")
    private ClassInstance classInstance;

    @Builder
    public StudentClass(Integer studentClassNo, Student student, ClassInstance classInstance) {
        this.studentClassNo = studentClassNo;
        this.student = student;
        this.classInstance = classInstance;
    }

}
