package com.hohoedu.all_pass.attendance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ScheduleRunResultDTO;
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
        String yy = DateConfig.currentYearMonth().get("currentYear");
        String mm = DateConfig.currentYearMonth().get("currentMonth");
        String day = DateConfig.currentYearMonth().get("currentDayName");
        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        ScheduleRunResultDTO result = attendanceService.executeScheduledAttendance(nowHHmm, yy, mm, day, today);

        return ResponseEntity.ok(ApiUtils.success(result));
    }


}