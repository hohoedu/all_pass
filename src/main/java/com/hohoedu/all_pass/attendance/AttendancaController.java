package com.hohoedu.all_pass.attendance;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ScheduleRunResultDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RequestMapping("/scheduler")
@RestController
@RequiredArgsConstructor
public class AttendancaController {

    private final AttendanceService attendanceService;
    private final DateConfig dateConfig;

    @PostMapping("/select")
    public ResponseEntity<?> attendanceScheduler() {

        String today = dateConfig.currentYearMonth().get("today");
        String yy    = dateConfig.currentYearMonth().get("currentYear");
        String mm    = dateConfig.currentYearMonth().get("currentMonth");
        String day   = dateConfig.currentYearMonth().get("currentDayName");
        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        ScheduleRunResultDTO result =
                attendanceService.executeScheduledAttendance(nowHHmm, yy, mm, day, today);

        return ResponseEntity.ok(ApiUtils.success(result));
    }


}