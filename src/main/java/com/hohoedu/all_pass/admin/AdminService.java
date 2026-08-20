package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryEbookRepository;
import com.hohoedu.all_pass.secondary.repository.SecondaryLogisticsRepository;
import com.hohoedu.all_pass.third._dto.ThirdDTO;
import com.hohoedu.all_pass.third.repository.ThirdEbookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class AdminService {
    private static final String SECONDARY_CENTER_CODE = "ULS001";
    /** 유곡점은 반 구분이 없어 K/M/J 에 동일한 인물이 들어간다 */
    private static final List<String> SECONDARY_CLASS_KEYS = List.of("K", "M", "J");
    /** 유곡점 저장 시 기준이 되는 반 (세 반 값이 동일하므로 하나만 저장) */
    private static final String SECONDARY_BASE_CLASS_KEY = "K";

    /**
     * 이북 코드가 보관된 third DB(hohosc_TableBookLabel)는 지점을 preschool_code 로 구분한다.
     * 본사(PUS001)는 이북 코드 대상이 아니라 매핑하지 않는다.
     */
    private static final Map<String, String> PRESCHOOL_CODE_BY_CENTER = Map.of(
            "PUS002", "000",
            "DAE001", "hohows",
            "ULS001", "onlyjyu",
            "PUS003", "8170900"); // 임시 테스트용 지점. third DB에 대응 데이터 없음

    /** third DB 샘플에서 지점별로 고정돼 있던 id 값. 저장 시 그대로 채운다 */
    private static final Map<String, String> ID_BY_PRESCHOOL_CODE = Map.of(
            "000", "6288890",
            "hohows", "2628890",
            "onlyjyu", "37807059");

    /** third DB idate 컬럼 표기 양식 (예: "07 27 2026 04:43PM") */
    private static final DateTimeFormatter IDATE_FORMATTER =
            DateTimeFormatter.ofPattern("MM dd yyyy hh:mma", Locale.ENGLISH);

    /** 호수가 인물명인 교재 (unit_key A01~C10). 그 외는 호수 교재(H01~) */
    private static final Set<String> PERSON_EXT_CODES = Set.of("UA", "UB", "UC");

    /**
     * 이북 코드 접두어 둘째 자리에 세팅값이 들어가는 교재 (그 외는 ocode 2자리를 그대로 쓴다).
     * 화면의 SETTING_CLASS_KEYS 와 같은 목록이다.
     */
    private static final Set<String> SETTING_CLASS_KEYS = Set.of("Y", "S", "P", "K", "M", "J");

    /**
     * 교재 타입별 세팅값. 화면의 SETTING_OPTIONS 와 같은 목록이다.
     * 여기에 없는 값(옛 데이터의 한자 Z, C)은 세팅값으로 보지 않는다.
     */
    private static final Map<String, Set<String>> SETTING_VALUES_BY_CLASS_TYPE = Map.of(
            "1", Set.of("X", "Z"), // 한자
            "2", Set.of("X", "Y", "Z", "A")); // 독서

    private final AdminRepository adminRepository;
    private final SecondaryLogisticsRepository secondaryLogisticsRepository;
    private final SecondaryEbookRepository secondaryEbookRepository;
    private final ThirdEbookRepository thirdEbookRepository;

    public int insertPersonYear(AdminReqDTO.PersonSettingDTO reqDTO) {

        if (SECONDARY_CENTER_CODE.equals(reqDTO.getCenterCode())) {
            return saveSecondaryPersonYear(reqDTO);
        }

        int count = 0;
        for (AdminReqDTO.PersonSettingDTO.EbookClassDTO cls : reqDTO.getClasses()) {
            String classKey = cls.getClass_key();

            for (AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m : cls.getMonths()) {

                count += adminRepository.insertPersonYear(
                        reqDTO.getCenterCode(),
                        reqDTO.getYear(),
                        m.getMonth(),
                        classKey,
                        m.getUnit_key(),
                        m.getSub_unit_key());
            }
        }
        return count;
    }

    public AdminReqDTO.PersonSettingDTO loadPersonYear(AdminReqDTO.PersonFindDTO req) {

        if (SECONDARY_CENTER_CODE.equals(req.getCenterCode())) {
            return loadSecondaryPersonYear(req);
        }

        List<AdminRespDTO.PersonYearDTO> list = adminRepository.selectPersonYear(req.getCenterCode(), req.getYear());

        if (list == null || list.isEmpty()) {
            return null;
        }

        Map<String, List<AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO>> grouped = list.stream()
                .collect(Collectors.groupingBy(
                        AdminRespDTO.PersonYearDTO::getClassKey,
                        LinkedHashMap::new,
                        Collectors.mapping(row -> {
                            AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m = new AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO();
                            m.setMonth(row.getMm());
                            m.setUnit_key(row.getUnitKey());
                            m.setSub_unit_key(row.getSubUnitKey());
                            return m;
                        }, Collectors.toList())));

        List<AdminReqDTO.PersonSettingDTO.EbookClassDTO> classes = grouped.entrySet().stream()
                .map(e -> {
                    AdminReqDTO.PersonSettingDTO.EbookClassDTO c = new AdminReqDTO.PersonSettingDTO.EbookClassDTO();
                    c.setClass_key(e.getKey());
                    c.setMonths(e.getValue());
                    return c;
                })
                .collect(Collectors.toList());

        // 3) 최종 DTO 구성
        AdminReqDTO.PersonSettingDTO dto = new AdminReqDTO.PersonSettingDTO();
        dto.setCenterCode(req.getCenterCode());
        dto.setYear(req.getYear());
        dto.setClasses(classes);

        return dto;
    }

    /**
     * 유곡점(ULS001)은 우리 DB(erp_person_year)가 아닌 외부 DB의 hohosc_book_yy_config 를 사용한다.
     * - 연-월당 1건(PK: yy+mm), code = 메인 / code2 = 서브
     * - 코드값 01~30 은 우리 unit_key A01~C10 에 대응
     * - 반(K/M/J) 구분이 없어 3개 반에 동일한 값을 넣어준다
     */
    private AdminReqDTO.PersonSettingDTO loadSecondaryPersonYear(AdminReqDTO.PersonFindDTO req) {

        List<SecondaryDTO.EbookYearConfigDTO> configs = secondaryEbookRepository.findEbookYearConfig(req.getYear());

        List<AdminReqDTO.PersonSettingDTO.EbookClassDTO> classes = new ArrayList<>();
        for (String classKey : SECONDARY_CLASS_KEYS) {
            List<AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO> months = new ArrayList<>();

            if (configs != null) {
                for (SecondaryDTO.EbookYearConfigDTO config : configs) {
                    AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m = new AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO();
                    m.setMonth(config.getMm());
                    m.setUnit_key(toUnitKey(config.getUnitNo()));
                    m.setSub_unit_key(toUnitKey(config.getSubUnitNo()));
                    months.add(m);
                }
            }

            AdminReqDTO.PersonSettingDTO.EbookClassDTO c = new AdminReqDTO.PersonSettingDTO.EbookClassDTO();
            c.setClass_key(classKey);
            c.setMonths(months);
            classes.add(c);
        }

        AdminReqDTO.PersonSettingDTO dto = new AdminReqDTO.PersonSettingDTO();
        dto.setCenterCode(req.getCenterCode());
        dto.setYear(req.getYear());
        dto.setClasses(classes);

        return dto;
    }

    /**
     * 유곡점 저장: K/M/J 가 항상 같은 값이므로 K 반 기준으로 hohosc_book_yy_config 에 upsert 한다.
     * 빈 값은 NULL 로 저장하고, 해당 연도 행이 없으면 새로 INSERT 된다.
     */
    private int saveSecondaryPersonYear(AdminReqDTO.PersonSettingDTO reqDTO) {

        AdminReqDTO.PersonSettingDTO.EbookClassDTO baseClass = reqDTO.getClasses().stream()
                .filter(c -> SECONDARY_BASE_CLASS_KEY.equals(c.getClass_key()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "유곡점 저장 실패 - " + SECONDARY_BASE_CLASS_KEY + " 반 데이터가 없습니다."));

        int count = 0;
        for (AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m : baseClass.getMonths()) {
            count += secondaryEbookRepository.upsertEbookYearConfig(
                    reqDTO.getYear(),
                    m.getMonth(),
                    toUnitNo(m.getUnit_key()),
                    toUnitNo(m.getSub_unit_key()));
        }
        return count;
    }

    /** 유곡 코드 "01"~"30" → 우리 unit_key "A01"~"C10" */
    private String toUnitKey(String unitNo) {
        if (unitNo == null || unitNo.isBlank()) {
            return null;
        }

        int no;
        try {
            no = Integer.parseInt(unitNo.trim());
        } catch (NumberFormatException e) {
            log.warn("유곡 연간 이북 코드 변환 실패 - unitNo={}", unitNo);
            return null;
        }

        if (no < 1 || no > 30) {
            log.warn("유곡 연간 이북 코드 범위 초과 - unitNo={}", unitNo);
            return null;
        }

        char prefix = (char) ('A' + (no - 1) / 10);
        int seq = (no - 1) % 10 + 1;

        return String.format("%s%02d", prefix, seq);
    }

    /** 우리 unit_key "A01"~"C10" → 유곡 코드 "01"~"30" */
    private String toUnitNo(String unitKey) {
        if (unitKey == null || unitKey.isBlank()) {
            return null;
        }

        String key = unitKey.trim().toUpperCase();
        char prefix = key.charAt(0);

        int seq;
        try {
            seq = Integer.parseInt(key.substring(1));
        } catch (NumberFormatException e) {
            log.warn("유곡 연간 이북 unit_key 변환 실패 - unitKey={}", unitKey);
            return null;
        }

        if (prefix < 'A' || prefix > 'C' || seq < 1 || seq > 10) {
            log.warn("유곡 연간 이북 unit_key 범위 초과 - unitKey={}", unitKey);
            return null;
        }

        return String.format("%02d", (prefix - 'A') * 10 + seq);
    }

    public List<AdminRespDTO.KeycodeDTO> findKeycodeList(AdminReqDTO.KeycodeFindDTO req) {

        if (SECONDARY_CENTER_CODE.equals(req.getCenterCode())) {
            List<AdminRespDTO.KeycodeDTO> secondaryList = findSecondaryKeycodeList(req);
            fillExistingKeycodes(secondaryList, req);
            return secondaryList;
        }

        List<AdminRespDTO.KeycodeDTO> list = adminRepository.selectKeycodeList(req.getCenterCode(), req.getYear(),
                req.getMonth());

        // 교재 코드는 지점과 무관하게 같은 체계이므로, 매핑을 역방향으로 읽어 Ggubun / ocode 를 채운다
        Map<String, Map<String, String>> mapByClassKey = adminRepository.selectSecondaryClassMap(SECONDARY_CENTER_CODE)
                .stream()
                .collect(Collectors.toMap(m -> m.get("classKey"), m -> m, (a, b) -> a));

        list.forEach(d -> {
            Map<String, String> m = mapByClassKey.get(d.getClassKey());
            if (m == null)
                return;
            d.setGgubun(m.get("extCode"));
            d.setOcode(m.get("ocode"));
        });

        fillExistingKeycodes(list, req);

        return list;
    }

    /**
     * 화면에서 만든 이북 코드를 임시 테이블(erp_table_book_label_temp)에 저장한다.
     * PK 가 keycode 단독이라 재생성 시 값이 바뀌므로, 자연키(preschool_code, orderym, ggubun, mgubun)
     * 기준으로 기존 행을 지우고 새로 넣는다(delete-then-insert).
     */
    public int saveKeycodeList(AdminReqDTO.KeycodeSaveDTO req) {

        String preschoolCode = PRESCHOOL_CODE_BY_CENTER.get(req.getCenterCode());
        if (preschoolCode == null) {
            throw new IllegalStateException("이북 코드 저장 대상 지점이 아닙니다.");
        }

        String orderym = req.getYear() + req.getMonth();

        Map<String, String> extCodeByClassKey = adminRepository.selectSecondaryClassMap(SECONDARY_CENTER_CODE)
                .stream()
                .collect(Collectors.toMap(m -> m.get("classKey"), m -> m.get("extCode"), (a, b) -> a));

        LocalDateTime now = LocalDateTime.now();
        String idate = now.format(IDATE_FORMATTER);
        String startdate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String expiration = now.plusDays(40).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String id = ID_BY_PRESCHOOL_CODE.get(preschoolCode);

        List<Map<String, Object>> rows = new ArrayList<>();

        for (AdminReqDTO.KeycodeSaveDTO.KeycodeItemDTO item : Optional.ofNullable(req.getCodes()).orElse(List.of())) {
            if (item.getKeyCode() == null || item.getKeyCode().isBlank()) {
                continue; // 세팅값 미선택 등으로 아직 완성되지 않은 행
            }

            String ggubun = extCodeByClassKey.get(item.getClassKey());
            if (ggubun == null) {
                log.warn("이북 코드 저장 실패 - class_key 매핑 없음: {}", item.getClassKey());
                continue;
            }

            String mgubun = unitNoOf(ggubun, item.getUnitKey());
            if (mgubun == null) {
                log.warn("이북 코드 저장 실패 - 호수 계산 실패: classKey={}, unitKey={}", item.getClassKey(), item.getUnitKey());
                continue;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("keycode", item.getKeyCode().replace("-", "").toUpperCase());
            row.put("preschoolCode", preschoolCode);
            row.put("yy", req.getYear());
            row.put("idate", idate);
            row.put("startdate", startdate);
            row.put("expiration", expiration);
            row.put("oldqty", 0);
            row.put("id", id);
            row.put("orderym", orderym);
            row.put("ggubun", ggubun);
            row.put("mgubun", mgubun);
            rows.add(row);
        }

        if (rows.isEmpty()) {
            return 0;
        }

        thirdEbookRepository.deleteKeycodes(rows);
        thirdEbookRepository.insertKeycodes(rows);

        return rows.size();
    }

    /**
     * third DB(hohosc_TableBookLabel)에 이미 만들어 둔 이북 코드를 각 행에 붙인다.
     * 매핑이 없는 지점(본사)이나 코드가 없는 행은 그대로 두고, 화면에서 새로 생성한다.
     */
    private void fillExistingKeycodes(List<AdminRespDTO.KeycodeDTO> list, AdminReqDTO.KeycodeFindDTO req) {

        if (list == null || list.isEmpty()) {
            return;
        }

        String preschoolCode = PRESCHOOL_CODE_BY_CENTER.get(req.getCenterCode());
        if (preschoolCode == null) {
            return;
        }

        String orderym = req.getYear() + req.getMonth();
        List<ThirdDTO.KeycodeRawDTO> raws = thirdEbookRepository.findKeycodeList(preschoolCode, orderym);

        if (raws == null || raws.isEmpty()) {
            return;
        }

        // 외부 교재 코드(ggubun) → 우리 class_key. 교재 코드 체계는 지점과 무관하게 같다
        Map<String, String> classKeyByExtCode = adminRepository.selectSecondaryClassMap(SECONDARY_CENTER_CODE)
                .stream()
                .collect(Collectors.toMap(m -> m.get("extCode"), m -> m.get("classKey"), (a, b) -> a));

        // mgubun 이 있으면 그대로, 없으면 keycode 안의 호수 자리로 찾는다
        Map<String, String> byUnitNo = new HashMap<>();
        Map<String, String> byKeycodeNo = new HashMap<>();

        for (ThirdDTO.KeycodeRawDTO raw : raws) {
            String classKey = classKeyByExtCode.get(raw.getGgubun());
            if (classKey == null)
                continue;

            String unitNo = toUnitNoDigits(raw.getMgubun());
            if (unitNo != null) {
                byUnitNo.putIfAbsent(classKey + "|" + unitNo, raw.getKeycode());
                continue;
            }

            String keycodeNo = unitNoInKeycode(raw.getKeycode());
            if (keycodeNo != null) {
                byKeycodeNo.putIfAbsent(classKey + "|" + keycodeNo, raw.getKeycode());
            }
        }

        for (AdminRespDTO.KeycodeDTO d : list) {
            if (d.isLevelUnit())
                continue; // 급수는 이북 코드 대상이 아니다

            String unitNo = unitNoOf(d);
            String keyCode = unitNo == null ? null : byUnitNo.get(d.getClassKey() + "|" + unitNo);

            // mgubun 이 비어 있던 행은 keycode 의 호수 자리로 맞춘다.
            // 이 자리는 unit_key 의 숫자 부분과 같다 (H08 → 08, B06 → 06)
            if (keyCode == null) {
                String keycodeNo = toUnitNoDigits(d.getUnitKey());
                if (keycodeNo != null) {
                    keyCode = byKeycodeNo.get(d.getClassKey() + "|" + keycodeNo);
                }
            }

            d.setSettingValue(settingValueOf(d, keyCode));
            d.setKeyCode(formatKeyCode(keyCode));
        }
    }

    /**
     * 이북 코드 접두어 둘째 자리에서 세팅값을 읽는다. 예) GX08X3AR → X
     * 세팅값을 쓰지 않는 교재(접두어가 ocode 2자리 그대로)는 null 을 준다.
     * 교재 타입에 없는 값(옛 데이터의 한자 Z, C)도 null 로 두어 화면에서 다시 고르게 한다.
     */
    private String settingValueOf(AdminRespDTO.KeycodeDTO dto, String keyCode) {
        if (keyCode == null || keyCode.length() < 2) {
            return null;
        }

        if (!SETTING_CLASS_KEYS.contains(dto.getClassKey())) {
            return null;
        }

        Set<String> allowed = SETTING_VALUES_BY_CLASS_TYPE.get(dto.getClassType());
        if (allowed == null) {
            return null;
        }

        String value = keyCode.substring(1, 2).toUpperCase();

        return allowed.contains(value) ? value : null;
    }

    /**
     * 화면 표기용으로 접두어 4자리와 난수 4자리 사이에 '-' 를 넣는다. 예) GX08X3AR → GX08-X3AR
     * 이미 '-' 가 있거나 8자리가 아닌 값은 그대로 둔다.
     */
    private String formatKeyCode(String keyCode) {
        if (keyCode == null || keyCode.length() != 8 || keyCode.contains("-")) {
            return keyCode;
        }

        return keyCode.substring(0, 4) + "-" + keyCode.substring(4);
    }

    /**
     * keycode 의 호수 자리(접두어 2자리 뒤 숫자 2자리)를 뽑는다. 예) GX08X3AR → 08
     * 인물 교재는 이 자리가 unit_key 의 숫자 부분이라 A/B/C 구분은 담기지 않는다.
     */
    private String unitNoInKeycode(String keycode) {
        if (keycode == null || keycode.length() < 4) {
            return null;
        }

        String no = keycode.substring(2, 4);
        if (!no.matches("\\d{2}")) {
            return null;
        }

        return no;
    }

    /**
     * 우리 unit_key 를 외부 DB 의 호수 번호(2자리)로 바꾼다.
     * - 인물 교재: A01~C10 → 01~30
     * - 호수 교재: H01~H15 → 01~15
     */
    private String unitNoOf(AdminRespDTO.KeycodeDTO dto) {
        return unitNoOf(dto.getGgubun(), dto.getUnitKey());
    }

    /**
     * 우리 unit_key 를 외부 DB 의 호수 번호(2자리)로 바꾼다. 저장 로직에서도 같은 계산이 필요해 분리했다.
     * - 인물 교재: A01~C10 → 01~30
     * - 호수 교재: H01~H15 → 01~15
     */
    private String unitNoOf(String ggubun, String unitKey) {
        if (unitKey == null || unitKey.isBlank()) {
            return null;
        }

        if (PERSON_EXT_CODES.contains(ggubun)) {
            return toUnitNo(unitKey);
        }
        return toUnitNoDigits(unitKey);
    }

    /** 숫자만 뽑아 2자리로 맞춘다. 숫자가 없으면 null */
    private String toUnitNoDigits(String value) {
        if (value == null) {
            return null;
        }

        String digits = value.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }

        return String.format("%02d", Integer.parseInt(digits));
    }

    /**
     * 유곡점(ULS001)은 시간표가 외부 DB(hohosc_DailyTime)에 있고 class_type 구분이 없다.
     * 교재 코드(ggubun)를 우리 class_key 로 옮기면 교재명/타입/호수 코드는 우리 코드 테이블에서 그대로 따라온다.
     */
    private List<AdminRespDTO.KeycodeDTO> findSecondaryKeycodeList(AdminReqDTO.KeycodeFindDTO req) {

        List<SecondaryDTO.KeycodeRawDTO> raws = secondaryEbookRepository.findKeycodeList(req.getYear(), req.getMonth());

        if (raws == null || raws.isEmpty()) {
            return List.of();
        }

        // 외부 교재 코드 → 우리 class_key / ocode (erp_secondary_class_map)
        Map<String, Map<String, String>> mapByExtCode = adminRepository.selectSecondaryClassMap(req.getCenterCode())
                .stream()
                .collect(Collectors.toMap(m -> m.get("extCode"), m -> m, (a, b) -> a));

        Map<String, ClassCode> classByKey = adminRepository.selectClassCodes().stream()
                .collect(Collectors.toMap(ClassCode::getClassKey, c -> c, (a, b) -> a));

        Map<String, UnitCode> unitByName = adminRepository.selectUnitCodes().stream()
                .collect(Collectors.toMap(UnitCode::getUnitName, u -> u, (a, b) -> a));

        // 우리 코드 테이블에 없는 값이라도 버리지 않고 원본 그대로 넣는다
        List<AdminRespDTO.KeycodeDTO> result = new ArrayList<>();
        for (SecondaryDTO.KeycodeRawDTO raw : raws) {

            // 조회 컬럼이 모두 NULL 인 행은 MyBatis 가 null 로 담아준다
            if (raw == null || raw.getGgubun() == null)
                continue;

            // 급수는 이북 코드 대상이 아니다
            if (isSecondaryLevelUnit(raw.getGgubun(), raw.getMgubun()))
                continue;

            Map<String, String> classMap = mapByExtCode.get(raw.getGgubun());
            String classKey = classMap == null ? null : classMap.get("classKey");
            ClassCode classCode = classKey == null ? null : classByKey.get(classKey);
            UnitCode unitCode = unitByName.get(raw.getUnitName());

            AdminRespDTO.KeycodeDTO dto = new AdminRespDTO.KeycodeDTO();
            dto.setClassKey(classKey != null ? classKey : raw.getGgubun());
            dto.setClassName(classCode != null ? classCode.getClassName() : raw.getClassName());
            dto.setClassType(classCode != null ? classCode.getClassType() : null);
            dto.setUnitKey(unitCode != null ? unitCode.getUnitKey() : raw.getMgubun());
            dto.setUnitName(raw.getUnitName());
            dto.setOcode(classMap == null ? null : classMap.get("ocode"));
            // 유곡 호수 코드 2x 는 급수
            dto.setGgubun(raw.getGgubun());
            dto.setMgubun(raw.getMgubun());
            result.add(dto);
        }

        // 다른 지점과 같은 순서(교재 → 호수)로 맞추고, 매칭 안 된 행은 뒤로 보낸다
        result.sort(Comparator
                .comparingInt((AdminRespDTO.KeycodeDTO d) -> codeOrder(classByKey.get(d.getClassKey())))
                .thenComparingInt(d -> codeOrder(unitByName.get(d.getUnitName()))));

        return result;
    }

    /**
     * 유곡 호수 코드가 급수인지 판단한다.
     * 호수 교재는 호수가 1~10 이라 2x 가 급수(21=8급, 22=7급 ...)지만,
     * 인물 교재(키움/만남/자람)는 인물이 01~30 이라 2x 도 정상 호수다.
     */
    private boolean isSecondaryLevelUnit(String ggubun, String mgubun) {
        if (mgubun == null || !mgubun.startsWith("2")) {
            return false;
        }

        return !PERSON_EXT_CODES.contains(ggubun);
    }

    /** 우리 코드 테이블에 없는 값은 정렬에서 맨 뒤로 */
    private int codeOrder(ClassCode code) {
        return code == null ? Integer.MAX_VALUE : code.getId();
    }

    private int codeOrder(UnitCode code) {
        return code == null ? Integer.MAX_VALUE : code.getId();
    }

    public void saveBookSuggest(AdminReqDTO.BookSuggestSaveReqDTO req) {
        log.info("service: saveBookSuggest");

        List<String> classKeys;
        if ("J".equals(req.getClassKey())) {
            classKeys = List.of("K", "M", "J");

        } else if ("BSS".equals(req.getClassKey())) {
            classKeys = List.of("BSA", "BSS");

        } else {
            classKeys = List.of(req.getClassKey());
        }

        for (String classKey : classKeys) {
            for (AdminReqDTO.BookSuggestSaveReqDTO.WeekDTO w : req.getWeeks()) {

                AdminReqDTO.BookSuggestDTO dto = new AdminReqDTO.BookSuggestDTO();
                dto.setClassKey(classKey);
                dto.setYy(req.getYy());
                dto.setMm(req.getMm());
                dto.setWeek(w.getWeek());

                dto.setSubjectKey(w.getSubjectKey());
                dto.setBookName(w.getBookName());
                dto.setPublisher(w.getPublisher());
                dto.setBookImageUrl(w.getImageUrl());

                int affected = adminRepository.upsertBookSuggest(dto);

                if (affected == 0) {
                    throw new IllegalStateException(
                            "MERGE 결과 0건 - classKey=" + classKey
                                    + ", yy=" + dto.getYy()
                                    + ", mm=" + dto.getMm()
                                    + ", week=" + dto.getWeek());
                }
            }
        }
    }

    public List<SubjectCode> findSubjects() {

        return adminRepository.findSubject();
    }

    public List<AdminRespDTO.BookSuggestViewDTO> findBookSuggestByMonth(String classKey, String year, String month) {
        List<AdminRespDTO.BookSuggestViewDTO> response = adminRepository.findBookSuggestByMonth(classKey, year, month);
        return response;
    }

    public List<AdminRespDTO.BookSuggestViewDTO> findBookSuggest() {

        return adminRepository.findBookSuggest();
    }

    public void findAdminOrderList() {
    }

    public void updateOrderDeadline(AdminReqDTO.OrderDeadlineDTO dto) {
        adminRepository.updateOrderDeadline(dto);

        if (SECONDARY_CENTER_CODE.equals(dto.getCenterCode())) {
            secondaryLogisticsRepository.updateOrderDeadline(dto.getDeadline());
        }
    }
}