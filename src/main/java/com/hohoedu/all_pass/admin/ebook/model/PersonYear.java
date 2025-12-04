package com.hohoedu.all_pass.admin.ebook.model;

import com.hohoedu.all_pass.admin.center.Center;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_person_year")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PersonYear {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String yy;

    private String mm;

    @Column(name = "unit_key")
    private String unitKey;

    @Column(name = "sub_unit_key")
    private String subUnitKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_key", referencedColumnName = "class_key", nullable = false)
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @Builder
    public PersonYear(String yy, String mm, String unitKey, String subUnitKey, ClassCode classCode, Center center) {
        this.yy = yy;
        this.mm = mm;
        this.unitKey = unitKey;
        this.subUnitKey = subUnitKey;
        this.classCode = classCode;
        this.center = center;
    }
}
