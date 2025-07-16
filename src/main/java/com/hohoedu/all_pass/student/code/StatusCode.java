package com.hohoedu.all_pass.student.code;

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
@Table(name = "status_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer statusNo;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false, length = 20)
    private String code;

    @Builder
    public StatusCode(Integer statusNo, String status, String code) {
        this.statusNo = statusNo;
        this.status = status;
        this.code = code;
    }
}
