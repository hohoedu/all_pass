package com.hohoedu.all_pass.student.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.student._dto.StudentRespDTO;
import com.hohoedu.all_pass.student._dto.StudentReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.StudentReqDTO.StudentJoinDTO;
import com.hohoedu.all_pass.student.model.Student;

@Mapper
public interface StudentRepository {
    public List<Student> findAll();

    public StudentRespDTO.StudentDTO findStudentByStudentId(@Param("studentId") Integer studentId);

    public void insert(StudentJoinDTO student);

    public void statusInsert(StatusHistoryDTO historyDTO);
}
