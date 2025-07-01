package com.hohoedu.all_pass.user.code;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "user_role_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRoleCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userRoleNo;

    @Column(nullable = false, length = 20)
    private String userRole;

    @Builder
    public UserRoleCode(Integer userRoleNo, String userRole) {
        this.userRoleNo = userRoleNo;
        this.userRole = userRole;
    }

}
