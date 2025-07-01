package com.hohoedu.all_pass.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.student.code.GradeCode;

public interface GradeJpaRepository extends JpaRepository<GradeCode, Integer> {

}