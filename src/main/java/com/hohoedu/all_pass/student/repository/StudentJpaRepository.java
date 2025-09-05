package com.hohoedu.all_pass.student.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hohoedu.all_pass.student.Student;

public interface StudentJpaRepository extends JpaRepository<Student, Integer>{

}
