package com.hohoedu.all_pass.class_instance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.class_instance.model.ClassCode;

import java.util.List;
import java.util.Optional;

public interface ClassCodeJpaRepository extends JpaRepository<ClassCode, Integer> {

    Optional<ClassCode> findByClassKey(String classKey);
}
