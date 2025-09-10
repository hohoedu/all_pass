package com.hohoedu.all_pass.class_instance.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_before_class", uniqueConstraints = {
        @UniqueConstraint(name = "uq_before_class_key", columnNames = "before_class_key") })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BeforeClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "before_class_key")
    private String beforeClassKey;

    @Column(name = "ju_1")
    private String ju1;

    @Column(name = "ju_2")
    private String ju2;

    @Column(name = "ju_3")
    private String ju3;

    @Column(name = "ju_4")
    private String ju4;

    @Column(name = "class_type")
    private String classType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @Builder
    public BeforeClass(Integer id, String beforeClassKey, String ju1, String ju2, String ju3, String ju4,
            String classType, ClassCode classCode, UnitCode unitCode) {
        this.id = id;
        this.beforeClassKey = beforeClassKey;
        this.ju1 = ju1;
        this.ju2 = ju2;
        this.ju3 = ju3;
        this.ju4 = ju4;
        this.classType = classType;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }

}
