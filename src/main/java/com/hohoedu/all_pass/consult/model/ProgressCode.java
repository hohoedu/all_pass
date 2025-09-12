package com.hohoedu.all_pass.consult.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_progress_code", uniqueConstraints = @UniqueConstraint(name = "uq_progress_key", columnNames = "progress_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgressCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "progress_key", nullable = false, length = 20)
    private String progressKey;

    @Column(name = "progress_name", nullable = false, length = 20)
    private String progressName;

    @Builder
    public ProgressCode(String progressKey, String progressName) {
        this.progressKey = progressKey;
        this.progressName = progressName;
    }
}
