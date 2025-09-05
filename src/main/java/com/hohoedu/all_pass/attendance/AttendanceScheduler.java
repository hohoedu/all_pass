package com.hohoedu.all_pass.attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hohoedu.all_pass.class_instance.model.TimeTable;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;
    private final AttendanceService attendanceService;

    // 매 분 실행 (KST). '끝난 수업'만 선택해서 멱등 처리
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void runForFinishedClasses() {
        LocalDate today = LocalDate.now();
        String yy = String.valueOf(today.getYear());
        String mm = String.format("%02d", today.getMonthValue());
        String day = today.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toLowerCase(Locale.ENGLISH); // mon,tue,wed...

        String nowHHmm = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        List<TimeTable> finished = attendanceRepository.findClassesToProcess(yy, mm, day, nowHHmm);

        for (TimeTable tt : finished) {
            attendanceService.processAttendanceAndRemedialForClass(tt, today);
        }
    }
}
