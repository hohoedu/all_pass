package com.hohoedu.all_pass.attendance;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.hohoedu.all_pass.class_instance.model.TimeTable;

@Mapper
public interface AttendanceRepository {

    // 오늘 '끝난 수업' 조회 (end_time <= nowHH:mm)
    List<TimeTable> findClassesToProcess(
        @Param("yy") String yy,
        @Param("mm") String mm,
        @Param("dayname") String dayname,
        @Param("nowTimeHHmm") String nowTimeHHmm
    );

    // 멱등 벌크 처리
    int bulkInsertAbsentForClass(@Param("timeTableCode") String timeTableCode,
                                 @Param("attendanceDate") String attendanceDate);

    int bulkInsertRemedialForClass(@Param("timeTableCode") String timeTableCode,
                                   @Param("absenceDate") String absenceDate);

    int updateLatenessForClass(@Param("timeTableCode") String timeTableCode,
                               @Param("attendanceDate") String attendanceDate);
}
