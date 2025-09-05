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
@Table(name = "erp_level_code", uniqueConstraints = @UniqueConstraint(name = "uq_level_code_key", columnNames = "level_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LevelCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "level_key", nullable = false, length = 20)
    private String levelKey;

    @Column(name = "level_name", nullable = false, length = 50)
    private String levelName;

    @Builder
    public LevelCode(String levelKey, String levelName) {
        this.levelKey = levelKey;
        this.levelName = levelName;
    }
}
