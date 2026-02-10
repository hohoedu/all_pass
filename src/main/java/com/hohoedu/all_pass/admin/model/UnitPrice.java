package com.hohoedu.all_pass.admin.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_unit_price", uniqueConstraints = {@UniqueConstraint(name = "UX_erp_unit_price", columnNames = {"center_code", "class_key", "start_date"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UnitPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "center_code", nullable = false, length = 20)
    private String centerCode;

    @Column(name = "class_key", nullable = false)
    private String classKey;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private Integer unitPrice;

    @Column(name = "start_date", nullable = false)
    private String startDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Builder
    public UnitPrice(String centerCode, String classKey, Integer unitPrice, String startDate, Boolean isActive) {
        this.centerCode = centerCode;
        this.classKey = classKey;
        this.unitPrice = unitPrice;
        this.startDate = startDate;
        this.isActive = isActive;
    }
}
