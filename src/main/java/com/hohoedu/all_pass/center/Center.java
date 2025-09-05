package com.hohoedu.all_pass.center;

import java.time.LocalDate;

import com.hohoedu.all_pass.center.code.RegionCode;

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
@Table(name = "erp_center", uniqueConstraints = @UniqueConstraint(name = "uq_center_code", columnNames = "center_code"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Center {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "center_code", nullable = false, length = 20)
    private String centerCode;

    @Column(name = "center_name", nullable = false, length = 100)
    private String centerName;

    @Column(name = "opened_at", nullable = false)
    private LocalDate openedAt;

    @Column(name = "biz_no", nullable = false, length = 20)
    private String bizNo;

    @Column(name = "director_name", nullable = false, length = 50)
    private String directorName;

    @Column(name = "tel", nullable = false, length = 30)
    private String tel;

    @Column(name = "center_email", nullable = false, length = 100)
    private String centerEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "region_key", referencedColumnName = "region_key", nullable = false)
    private RegionCode region;

    @Builder
    public Center(String centerCode, String centerName, LocalDate openedAt, String bizNo, String directorName,
            String tel, String centerEmail, RegionCode region) {
        this.centerCode = centerCode;
        this.centerName = centerName;
        this.openedAt = openedAt;
        this.bizNo = bizNo;
        this.directorName = directorName;
        this.tel = tel;
        this.centerEmail = centerEmail;
        this.region = region;
    }
}