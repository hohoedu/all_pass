package com.hohoedu.all_pass.family.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.family.model.RelationCode;

public interface RelationJpaRepository extends JpaRepository<RelationCode, Integer>{
    
}
