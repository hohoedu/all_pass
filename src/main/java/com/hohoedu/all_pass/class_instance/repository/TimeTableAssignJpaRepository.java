package com.hohoedu.all_pass.class_instance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.class_instance.model.TimeTableAssign;

public interface TimeTableAssignJpaRepository extends JpaRepository<TimeTableAssign, Integer>{
    
}
