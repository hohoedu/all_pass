package com.hohoedu.all_pass.class_instance.code;

import com.hohoedu.all_pass.center.model.Center;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "class_code")
@NoArgsConstructor
public class ClassCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer classNo;

    @Column(nullable = false, length = 20)
    private String className;

    @Column(nullable = false, length = 20)
    private String classType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_no")
    private Center center;

    @Builder
    public ClassCode(Integer classNo, String className, String classType, Center center) {
        this.classNo = classNo;
        this.className = className;
        this.classType = classType;
        this.center = center;
    }

}
