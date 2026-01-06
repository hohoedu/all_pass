package com.hohoedu.all_pass.family.model;

import com.hohoedu.all_pass.student.Student;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "erp_sibling", uniqueConstraints = @UniqueConstraint(name = "uq_sm", columnNames = {"sibling_key", "student_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sibling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // PK

    // 그룹은 비즈니스키(sibling_code)로 참조

    @Column(name = "sibling_key")
    private String siblingKey;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", nullable = false)
    private Student student;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Sibling(String siblingKey, Student student) {
        this.siblingKey = siblingKey;
        this.student = student;
    }
}
