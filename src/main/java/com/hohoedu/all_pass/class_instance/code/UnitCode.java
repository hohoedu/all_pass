package com.hohoedu.all_pass.class_instance.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unit_code")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnitCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer unitNo;

    @Column(nullable = false, length = 20)
    private String unitCode;

    @Builder
    public UnitCode(Integer unitNo, String unitCode) {
        this.unitNo = unitNo;
        this.unitCode = unitCode;
    }

}
