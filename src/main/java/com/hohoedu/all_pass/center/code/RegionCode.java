package com.hohoedu.all_pass.center.code;

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
@Getter
@Table(name = "region_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RegionCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer regionNo;

    @Column(nullable = false, length = 10)
    private String region;

    @Builder
    public RegionCode(Integer regionNo, String region) {
        this.regionNo = regionNo;
        this.region = region;
    }

}
