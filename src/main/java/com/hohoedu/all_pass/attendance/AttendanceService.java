package com.hohoedu.all_pass.attendance;

import java.util.ArrayList;
import java.util.List;

import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ScheduleRunResultDTO;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ProcessedClassDTO;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.class_instance.TimeTable;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public ScheduleRunResultDTO executeScheduledAttendance(String nowHHmm, String yy, String mm, String day, String today) {

        List<TimeTable> finished = attendanceRepository.findClassesToProcess(yy, mm, day, nowHHmm);

        int processed = 0, skipped = 0;

        List<ProcessedClassDTO> details = new ArrayList<>();

        for (TimeTable tt : finished) {
            String timeTableKey = tt.getTimeTableKey();

            try {
                attendanceRepository.bulkInsertAbsentForClass(timeTableKey, today);
                attendanceRepository.bulkInsertRemedialForClass(timeTableKey, today);
                attendanceRepository.updateLatenessForClass(timeTableKey, today);

                processed++;
                details.add(ProcessedClassDTO.processedOf(tt));

            } catch (DataAccessException e) {
                System.out.println("e = " + e.getMessage());
                details.add(ProcessedClassDTO.failedOf(tt, e.getClass().getSimpleName()));

            }
        }
        return null;
    }

}

