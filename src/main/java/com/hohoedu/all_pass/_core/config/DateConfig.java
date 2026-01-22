package com.hohoedu.all_pass._core.config;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class DateConfig {

    private DateConfig() {
    }

    public static Map<String, String> currentYearMonth() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        String yy = String.valueOf(today.getYear());
        String mm = String.format("%02d", today.getMonthValue());
        String dayName = today.getDayOfWeek()
                .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                .toLowerCase(Locale.ENGLISH);

        Map<String, String> map = new HashMap<>();
        map.put("today", today.toString());
        map.put("currentYear", yy);
        map.put("currentMonth", mm);
        map.put("currentDayName", dayName);

        return map;
    }
}

