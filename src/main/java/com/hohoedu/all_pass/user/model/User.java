package com.hohoedu.all_pass.user.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import com.hohoedu.all_pass.center.model.Center;
import com.hohoedu.all_pass.user.code.UserRoleCode;

import jakarta.persistence.Column;
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
@Table(name = "user_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userNo;

    @Column(nullable = false, length = 10)
    private String userId;

    @Column(nullable = false, length = 20)
    private String password;

    @Column(nullable = false, length = 20)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_role_no")
    private UserRoleCode userRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "center_no")
    private Center center;

    @CreationTimestamp
    private Timestamp createdAt;

    @Builder
    public User(Integer userNo, String userId, String password, String username, UserRoleCode userRole,
            Center center, Timestamp createdAt) {
        this.userNo = userNo;
        this.userId = userId;
        this.password = password;
        this.username = username;
        this.userRole = userRole;
        this.center = center;
        this.createdAt = createdAt;
    }

}
