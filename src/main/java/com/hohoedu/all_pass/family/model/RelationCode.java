package com.hohoedu.all_pass.family.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "erp_relation_code", uniqueConstraints = @UniqueConstraint(name = "uq_relation_code_key", columnNames = "relation_key"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RelationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "relation_key", nullable = false, length = 20)
    private String relationKey;

    @Column(name = "relation_name", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private String relationName;

    @Builder
    public RelationCode(String relationKey, String relationName) {
        this.relationKey = relationKey;
        this.relationName = relationName;
    }
}