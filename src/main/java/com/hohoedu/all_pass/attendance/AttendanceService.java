package com.hohoedu.all_pass.attendance;

import java.util.List;

import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final ClassRepository classRepository;

    public List<ClassRespDTO.ClassWeekInfoDTO> executeScheduledAttendance(String today, String dayname,
            String nowHHmm) {

        // 1. 오늘 날짜에 맞는 수업 주차를 찾고 수업의 년월을 구하기
        List<ClassRespDTO.ClassWeekInfoDTO> weekInfoList = classRepository.findWeekInfoAllCenters(today, dayname);

        for (ClassRespDTO.ClassWeekInfoDTO weekInfo : weekInfoList) {
            // 2. 센터별 종료된 수업 찾기
            List<ClassRespDTO.FinishClassDTO> finished = attendanceRepository.findClassesToProcess(
                    weekInfo.getCenterCode(),
                    weekInfo.getYear(),
                    weekInfo.getMonth(),
                    dayname,
                    weekInfo.getWeek(),
                    nowHHmm);

            if (finished == null || finished.isEmpty()) {
                log.info("[출석처리 SKIP] 처리 대상 없음 centerCode={}", weekInfo.getCenterCode());
                continue;
            }

            for (ClassRespDTO.FinishClassDTO tt : finished) {
                try {
                    // 1. 지각 처리
                    attendanceRepository.updateLatenessForClass(tt.getTimeTableKey(), today);

                    // 2. 결석 처리 (before → absent)
                    attendanceRepository.bulkInsertAbsentForClass(tt.getTimeTableKey(), today, tt.getWeek());

                    // 3. 보충 등록 (absent인 학생만 insert)
                    attendanceRepository.bulkInsertRemedialForClass(
                            tt.getTimeTableKey(),
                            today,
                            tt.getWeek(),
                            weekInfo.getYear(),
                            weekInfo.getMonth());

                } catch (Exception e) {
                    log.error("[출석처리 오류] timeTableKey={}, centerCode={}",
                            tt.getTimeTableKey(), tt.getCenterCode(), e);
                }
            }
        }
        return weekInfoList;
    }

}
