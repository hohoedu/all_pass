package com.hohoedu.all_pass.student.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;


@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StudentSnapshotId implements Serializable {
    @Column(name = "snapshot_ym", length = 6, nullable = false)
    private String snapshotYm;

    @Column(name = "center_code", length = 20, nullable = false)
    private String centerCode;
}