package com.hohoedu.all_pass.parent.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.parent.code.RelationCode;

public interface RelationJpaRepository extends JpaRepository<RelationCode, Integer>{
    
}
