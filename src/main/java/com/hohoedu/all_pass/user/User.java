package com.hohoedu.all_pass.user;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user.model.UserRoleCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "erp_user", uniqueConstraints = @UniqueConstraint(name = "uq_user_code", columnNames = "user_code"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_code", nullable = false, length = 50)
    private String userCode;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "salt")
    private String salt;

    @Column(name = "type")
    private String type;

    @Column(name = "user_phone")
    private String userPhone;

    @Column(name = "use_yn")
    private Boolean useYn;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_key", referencedColumnName = "role_key", nullable = false)
    private UserRoleCode role;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "center_code", referencedColumnName = "center_code", nullable = false)
    private Center center;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String userCode, String userId, String userName, String userPhone, String passwordHash, String salt, String type, Boolean useYn,
                UserRoleCode role, Center center) {
        this.userCode = userCode;
        this.userId = userId;
        this.userName = userName;
        this.passwordHash = passwordHash;
        this.userPhone = userPhone;
        this.salt = salt;
        this.type = type;
        this.useYn = useYn;
        this.role = role;
        this.center = center;
    }
}