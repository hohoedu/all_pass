package com.hohoedu.all_pass.class_instance.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_class_unit_map")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassUnitMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key")
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key")
    private UnitCode unitCode;

    @Builder
    public ClassUnitMap(ClassCode classCode, UnitCode unitCode) {
        this.classCode = classCode;
        this.unitCode = unitCode;
    }
}

