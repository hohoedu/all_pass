package com.hohoedu.all_pass.class_instance.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_class_code", uniqueConstraints = {@UniqueConstraint(name = "uq_class_key", columnNames = "class_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "class_key", nullable = false, length = 20)
    private String classKey;

    @Column(name = "class_name", nullable = false, length = 50)
    private String className;

    @Column(name = "class_type", nullable = false, length = 1)
    private String classType;

    @Column(name = "unit_type", nullable = false, length = 20)
    private String unitType;


    @Builder
    public ClassCode(String classKey, String className, String classType, String unitType) {
        this.classKey = classKey;
        this.className = className;
        this.classType = classType;
        this.unitType = unitType;
    }
}


