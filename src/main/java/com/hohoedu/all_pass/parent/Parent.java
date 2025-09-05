package com.hohoedu.all_pass.parent;

import com.hohoedu.all_pass.parent.code.RelationCode;
import com.hohoedu.all_pass.student.Student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Table(name = "parent")
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parentNo;

    @Column(nullable = false, length = 20)
    private String parentName;

    @Column(nullable = false, length = 20)
    private String parentTelFirst;

    @Column(nullable = false, length = 20)
    private String parentTelMiddle;

    @Column(nullable = false, length = 20)
    private String parentTelLast;

    private boolean parentPrivacyAgree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_no")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_no")
    private RelationCode relationCode;

    @Builder
    public Parent(Integer parentNo, String parentName, String parentTelFirst, String parentTelMiddle,
            String parentTelLast, boolean parentPrivacyAgree, Student student, RelationCode relationCode) {
        this.parentNo = parentNo;
        this.parentName = parentName;
        this.parentTelFirst = parentTelFirst;
        this.parentTelMiddle = parentTelMiddle;
        this.parentTelLast = parentTelLast;
        this.parentPrivacyAgree = parentPrivacyAgree;
        this.student = student;
        this.relationCode = relationCode;
    }

}
