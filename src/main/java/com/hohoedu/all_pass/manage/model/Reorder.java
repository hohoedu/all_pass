package com.hohoedu.all_pass.manage.model;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Getter
@Table(name = "erp_reorder")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reorder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reorder_type", nullable = false)
    private String reorderType;

    @Column(name = "class_key", nullable = false)
    private String classKey;

    @Column(name = "unit_key", nullable = false)
    private String unitKey;

    @Column(name = "count", nullable = false)
    private Integer count;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Column(name = "yy", nullable = false)
    private String yy;

    @Column(name = "mm", nullable = false)
    private String mm;

    @Column(name = "confirmed")
    private String confirmed; //(Y/N)

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @CreationTimestamp
    private Timestamp confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_code", referencedColumnName = "user_code", nullable = false)
    private User user;

    @Builder
    public Reorder(String reorderType, String classKey, String unitKey, Integer count, String reason, String yy, String mm, String confirmed, Center center, User user) {
        this.reorderType = reorderType;
        this.classKey = classKey;
        this.unitKey = unitKey;
        this.count = count;
        this.reason = reason;
        this.yy = yy;
        this.mm = mm;
        this.confirmed = confirmed;
        this.center = center;
        this.user = user;
    }
}
