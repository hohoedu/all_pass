package com.hohoedu.all_pass.student.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_remark_category", uniqueConstraints = @UniqueConstraint(name = "uq_remark_category_key", columnNames = "remark_category_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RemarkCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "remark_category_key", nullable = false, length = 20)
    private String remarkCategoryKey;

    @Column(name = "remark_category_name", nullable = false, length = 50)
    private String remarkCategoryName;

    @Builder
    public RemarkCategory(String remarkCategoryKey, String remarkCategoryName) {
        this.remarkCategoryKey = remarkCategoryKey;
        this.remarkCategoryName = remarkCategoryName;
    }
}
