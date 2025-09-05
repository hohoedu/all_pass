package com.hohoedu.all_pass.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.student.model.GradeCode;

public interface GradeJpaRepository extends JpaRepository<GradeCode, Integer> {

}