package com.hohoedu.all_pass.class_instance.repository;

import com.hohoedu.all_pass.class_instance.model.ClassUnitMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ClassUnitMapJpaRepository extends JpaRepository<ClassUnitMap, Integer> {

    @Query("SELECT cum FROM ClassUnitMap cum " +
            "JOIN FETCH cum.classCode " +
            "JOIN FETCH cum.unitCode")
    List<ClassUnitMap> findAllWithUnitCode();

//    List<ClassUnitMap> findByClassCode_ClassKey(String classKey);
}
