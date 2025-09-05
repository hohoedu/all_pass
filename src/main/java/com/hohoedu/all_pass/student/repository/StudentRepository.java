package com.hohoedu.all_pass.student.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.StudentTransferHistory;
import com.hohoedu.all_pass.student._dto.StudentReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.StudentReqDTO.StudentJoinDTO;

@Mapper
public interface StudentRepository {
        public List<Student> findAll(@Param("centerNo") String centerNo);

        public StudentDTO findStudentByStudentId(@Param("studentId") Integer studentId);

        public List<MainStudentDTO> selectStudentByUserCode(@Param("userCode") String userCode);

        public List<MainStudentDTO> selectStudentByClassCode(@Param("timeTableCode") String timeTableCode);

        public void insert(StudentJoinDTO student);

        public int statusHistoryInsert(StatusHistoryDTO historyDTO);

        public int studentStatusUpdate(StatusHistoryDTO historyDTO);

        public List<StudentInOutDTO> selectTransferStudents();

        public List<StudentTransferDTO> findInOutByStudentId(@Param("studentId") Integer studentId);

        public void transfer(
                        @Param("userCode") String userCode,
                        @Param("studentNo") Integer studentNo,
                        @Param("classType") String classType);

        public void insertTransferHistory(StudentTransferHistory dto);

        public boolean existsByYm(String ym);

        public void insertSnapshotIfNotExists(String ym);

        public List<StudentSnapshotRespDTO> findAllStudentOverview(
                        @Param("startYm") String startYm,
                        @Param("endYm") String endYm);

        public List<StudentSnapshotRespDTO> findStudentOverview(
                        @Param("startYm") String startYm,
                        @Param("endYm") String endYm,
                        @Param("userNo") int userNo);

        public Student findByAppId(@Param("appId") String appId);

}
