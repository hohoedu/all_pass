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
@Table(name = "level_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LevelCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer levelNo;

    @Column(nullable = false, length = 20)
    private String level;

    @Builder
    public LevelCode(Integer levelNo, String level) {
        this.levelNo = levelNo;
        this.level = level;
    }

}
