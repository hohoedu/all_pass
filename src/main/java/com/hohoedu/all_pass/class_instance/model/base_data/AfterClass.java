package com.hohoedu.all_pass.class_instance.model.base_data;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_after_class", uniqueConstraints = {@UniqueConstraint(name = "uq_after_class_key", columnNames = "after_class_key")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AfterClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "after_class_key", nullable = false)
    private String afterClassKey;

    @Column(name = "week", nullable = false, length = 20)
    private String week;

    @Column(name = "word", columnDefinition = "nvarchar(300)")
    private String word;

    @Column(name = "content", columnDefinition = "nvarchar(300)")
    private String content;

    @Column(name = "class_type")
    private String classType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_key", referencedColumnName = "unit_key", nullable = false)
    private UnitCode unitCode;

    @Builder
    public AfterClass(Integer id, String afterClassKey, String week, String word, String content, String classType, ClassCode classCode, UnitCode unitCode) {
        this.id = id;
        this.afterClassKey = afterClassKey;
        this.week = week;
        this.word = word;
        this.content = content;
        this.classType = classType;
        this.classCode = classCode;
        this.unitCode = unitCode;
    }
}
