package com.hohoedu.all_pass.family;

import com.hohoedu.all_pass.family.model.RelationCode;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_parent")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "parent_name", nullable = false, length = 20)
    private String parentName;

    @Column(name = "parent_tel_first", nullable = false, length = 10)
    private String parentTelFirst;

    @Column(name = "parent_tel_middle", nullable = false, length = 10)
    private String parentTelMiddle;

    @Column(name = "parent_tel_last", nullable = false, length = 10)
    private String parentTelLast;

    @Column(name = "parent_privacy_agree", nullable = false)
    private boolean parentPrivacyAgree;

    @Column(name="signature")
    private String signature;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relation_key", referencedColumnName = "relation_key", nullable = false)
    private RelationCode relationCode;

    @Builder
    public Parent(Integer id, String parentName, String parentTelFirst, String parentTelMiddle, String parentTelLast, boolean parentPrivacyAgree, String signature, Student student, RelationCode relationCode) {
        this.id = id;
        this.parentName = parentName;
        this.parentTelFirst = parentTelFirst;
        this.parentTelMiddle = parentTelMiddle;
        this.parentTelLast = parentTelLast;
        this.parentPrivacyAgree = parentPrivacyAgree;
        this.signature = signature;
        this.student = student;
        this.relationCode = relationCode;
    }
}
