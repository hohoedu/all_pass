package com.hohoedu.all_pass.class_instance.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.class_instance.TimeTable;

public interface ClassJpaRepository extends JpaRepository<TimeTable, Integer> {

}
