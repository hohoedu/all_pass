package com.hohoedu.all_pass.class_instance.model.base_data;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.C;

@Entity
@Getter
@Table(name = "erp_monthly_comment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonthlyComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "yy")
    private String yy;

    @Column(name = "top_comment", columnDefinition = "nvarchar(300)")
    private String topComment;

    @Column(name = "bottom_comment", columnDefinition = "nvarchar(300)")
    private String bottomComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @Builder
    public MonthlyComment( String yy, String topComment, String bottomComment, ClassCode classCode, UnitCode unitCode) {
        this.yy = yy;
        this.topComment = topComment;
        this.bottomComment = bottomComment;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }
}
