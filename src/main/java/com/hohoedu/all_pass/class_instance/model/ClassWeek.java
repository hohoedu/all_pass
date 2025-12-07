package com.hohoedu.all_pass.class_instance.model;

import com.hohoedu.all_pass.center.Center;
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

    @Column(name = "week")
    private String week;

    @Column(name = "mon")
    private String mon;

    @Column(name = "tue")
    private String tue;

    @Column(name = "wed")
    private String wed;

    @Column(name = "thu")
    private String thu;

    @Column(name = "fri")
    private String fri;

    @Column(name = "sat")
    private String sat;

    @Column(name = "sun")
    private String sun;

    @ManyToOne
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    @Builder
    public ClassWeek(String year, String month, String week, String mon, String tue, String wed, String thu, String fri, String sat, String sun, Center center) {
        this.year = year;
        this.month = month;
        this.week = week;
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.sat = sat;
        this.sun = sun;
        this.center = center;
    }
}
