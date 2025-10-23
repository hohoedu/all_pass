package com.hohoedu.all_pass.student.repository;

import java.util.List;

import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.student.model.StudentClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.StudentTransferHistory;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StudentJoinDTO;
import org.springframework.security.core.parameters.P;

@Mapper
public interface StudentRepository {

    public List<Student> findStudentByCenterCode(
            @Param("year") String year,
            @Param("month") String month,
            @Param("centerCode") String centerCode);

    public StudentDTO findStudentByStudentId(@Param("studentId") String studentId);

    public List<MainStudentDTO> selectStudentByUserCode(
            @Param("userCode") String userCode,
            @Param("centerCode") String centerCode);

    public List<MainStudentDTO> selectStudentByKey(
            @Param("timeTableKey") String timeTableKey,
            @Param("userCode") String userCode);

    public void insert(StudentJoinDTO student);

    public int statusHistoryInsert(StatusHistoryDTO historyDTO);

    public int studentStatusUpdate(StatusHistoryDTO historyDTO);

    public void insertStudentClass(StudentClass studentClass);

    StudentWebRespDTO.StudentStatusDTO findStatusByStudentId(@Param("studentId") String studentId);

    public List<StudentInOutDTO> selectTransferStudents(@Param("centerCode") String centerCode);

    public List<StudentTransferDTO> findInOutByStudentId(@Param("studentId") Integer studentId);

    public void transfer(
            @Param("userCode") String userCode,
            @Param("studentNo") String studentId,
            @Param("classType") String classType);

    public void insertTransferHistory(StudentTransferHistory dto);

    public boolean existsByYm(String ym);


    // 출석 여부 체크
    public Integer countByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    // 출석 insert
    public void checkinStudentAttendance(
            @Param("studentId") String studentId,
            @Param("inTime") String inTime,
            @Param("attendanceDate") String attendanceDate);

    // 하원 여부 체크
    //TODO: 수정 필요
    public List<StudentAttendance> findByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    // 하원 update
    public void checkoutStudentAttendance(
            @Param("studentId") String studentId,
            @Param("outTime") String outTime,
            @Param("attendanceDate") String attendanceDate);

    public void insertSnapshotIfNotExists(String ym);

    public List<StudentSnapshotRespDTO> findAllStudentOverview(
            @Param("startYm") String startYm,
            @Param("endYm") String endYm);

    public List<StudentSnapshotRespDTO> findStudentOverview(
            @Param("startYm") String startYm,
            @Param("endYm") String endYm,
            @Param("userNo") int userNo);

    public Student findByStudentId(
            @Param("studentId") String studentId);

    public int updateAppTokenByStudentId(
            @Param("studentId") String studentId,
            @Param("appToken") String appToken);

    public StudentAppRespDTO.AppTokenRespDTO findAppTokenByAppId(
            @Param("appId") String appId);

    public StudentAppRespDTO.AppLoginViewDTO appLogin(
            @Param("appId") String appId);

}
