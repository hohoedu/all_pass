package com.hohoedu.all_pass.class_instance.repository;

import java.util.List;

import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassMonthlyScoreDTO.MonthlyScoreDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassRegisterDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.BeforeClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.InitRecordDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RecordStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableLabelDTO;

import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.class_instance.model.TimeTable;
import com.hohoedu.all_pass.class_instance.model.TimeTableCode;
import com.hohoedu.all_pass.user.User;

@Mapper
public interface ClassRepository {

    public void registerClass(ClassRegisterDTO classRegister);

    public void createTimeTableKey(TimeTableCode entity);

    public TimeTableDTO existsByYearAndMonthAndPeriodNo(
            @Param("periodNo") String periodNo,
            @Param("year") String year,
            @Param("month") String month,
            @Param("dayname") String dayname);

    public int updateClass(
            @Param("dto") ClassReqDTO.ClassRegisterDTO dto,
            @Param("timeTableKey") String timeTableKey,
            @Param("userCode") String userCode);

    public void addStudent(ClassReqDTO.AddStudentDTO addStudentDTO);

    public void insertMonthlyScore(
            @Param("dto") ClassReqDTO.AddStudentDTO dto,
            @Param("yy") String yy,
            @Param("mm") String mm);

    public List<TimeTableDTO> findTimeTableBasic(
            @Param("userCode") String userCode,
            @Param("year") String year,
            @Param("month") String month);

    public List<TimeTableDTO.StudentDTO> findStudentsByTimeTableKey(String timeTableKey);

    public int countByTimeTableKey(@Param("timeTableKey") String timeTableKey);

    public void deleteByAssignNo(@Param("timeTableAssignNo") Integer timeTableAssignNo);

    public List<TimeTable> findTimeTable(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("userNo") String userNo);

    public List<TimeTableCode> findTimeTableCodeByUserNo(@Param("userNo") Integer userNo);

    // 센터별 선생님 조회
    public List<User> findUserByCenterNo(@Param("centerNo") String centerNo);

    public List<InitRecordDTO> findTimeTableByUserNo(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("userNo") String userNo);

    // 날짜별 선생님별 수업 조회
    public List<InitRecordDTO> findTimeTableByDate(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("dayName") String dayName,
            @Param("userCode") String userCode);

    public List<RecordStudentDTO> findTimeTableByKey(
            @Param("timeTableKey") String timeTableKey,
            @Param("date") String date);

    public List<RemedialDTO> findRemedialByUserNo(
            @Param("year") String year,
            @Param("month") String month);

    public int updateRemedialAction(
            @Param("remedialKey") String remedialKey,
            @Param("action") boolean action);

    public void updateRemedialDate(
            @Param("remedialKey") String remedialKey,
            @Param("remedialDate") String remedialDate);

    // 출석 여부 체크
    public Integer countByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    // 출석 insert
    public void insertStudentAttendance(StudentAttendance studentAttendance);

    // 하원 여부 체크
    public StudentAttendance findByStudentAndDate(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate);

    // 하원 update
    public int updateStudentAttendance(
            @Param("studentId") String studentId,
            @Param("attendanceDate") String attendanceDate,
            @Param("outTime") String outTime);

    // 선생님별 클래스 코드 조회
    public List<TimeTableLabelDTO> findClassLabelByUserCode(
            @Param("userCode") String userCode,
            @Param("yy") String yy,
            @Param("mm") String mm);

    public BeforeClassRespDTO findBeforeClass(
            @Param("classKey") String classKey,
            @Param("unitKey") String unitKey,
            @Param("week") String week,
            @Param("timeTableKey") String timeTableKey);

    public List<MonthlyStudentDTO> findStudentByClassCode(
            @Param("classCode") String classCode);

    int updateMonthlyScore(
            @Param("studentId") String studentId,
            @Param("classCode") String classCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("score") MonthlyScoreDTO score);
}
