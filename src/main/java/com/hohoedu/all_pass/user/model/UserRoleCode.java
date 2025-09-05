package com.hohoedu.all_pass.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_user_role_code", uniqueConstraints = @UniqueConstraint(name = "uq_user_role_key", columnNames = "role_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRoleCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "role_key", nullable = false, length = 30)
    private String roleKey;

    @Column(name = "role_name", nullable = false, length = 50)
    private String roleName;

    @Builder
    public UserRoleCode(String roleKey, String roleName) {
        this.roleKey = roleKey;
        this.roleName = roleName;
    }
}