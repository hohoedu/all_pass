package com.hohoedu.all_pass.consult.model;

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
@Table(name = "progress_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProgressCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressNo;

    @Column(nullable = false, length = 10)
    private String progress;

    @Builder
    public ProgressCode(Integer progressNo, String progress) {
        this.progressNo = progressNo;
        this.progress = progress;
    }

}
