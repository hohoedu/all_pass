package com.hohoedu.all_pass.payment.model;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_class_fee_map")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassFeeMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column (nullable = false, length = 20)
    private Integer fee;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "class_key", nullable = false, referencedColumnName = "class_key")
    private ClassCode classCode;

    @ManyToOne
    @JoinColumn(name = "center_code", nullable = false, referencedColumnName = "center_code")
    private Center center;

    @Builder
    public ClassFeeMap(Integer fee, ClassCode classCode, Center center) {
        this.fee = fee;
        this.classCode = classCode;
        this.center = center;
    }
}
