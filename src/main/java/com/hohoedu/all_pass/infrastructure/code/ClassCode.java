package com.hohoedu.all_pass.infrastructure.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "class_code")
@NoArgsConstructor
public class ClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer classCodeNo;

    @Column(nullable = false, length = 20)
    private String className;

    @Column(nullable = false, length = 20)
    private String classType;

    @Builder
    public ClassCode(Integer classCodeNo, String className, String classType) {
        this.classCodeNo = classCodeNo;
        this.className = className;
        this.classType = classType;
    }

}
