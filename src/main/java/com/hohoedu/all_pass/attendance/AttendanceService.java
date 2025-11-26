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

        if (today == null || week == null) {
            return "";
        }

        try {
            if (isSame(today, week.getMon()) ||
                    isSame(today, week.getTue()) ||
                    isSame(today, week.getWed()) ||
                    isSame(today, week.getThu()) ||
                    isSame(today, week.getFri()) ||
                    isSame(today, week.getSat()) ||
                    isSame(today, week.getSun())) {

                return week.getWeek();   // "ju_1", "ju_2" 등
            }

        } catch (Exception e) {
            log.error("주차 판단 오류: {}", e.getMessage());
        }

        return ""; // 해당 없음
    }

    private boolean isSame(LocalDate today, String target) {
        if (target == null || target.isBlank()) return false;

        try {
            LocalDate parsed = LocalDate.parse(target);
            return today.isEqual(parsed);
        } catch (Exception e) {
            log.warn("날짜 파싱 실패: {}", target);
            return false;
        }
    }


    public ScheduleRunResultDTO executeScheduledAttendance(
            String nowHHmm, String yy, String mm, String day, String today) {

        LocalDate currentDay = LocalDate.parse(today);

        List<ClassRespDTO.FinishClassDTO> finished = attendanceRepository.findClassesToProcess(yy, mm, day, nowHHmm);

        Map<String, String> weekMap = new HashMap<>();

        for (ClassRespDTO.FinishClassDTO dto : finished) {
            String centerCode = dto.getCenterCode();

            if (!weekMap.containsKey(centerCode)) {

                List<ClassRespDTO.ClassWeekDTO> classWeeks = classRepository.getClassWeek(yy, mm, centerCode);
                if (classWeeks == null || classWeeks.isEmpty()) {
                    weekMap.put(centerCode, "");
                    continue;
                }

                String week = "";
                for (ClassRespDTO.ClassWeekDTO w : classWeeks) {
                    week = findWeek(currentDay, w);
                    if (!week.isBlank()) break;
                }

                weekMap.put(centerCode, week);
            }
        }

        for (ClassRespDTO.FinishClassDTO dto : finished) {
            dto.setWeek(weekMap.get(dto.getCenterCode()));
        }

        int processed = 0;
        List<ProcessedClassDTO> details = new ArrayList<>();

        for (ClassRespDTO.FinishClassDTO tt : finished) {

            try {
                attendanceRepository.bulkInsertAbsentForClass(tt.getTimeTableKey(), today, tt.getWeek());
                attendanceRepository.bulkInsertRemedialForClass(tt.getTimeTableKey(), today, tt.getWeek());
                attendanceRepository.updateLatenessForClass(tt.getTimeTableKey(), today);

                processed++;
                details.add(ProcessedClassDTO.processedOf(tt));

            } catch (DataAccessException e) {
                log.error("출석 처리 중 오류", e);
            }
        }

        return null; // 필요 시 ScheduleRunResultDTO 반환 구성
    }

}

