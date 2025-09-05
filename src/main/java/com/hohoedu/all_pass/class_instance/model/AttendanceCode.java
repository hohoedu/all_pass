package com.hohoedu.all_pass.class_instance.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_attendance_code", uniqueConstraints = {@UniqueConstraint(name = "uq_attendance_key", columnNames = "attendance_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttendanceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "attendance_key", nullable = false, length = 10)
    private String attendanceKey;

    @Column(name = "attendance_name", nullable = false, length = 50)
    private String attendanceName;

    @Builder
    public AttendanceCode(String attendanceKey, String attendanceName) {
        this.attendanceKey = attendanceKey;
        this.attendanceName = attendanceName;
    }
}