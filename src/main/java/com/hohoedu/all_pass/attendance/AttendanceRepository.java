package com.hohoedu.all_pass.attendance;

import java.util.List;

import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import org.apache.ibatis.annotations.Mapper;

import com.hohoedu.all_pass.class_instance.TimeTable;
import org.springframework.data.repository.query.Param;

@Mapper
public interface AttendanceRepository {

    // 종료된 수업 조회
    List<ClassRespDTO.FinishClassDTO> findClassesToProcess(
            @Param("yy") String yy,
            @Param("mm") String mm,
            @Param("dayname") String dayname,
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
            @Param("week") String week);
}
