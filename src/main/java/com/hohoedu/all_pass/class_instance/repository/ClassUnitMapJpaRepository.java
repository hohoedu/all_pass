package com.hohoedu.all_pass.class_instance.repository;

import com.hohoedu.all_pass.class_instance.model.ClassUnitMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassUnitMapJpaRepository extends JpaRepository<ClassUnitMap, Integer> {
    List<ClassUnitMap> findByClassCode_ClassKey(String classKey);
}
