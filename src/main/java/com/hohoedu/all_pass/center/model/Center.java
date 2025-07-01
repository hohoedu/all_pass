package com.hohoedu.all_pass.center.model;

import com.hohoedu.all_pass.center.code.RegionCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "center_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Center {

    @Id
    private String centerNo;

    @Column(nullable = false, length = 20)
    private String centerName;

    @Column(nullable = false, length = 20)
    private String opendAt;

    @Column(nullable = false, length = 20)
    private String bizNo;

    @Column(nullable = false, length = 20)
    private String ceoName;

    @Column(nullable = false, length = 20)
    private String tel;

    @Column(nullable = false, length = 20)
    private String centerMail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_no")
    private RegionCode regionCode;

    @Builder
    public Center(String centerNo, String centerName, String opendAt, String bizNo, String ceoName, String tel,
            String centerMail) {
        this.centerNo = centerNo;
        this.centerName = centerName;
        this.opendAt = opendAt;
        this.bizNo = bizNo;
        this.ceoName = ceoName;
        this.tel = tel;
        this.centerMail = centerMail;
    }

}
