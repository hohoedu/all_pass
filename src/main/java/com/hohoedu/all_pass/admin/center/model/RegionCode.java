package com.hohoedu.all_pass.admin.center.model;

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

@Getter
@Entity
@Table(name = "erp_region_code", uniqueConstraints = @UniqueConstraint(name = "uq_region_key", columnNames = "region_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "region_key", nullable = false, length = 20)
    private String regionKey;

    @Column(name = "region_name", nullable = false, length = 50)
    private String regionName;

    @Builder
    public RegionCode(String regionKey, String regionName) {
        this.regionKey = regionKey;
        this.regionName = regionName;
    }
}