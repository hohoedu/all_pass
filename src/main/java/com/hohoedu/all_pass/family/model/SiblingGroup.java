package com.hohoedu.all_pass.family.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "erp_sibling_group", uniqueConstraints = @UniqueConstraint(name = "uq_sibling_key", columnNames = "sibling_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SiblingGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "sibling_key", nullable = false, length = 20)
    private String siblingKey;

    @Column(name = "group_name", length = 20, columnDefinition = "NVARCHAR(20)")
    private String groupName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public SiblingGroup(String siblingKey, String groupName) {
        this.siblingKey = siblingKey;
        this.groupName = groupName;
    }
}
