package com.hohoedu.all_pass.class_instance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.class_instance.code.ClassCode;

public interface ClassCodeJpaRepository extends JpaRepository<ClassCode, Integer>{
    
}
