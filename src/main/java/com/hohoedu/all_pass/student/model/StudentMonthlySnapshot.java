package com.hohoedu.all_pass.student.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

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
@Table(name = "student_monthly_snapshot")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentMonthlySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer snapshotNo;

    @Column(length = 7, nullable = false)
    private String snapshotYm;

    private Integer activeCount;
    private Integer restCount;
    private Integer withdrawnCount;
    private Integer waitCount;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public StudentMonthlySnapshot(Integer snapshotNo, String snapshotYm, Integer activeCount,
            Integer restCount, Integer withdrawnCount, Integer waitCount, Timestamp createdAt) {
        this.snapshotNo = snapshotNo;
        this.snapshotYm = snapshotYm;
        this.activeCount = activeCount;
        this.restCount = restCount;
        this.withdrawnCount = withdrawnCount;
        this.waitCount = waitCount;
        this.createdAt = createdAt;
    }

}
