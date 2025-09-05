package com.hohoedu.all_pass.attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hohoedu.all_pass.class_instance.model.TimeTable;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AttendancaController {

    private final AttendanceService attendanceService;
    private final AttendanceRepository attendanceRepository;

    @PostMapping("/run")
    public ResponseEntity<String> runScheduler() {
        LocalDate today = LocalDate.now();
        String yy  = String.valueOf(today.getYear());
        String mm  = String.format("%02d", today.getMonthValue());
        String day = today.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toLowerCase(Locale.ENGLISH); // mon,tue,wed...

        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        List<TimeTable> finished =
                attendanceRepository.findClassesToProcess(yy, mm, day, nowHHmm);

        for (TimeTable tt : finished) {
            attendanceService.processAttendanceAndRemedialForClass(tt, today);
        }
        return ResponseEntity.ok("스케줄러 실행 완료");
    }
}