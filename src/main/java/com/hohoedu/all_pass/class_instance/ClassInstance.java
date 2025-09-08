package com.hohoedu.all_pass.class_instance;

import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "class_instance")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClassInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer classInstanceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_no")
    private ClassCode classCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_no")
    private UnitCode unitCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_no")
    private User user;

    @Builder
    public ClassInstance(Integer classInstanceNo, ClassCode classCode, UnitCode unitCode, User user) {
        this.classInstanceNo = classInstanceNo;
        this.classCode = classCode;
        this.unitCode = unitCode;
        this.user = user;
    }

}
