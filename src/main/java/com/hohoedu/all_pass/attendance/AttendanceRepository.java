package com.hohoedu.all_pass.attendance;

import java.util.List;
import java.util.Map;

import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AttendanceRepository {

    // 종료된 수업 조회
    List<ClassRespDTO.FinishClassDTO> findClassesToProcess(
            @Param("centerCode") String centerCode,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("dayname") String dayname,
            @Param("week") String week,
            @Param("nowTimeHHmm") String nowTimeHHmm
    );

    // 출결 없으면 결석 update
    int bulkInsertAbsentForClass(
            @Param("timeTableKey") String timeTableKey,
            @Param("attendanceDate") String attendanceDate,
            @Param("week") String week);

    // 보강 존재 여부 조회
    int updateLatenessForClass(
            @Param("timeTableKey") String timeTableKey,
            @Param("attendanceDate") String attendanceDate);

    // 보강 등록
    int bulkInsertRemedialForClass(
            @Param("timeTableKey") String timeTableKey,
            @Param("absenceDate") String absenceDate,
            @Param("week") String week,
            @Param("yy") String yy,
            @Param("mm") String mm);

    @Update("EXEC sp_set_session_context @key = #{key}, @value = #{value}")
    void setSessionContext(@Param("key") String key, @Param("value") String value);

    int insertRemedialForStudent(
            @Param("studentId") String studentId,
            @Param("timeTableKey") String timeTableKey,
            @Param("absenceDate") String absenceDate,
            @Param("week") String week,
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("userCode") String userCode);

    int deleteRemedialForStudent(@Param("studentId") String studentId,
                                 @Param("timeTableKey") String timeTableKey,
                                 @Param("absenceDate") String absenceDate);

    String findClassStartTime(
            @Param("timeTableKey") String timeTableKey
    );

    Map<String, String> findYyMmByTimeTableKey(@Param("timeTableKey") String timeTableKey);
}
