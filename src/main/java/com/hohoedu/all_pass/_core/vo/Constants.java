package com.hohoedu.all_pass._core.vo;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class Constants {

    public static final String QR_PRINT_USER_ID = "hohoedu";

    // 수강료 청구 시스템비(책방) 사용 센터 — 우선 PUS001만 적용, 전체 오픈 시 이 조건을 제거
    public static final Set<String> SYSTEM_FEE_CENTER_CODES = Set.of("PUS001");

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
            Map.of("id", "sat", "label", "토"));

    public static final Set<String> UNIT_INCREMENT_BOOK_CLASS_KEYS = Set.of(
            "SU", "DU", "TU", "BSN", "BSU", "BSX", "BSS");

    public static final Set<String> UNIT_INCREMENT_HAN_CLASS_KEYS = Set.of(
            "Y", "S", "P",
            "ES", "EP", "EG", "ED",
            "NES", "NEP", "NEG", "NED",
            "HSN", "HSU", "HSX", "HSS", "HSA");

    public static final Set<String> PERSON_YEAR_CLASS_KEYS = Set.of("K", "M", "J");

    public static final Map<String, String> CLASS_KEY_ROLLOVER = Map.of(
            "SU", "DU",
            "DU", "TU",
            "TU", "BSN",
            "BSN", "BSU",
            "BSU", "BSX",
            "BSX", "BSS");

    public static final Map<String, String> NE_TO_E = Map.of(
            "NES", "ES", "NEP", "EP", "NEG", "EG", "NED", "ED");

    public static final Map<String, String> E_TO_NE = Map.of(
            "ES", "NES", "EP", "NEP", "EG", "NEG", "ED", "NED");

    // 교재 자동 주문 마감일(매월 20일 고정). 유곡(ULS001)은 별도 DB 설정으로 운영
    public static final String ORDER_DEADLINE_DAY = "20";
    public static final String SECONDARY_CENTER_CODE = "ULS001";

    public static String resolveOrderDeadline(String centerCode, Supplier<String> dbDeadline) {
        return SECONDARY_CENTER_CODE.equals(centerCode) ? dbDeadline.get() : ORDER_DEADLINE_DAY;
    }
}
