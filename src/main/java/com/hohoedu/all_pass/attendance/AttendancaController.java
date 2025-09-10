package com.hohoedu.all_pass.attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ProcessedClassDTO;
import com.hohoedu.all_pass.attendance._dto.AttendanceRespDTO.ScheduleRunResultDTO;
import com.hohoedu.all_pass.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hohoedu.all_pass.class_instance.model.TimeTable;

import lombok.RequiredArgsConstructor;

@RequestMapping("/scheduler")
@RestController
@RequiredArgsConstructor
public class AttendancaController {

    private final AttendanceService attendanceService;
    private final DateConfig dateConfig;

    @PostMapping("/select")
    public ResponseEntity<?> attendanceScheduler() {
        // 컨트롤러는 “입력 수집”만
        String today = dateConfig.currentYearMonth().get("today");
        String yy    = dateConfig.currentYearMonth().get("currentYear");
        String mm    = dateConfig.currentYearMonth().get("currentMonth");
        String day   = dateConfig.currentYearMonth().get("currentDayName");
        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        // 서비스가 모든 업무 규칙과 트랜잭션/멱등 제어를 담당
        ScheduleRunResultDTO result =
                attendanceService.executeScheduledAttendance(nowHHmm, yy, mm, day, today);

        return ResponseEntity.ok(ApiUtils.success(result));
    }

//    @PostMapping("/run")
//    public ResponseEntity<String> runScheduler() {
//        LocalDate today = LocalDate.now();
//        String yy  = String.valueOf(today.getYear());
//        String mm  = String.format("%02d", today.getMonthValue());
//        String day = today.getDayOfWeek()
//                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
//                .toLowerCase(Locale.ENGLISH); // mon,tue,wed...
//
//        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
//
//        List<TimeTable> finished =
//                attendanceRepository.findClassesToProcess(yy, mm, day, nowHHmm);
//
//        for (TimeTable tt : finished) {
//            attendanceService.processAttendanceAndRemedialForClass(tt, today);
//        }
//        return ResponseEntity.ok("스케줄러 실행 완료");
//    }

}