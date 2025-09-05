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
@Table(name = "erp_status_code", uniqueConstraints = @UniqueConstraint(name = "uq_status_code_key", columnNames = "status_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StatusCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status_key", nullable = false, length = 20)
    private String statusKey;

    @Column(name = "status_name", nullable = false, length = 50)
    private String statusName;

    @Builder
    public StatusCode(String statusKey, String statusName) {
        this.statusKey = statusKey;
        this.statusName = statusName;
    }
}