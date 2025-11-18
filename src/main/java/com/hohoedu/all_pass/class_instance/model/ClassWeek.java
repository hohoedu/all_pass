package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "erp_class_week")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "year")
    private String year;

    @Column(name = "month")
    private String month;

    @Column(name = "ju_1_start")
    private String ju1Start;
    @Column(name = "ju_1_end")
    private String ju1End;

    @Column(name = "ju_2_start")
    private String ju2Start;
    @Column(name = "ju_2_end")
    private String ju2End;

    @Column(name = "ju_3_start")
    private String ju3Start;
    @Column(name = "ju_3_end")
    private String ju3End;

    @Column(name = "ju_4_start")
    private String ju4Start;
    @Column(name = "ju_4_end")
    private String ju4End;

    @ManyToOne
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Builder
    public ClassWeek(String year, String month, String ju1Start, String ju1End, String ju2Start, String ju2End, String ju3Start, String ju3End, String ju4Start, String ju4End, Center center) {
        this.year = year;
        this.month = month;
        this.ju1Start = ju1Start;
        this.ju1End = ju1End;
        this.ju2Start = ju2Start;
        this.ju2End = ju2End;
        this.ju3Start = ju3Start;
        this.ju3End = ju3End;
        this.ju4Start = ju4Start;
        this.ju4End = ju4End;
        this.center = center;
    }
}
