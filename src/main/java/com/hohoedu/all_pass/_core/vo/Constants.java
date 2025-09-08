package com.hohoedu.all_pass._core.vo;

import java.util.List;
import java.util.Map;

public class Constants {

    public static final Map<String, String> DAY_NAME_MAP = Map.of(
            "mon", "월",
            "tue", "화",
            "wed", "수",
            "thu", "목",
            "fri", "금",
            "sat", "토",
            "sun", "일");

    public static final List<Map<String, String>> DAYS = List.of(
            Map.of("id", "mon", "label", "월"),
            Map.of("id", "tue", "label", "화"),
            Map.of("id", "wed", "label", "수"),
            Map.of("id", "thu", "label", "목"),
            Map.of("id", "fri", "label", "금"),
            Map.of("id", "sat", "label", "토")
    );
}


