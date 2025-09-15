package com.hohoedu.all_pass.student.model;


import com.hohoedu.all_pass.center.Center;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "erp_student_snapshot", uniqueConstraints = @UniqueConstraint(name = "uk_snapshot_ym_center", columnNames = {"snapshot_ym","center_code"}),
        indexes = {@Index(name="ix_snapshot_ym", columnList="snapshot_ym"), @Index(name="ix_snapshot_center", columnList="center_code")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudentSnapshot {

    @EmbeddedId
    private StudentSnapshotId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", insertable = false, updatable = false)
    private Center center;

    @Column(name = "total_count",     nullable = false)
    private Integer totalCount;
    @Column(name = "active_count",    nullable = false)
    private Integer activeCount;
    @Column(name = "rest_count",      nullable = false)
    private Integer restCount;
    @Column(name = "withdrawn_count", nullable = false)
    private Integer withdrawnCount;
    @Column(name = "wait_count",      nullable = false)
    private Integer waitCount;

    @Column(name = "created_at", columnDefinition = "datetime2(0)", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getSnapshotYm() { return id != null ? id.getSnapshotYm() : null; }
    public String getCenterCode() { return id != null ? id.getCenterCode() : null; }

    @Builder
    public StudentSnapshot(StudentSnapshotId id, Center center, Integer totalCount, Integer activeCount, Integer restCount, Integer withdrawnCount, Integer waitCount) {
        this.id = id;
        this.center = center;
        this.totalCount = nz(totalCount);
        this.activeCount = nz(activeCount);
        this.restCount = nz(restCount);
        this.withdrawnCount = nz(withdrawnCount);
        this.waitCount = nz(waitCount);
    }
    private int nz(Integer v) { return v == null ? 0 : v; }
}
