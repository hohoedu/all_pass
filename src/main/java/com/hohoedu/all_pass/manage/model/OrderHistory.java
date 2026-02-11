package com.hohoedu.all_pass.manage.model;

import com.hohoedu.all_pass.class_instance.TimeTable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_order_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "class_key")
    private String classKey;

    @Column(name = "unit_key")
    private String unitKey;

    @Column(name = "center_code")
    private String centerCode;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "count")
    private Integer count;

    @Column(name = "yy")
    private String yy;

    @Column(name = "mm")
    private String mm;

    @CreationTimestamp
    @Column(name = "created_at")
    private Timestamp createdAt;

    @Builder
    public OrderHistory(String classKey, String unitKey, String centerCode, String userCode, Integer count, String yy, String mm) {
        this.classKey = classKey;
        this.unitKey = unitKey;
        this.centerCode = centerCode;
        this.userCode = userCode;
        this.count = count;
        this.yy = yy;
        this.mm = mm;
    }
}
