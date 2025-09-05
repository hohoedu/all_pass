package com.hohoedu.all_pass.attendance;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.class_instance.model.TimeTable;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    // 수업 종료 후 멱등 처리(여러 번 실행돼도 동일 결과)
    @Transactional
    public void processAttendanceAndRemedialForClass(TimeTable classInfo, LocalDate targetDate) {
        String ymd = targetDate.toString();

        attendanceRepository.bulkInsertAbsentForClass(classInfo.getTimeTableKey(), ymd);
        attendanceRepository.bulkInsertRemedialForClass(classInfo.getTimeTableKey(), ymd);
        attendanceRepository.updateLatenessForClass(classInfo.getTimeTableKey(), ymd);
    }
}
