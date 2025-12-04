package com.hohoedu.all_pass.manage.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_order")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "class_key", nullable = false)
    private String classKey;

    @Column(name = "unit_key", nullable = false)
    private String unitKey;

    @Column(name = "center_code", nullable = false, length = 20)
    private String centerCode;

    @Column(name = "user_code", nullable = false, length = 50)
    private String userCode;

    @Column(name = "base_count", nullable = false)
    private Integer baseCount;

    @Column(name = "add_count")
    private Integer addCount;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "yy", nullable = false, length = 6)
    private String yy;

    @Column(name = "mm", nullable = false, length = 6)
    private String mm;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Builder
    public Order(String classKey, String unitKey, String centerCode, String userCode, Integer baseCount, Integer addCount, Integer totalCount, String yy, String mm, Timestamp createdAt, Timestamp updatedAt) {
        this.classKey = classKey;
        this.unitKey = unitKey;
        this.centerCode = centerCode;
        this.userCode = userCode;
        this.baseCount = baseCount;
        this.addCount = addCount;
        this.totalCount = totalCount;
        this.yy = yy;
        this.mm = mm;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
