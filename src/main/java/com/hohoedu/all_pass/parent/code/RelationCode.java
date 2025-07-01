package com.hohoedu.all_pass.parent.code;

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
@Table(name = "relation_code")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RelationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer relationNo;

    @Column(nullable = false, length = 20)
    private String relation;

    @Builder
    public RelationCode(Integer relationNo, String relation) {
        this.relationNo = relationNo;
        this.relation = relation;
    }

}
