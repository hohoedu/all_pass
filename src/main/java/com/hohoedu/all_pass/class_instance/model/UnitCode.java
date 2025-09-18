package com.hohoedu.all_pass.class_instance.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_unit_code", uniqueConstraints = {@UniqueConstraint(name = "uq_unit_key", columnNames = "unit_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnitCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "unit_key", nullable = false, length = 20)
    private String unitKey;

    @Column(name = "unit_name", nullable = false, length = 50)
    private String unitName;

    @Column(name = "unit_type", nullable = false, length = 20)
    private String unitType;

    @Builder
    public UnitCode( String unitKey, String unitName, String unitType) {
        this.unitKey = unitKey;
        this.unitName = unitName;
        this.unitType = unitType;
    }
}
