package com.hohoedu.all_pass.sibling.model;

import com.hohoedu.all_pass.center.model.Center;
import com.hohoedu.all_pass.student.model.Student;

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
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "sibling")
public class Sibling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer siblingNo;

    @Column(nullable = false)
    private String siblingCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no")
    private Student student;

    @Builder
    public Sibling(Integer siblingNo, String siblingCode, Student student) {
        this.siblingNo = siblingNo;
        this.siblingCode = siblingCode;
        this.student = student;
    }

}
