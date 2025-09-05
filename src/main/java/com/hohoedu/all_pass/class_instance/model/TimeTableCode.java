package com.hohoedu.all_pass.class_instance.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_time_table_code", uniqueConstraints = {@UniqueConstraint(name = "uq_time_table_key_ym", columnNames = {"time_table_key", "time_table_ym"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeTableCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "time_table_key", nullable = false, length = 50)
    private String timeTableKey;

    @Column(name = "time_table_label", nullable = false, length = 100)
    private String timeTableLabel;

    @Column(name = "time_table_ym", nullable = false, length = 10)
    private String timeTableYm;

    @Builder
    public TimeTableCode(String timeTableKey, String timeTableLabel, String timeTableYm) {
        this.timeTableKey = timeTableKey;
        this.timeTableLabel = timeTableLabel;
        this.timeTableYm = timeTableYm;
    }
}
