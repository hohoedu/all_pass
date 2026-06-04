package com.hohoedu.all_pass.attendance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ScheduleRunResultDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@Slf4j
@RequestMapping("/scheduler")
@RestController
@RequiredArgsConstructor
public class AttendancaController {

    private final AttendanceService attendanceService;

    @PostMapping("/select")
    public ResponseEntity<?> attendanceSxcheduler() {
        log.info("AttendancaController select");
        String today = DateConfig.currentYearMonth().get("today");
        String day = DateConfig.currentYearMonth().get("currentDayName");
        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        List<ClassRespDTO.ClassWeekInfoDTO> result = attendanceService.executeScheduledAttendance(today, day, nowHHmm);

        return ResponseEntity.ok(ApiUtils.success(result));
    }

}