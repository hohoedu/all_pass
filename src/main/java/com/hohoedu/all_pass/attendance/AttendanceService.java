package com.hohoedu.all_pass.attendance;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ScheduleRunResultDTO;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ProcessedClassDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassWeek;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.hohoedu.all_pass.class_instance.TimeTable;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClassRepository classRepository;
    private String findWeek(LocalDate today, ClassRespDTO.ClassWeekDTO week) {

        LocalDate ju1Start = LocalDate.parse(week.getJu1Start());
        LocalDate ju1End = LocalDate.parse(week.getJu1End());
        if (!today.isBefore(ju1Start) && !today.isAfter(ju1End)) {
            return "ju_1";
        }

        LocalDate ju2Start = LocalDate.parse(week.getJu2Start());
        LocalDate ju2End = LocalDate.parse(week.getJu2End());
        if (!today.isBefore(ju2Start) && !today.isAfter(ju2End)) {
            return "ju_2";
        }

        LocalDate ju3Start = LocalDate.parse(week.getJu3Start());
        LocalDate ju3End = LocalDate.parse(week.getJu3End());
        if (!today.isBefore(ju3Start) && !today.isAfter(ju3End)) {
            return "ju_3";
        }

        LocalDate ju4Start = LocalDate.parse(week.getJu4Start());
        LocalDate ju4End = LocalDate.parse(week.getJu4End());
        if (!today.isBefore(ju4Start) && !today.isAfter(ju4End)) {
            return "ju_4";
        }

        return null; // 어느 주차에도 해당되지 않음
    }
    public ScheduleRunResultDTO executeScheduledAttendance(String nowHHmm, String yy, String mm, String day, String today) {

        LocalDate currentDay = LocalDate.parse(today);

        List<ClassRespDTO.FinishClassDTO> finished = attendanceRepository.findClassesToProcess(yy, mm, day, nowHHmm);


        Map<String, String> weekMap = new HashMap<>();

        for (ClassRespDTO.FinishClassDTO dto : finished) {
            String centerCode = dto.getCenterCode();
//
//            if (!weekMap.containsKey(centerCode)) {
//
//                ClassRespDTO.ClassWeekDTO classWeek =
//                        classRepository.findClassWeek(yy, mm, centerCode);
//
//                if (classWeek == null) {
//                    weekMap.put(centerCode, "");
//                    continue;
//                }
//
//                String week = findWeek(currentDay, classWeek);
//
//                weekMap.put(centerCode, week);
//            }
        }

        for (ClassRespDTO.FinishClassDTO dto : finished) {
            dto.setWeek(weekMap.get(dto.getCenterCode()));
        }

        int processed = 0, skipped = 0;

        List<ProcessedClassDTO> details = new ArrayList<>();

        for (ClassRespDTO.FinishClassDTO tt : finished) {
            String timeTableKey = tt.getTimeTableKey();


            try {
                attendanceRepository.bulkInsertAbsentForClass(timeTableKey, today, tt.getWeek());
                attendanceRepository.bulkInsertRemedialForClass(timeTableKey, today, tt.getWeek());
                attendanceRepository.updateLatenessForClass(timeTableKey, today);

                processed++;
                details.add(ProcessedClassDTO.processedOf(tt));

            } catch (DataAccessException e) {
                System.out.println("e = " + e.getMessage());
//                details.add(ProcessedClassDTO.failedOf(tt, e.getClass().getSimpleName()));

            }
        }
        return null;
    }

}

