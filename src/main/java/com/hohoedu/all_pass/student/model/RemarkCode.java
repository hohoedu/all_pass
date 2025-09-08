package com.hohoedu.all_pass.student.model;

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

@Getter
@Entity
@Table(name = "erp_remark_code", uniqueConstraints = @UniqueConstraint(name = "uq_remark_code_key", columnNames = "remark_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RemarkCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "remark_key", nullable = false, length = 20)
    private String remarkKey;

    @Column(name = "remark_name", nullable = false, length = 50)
    private String remarkName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remark_category_key", referencedColumnName = "remark_category_key", nullable = false)
    private RemarkCategory remarkCategory;

    @Builder
    public RemarkCode(String remarkKey, String remarkName, RemarkCategory remarkCategory) {
        this.remarkKey = remarkKey;
        this.remarkName = remarkName;
        this.remarkCategory = remarkCategory;
    }
}