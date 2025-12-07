package com.hohoedu.all_pass.admin.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_subject_code", uniqueConstraints = @UniqueConstraint(name = "uq_subject_code_key", columnNames = "subject_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubjectCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "subject_key", nullable = false, unique = true)
    private String subjectKey;

    @Column(name = "subject_name", nullable = false)
    private String subjectName;

    @Builder
    public SubjectCode(String subjectKey, String subjectName) {
        this.subjectKey = subjectKey;
        this.subjectName = subjectName;
    }
}
