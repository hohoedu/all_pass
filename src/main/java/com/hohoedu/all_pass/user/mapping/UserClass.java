package com.hohoedu.all_pass.user.mapping;

import com.hohoedu.all_pass.class_instance.model.ClassInstance;
import com.hohoedu.all_pass.user.model.User;

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
@Table(name = "user_class")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userClassNo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_no")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_instance_no")
    private ClassInstance classInstance;

    @Builder
    public UserClass(Integer userClassNo, User user, ClassInstance classInstance) {
        this.userClassNo = userClassNo;
        this.user = user;
        this.classInstance = classInstance;
    }

}
