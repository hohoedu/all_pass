package com.hohoedu.all_pass._core.vo;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
