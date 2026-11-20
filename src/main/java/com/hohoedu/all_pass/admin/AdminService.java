package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryEbookRepository;
import com.hohoedu.all_pass.secondary.repository.SecondaryLogisticsRepository;
import com.hohoedu.all_pass.third._dto.ThirdDTO;
import com.hohoedu.all_pass.third.repository.ThirdEbookRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
    private final ClassService classService;

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

        // D01 은 A10 과 같은 호수로 본다 (유곡 코드 체계에는 D 계열이 없음)
        if ("D01".equals(key)) {
            key = "A10";
        }

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

    /**
     * 센터별 현황 : 센터별 인원수 + 교재비 미납 학생
     * - 미납 인원/금액은 미납 학생 목록에서 센터별로 집계해 넣는다 (두 표의 숫자가 항상 일치)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> findCenterStatus(String year, String month) {
        List<AdminRespDTO.CenterStatusDTO> centers = adminRepository.findCenterStatusSummary(year, month, year + "-" + month);
        List<AdminRespDTO.UnpaidBookFeeDTO> unpaidStudents = adminRepository.findUnpaidBookFeeStudents(year, month);

        Map<String, List<AdminRespDTO.UnpaidBookFeeDTO>> unpaidByCenter = unpaidStudents.stream()
                .collect(Collectors.groupingBy(AdminRespDTO.UnpaidBookFeeDTO::getCenterCode));

        for (AdminRespDTO.CenterStatusDTO center : centers) {
            List<AdminRespDTO.UnpaidBookFeeDTO> unpaid = unpaidByCenter.getOrDefault(center.getCenterCode(), List.of());
            center.setUnpaidCount(unpaid.size());
            center.setUnpaidAmount(unpaid.stream().mapToInt(AdminRespDTO.UnpaidBookFeeDTO::getAmount).sum());
            center.setUnpaidRate(center.getStudentCount() == 0 ? 0 : (double) unpaid.size() / center.getStudentCount() * 100);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("centers", centers);
        result.put("unpaidStudents", unpaidStudents);
        return result;
    }

    // ===== 입탈퇴 조회 =====
    private static final String[][] MC_SUBJECTS = {{"book", "북스쿨"}, {"han", "한스쿨"}, {"hoho", "호호스쿨"}};
    // 센터별 선생님 칸 순서 (목록에 없는 선생님은 뒤에 이름순)
    // shortcut: 센터별 하드코딩, 센터가 늘어 관리가 번거로워지면 DB 설정으로 이동
    // 과목별 목록 : 칸 순서 + 시간표에 없는 학생의 입회·탈퇴를 어느 스쿨로 넣을지(선생님 과목)에도 사용
    private static final Map<String, Map<String, List<String>>> MC_TEACHER_SUBJECTS = Map.of(
            "PUS002", Map.of( // 남천점
                    "han", List.of("이정애", "김도경", "장혜진"),
                    "book", List.of("남지영", "이경은", "노수연", "성해랑"),
                    "hoho", List.of("이경진", "장혜진")));
    // 과목 구분 없이 순서만 정한 센터
    private static final Map<String, List<String>> MC_TEACHER_ORDER = Map.of(
            "DAE001", List.of("황아름", "권은미", "송민경")); // 월성점

    // 센터·과목별 선생님 칸 순서
    private static List<String> mcOrder(String centerCode, String subject) {
        Map<String, List<String>> bySubject = MC_TEACHER_SUBJECTS.get(centerCode);
        if (bySubject != null) return bySubject.getOrDefault(subject, List.of());
        return MC_TEACHER_ORDER.getOrDefault(centerCode, List.of());
    }
    // 집계에서 아예 빼는 선생님 (전체총원·요약에도 포함 안 됨)
    private static final Set<String> MC_EXCLUDED_TEACHERS = Set.of("미지정", "조정로");
    private static final String[][] MC_KINDS = {
            {"join", "입회"}, {"withdraw", "탈퇴"}, {"in", "전입"}, {"out", "전출"}, {"graduate", "졸업"}, {"member", "회원"}};

    // 회원 현황 gb → 입탈퇴 조회 kind
    private static final Map<String, String> MC_STAT_KIND = Map.of(
            "entry", "join", "inactive", "withdraw", "move_in", "in", "move_out", "out");
    // 학년으로 과목을 나누는 센터 (남천점) : 초2 이하 호호, 초3 이상 반 종류(한·북). 그 외 센터는 시간표 subject
    private static final Set<String> MC_GRADE_RULE_CENTERS = Set.of("PUS002");

    // 시간표에 든 학생 1명의 과목 : 남천점은 학년, 기본은 시간표 subject(han/book/hoho, 비면 반 종류)
    private static String mcSubjectOf(boolean gradeRule, boolean hohoGrade, String tableSubject, String classType) {
        if (gradeRule && hohoGrade) return "hoho";
        if (!gradeRule && tableSubject != null && !tableSubject.isBlank()) return tableSubject;
        return "1".equals(classType) ? "han" : "book";
    }

    // 선생님 1명·1개월 회원 집계 : 한·북 = 강좌(수업 횟수 합 × 0.25), 호호 = 총원(학생 수)
    private static final class McMonth {
        final Map<String, Double> weeks = new HashMap<>();
        final Set<String> hohoStudents = new HashSet<>();
        final Map<String, String> studentSubject = new HashMap<>(); // 입회·탈퇴 등 과목 판정용

        void add(String studentId, String subject, int week) {
            if (studentId == null) return; // 빈 시간표 : 그 달 시간표 존재 표시만
            studentSubject.putIfAbsent(studentId, subject);
            if ("hoho".equals(subject)) hohoStudents.add(studentId);
            else weeks.merge(subject, week * 0.25, Double::sum);
        }

        Map<String, Double> members() {
            Map<String, Double> m = new HashMap<>(weeks);
            if (!hohoStudents.isEmpty()) m.put("hoho", (double) hohoStudents.size());
            return m;
        }
    }

    /**
     * 입탈퇴 조회 (빠른 방식) : 시간표 조회 「회원 현황」 쿼리와 같은 조건을 1년치로 한 번에 조회.
     * 회원 = 한·북은 강좌(수업 횟수 합 × 0.25), 호호는 총원(학생 수). 결과가 화면과 같은지는 verifyMemberChange 로 확인.
     * 과목 : 기본은 시간표 subject, 남천점(MC_GRADE_RULE_CENTERS)만 학년 기준.
     */
    @Transactional(readOnly = true)
    public List<AdminRespDTO.MemberChangeSubject> findMemberChange(String centerCode, String year) {
        return findMemberChange(centerCode, year, null);
    }

    /** userCodes : 선택한 선생님만 (null·빈 값이면 전체). 상담문의도 선택한 선생님이 받은 상담만 */
    @Transactional(readOnly = true)
    public List<AdminRespDTO.MemberChangeSubject> findMemberChange(String centerCode, String year, Set<String> userCodes) {
        return aggregateMemberChange(mcFastRows(centerCode, year, userCodes == null || userCodes.isEmpty() ? null : userCodes),
                centerCode);
    }

    // 조회 화면 선생님 선택 목록 : 이 센터에 그 해 시간표가 있는 선생님 (제외 선생님 빼고, 센터 지정 순서(한·북·호호) → 이름순)
    @Transactional(readOnly = true)
    public List<Map<String, String>> findMemberChangeTeacherList(String centerCode, String year) {
        List<String> order = new ArrayList<>();
        for (String key : new String[]{"han", "book", "hoho"})
            mcOrder(centerCode, key).stream().filter(t -> !order.contains(t)).forEach(order::add);
        // 타입을 명시 : VS Code(Eclipse 컴파일러)가 thenComparing 람다 타입을 추론하지 못함
        Comparator<Map<String, String>> byOrder = Comparator.comparingInt(
                t -> order.contains(t.get("userName")) ? order.indexOf(t.get("userName")) : order.size());
        Comparator<Map<String, String>> byName = Comparator.comparing(t -> t.get("userName"));
        return adminRepository.findMemberChangeTeachers(centerCode, year).stream()
                .filter(t -> !MC_EXCLUDED_TEACHERS.contains(t.get("userName")))
                .sorted(byOrder.thenComparing(byName))
                .toList();
    }

    private List<AdminRespDTO.MemberChangeRowDTO> mcFastRows(String centerCode, String year, Set<String> userCodes) {
        boolean gradeRule = MC_GRADE_RULE_CENTERS.contains(centerCode);
        Set<String> hoho = gradeRule ? new HashSet<>(adminRepository.findHohoStudentIds(centerCode)) : Set.of();
        Set<String> graduates = new HashSet<>(adminRepository.findGraduateKeys(year));
        // 상담문의 : 전체면 센터 전체, 선생님을 고르면 그 선생님이 받은 상담만 (ERP_CONSULT.user_code)
        List<AdminRespDTO.MemberChangeRowDTO> rows = new ArrayList<>();
        for (AdminRespDTO.MemberChangeRowDTO c : adminRepository.findConsultRows(centerCode, year)) {
            if (userCodes != null && !userCodes.contains(c.getTeacherName())) continue;
            c.setTeacherName(null); // 상담은 선생님 칸이 아니라 과목별 상담문의 칸에 들어감
            rows.add(c);
        }
        Map<String, String> names = new HashMap<>();
        for (Map<String, String> t : adminRepository.findMemberChangeTeachers(centerCode, year))
            names.put(t.get("userCode"), t.get("userName"));

        // 선생님|월 → 회원 집계. 키가 있으면 그 달 시간표가 있는 것
        Map<String, McMonth> members = new TreeMap<>();
        for (Map<String, Object> r : adminRepository.findMcTimetableRows(centerCode, year)) {
            Object grade = r.get("grade");
            String subj = mcSubjectOf(gradeRule, grade != null && ((Number) grade).intValue() <= 12,
                    (String) r.get("subject"), r.get("classType") == null ? null : r.get("classType").toString());
            members.computeIfAbsent(r.get("userCode") + "|" + r.get("mm"), k -> new McMonth())
                    .add((String) r.get("studentId"), subj, ((Number) r.get("weeks")).intValue());
        }
        Map<String, List<String[]>> stats = new HashMap<>(); // 선생님|월 → [gb, studentId]
        for (Map<String, Object> r : adminRepository.findMcStatRows(centerCode, year))
            stats.computeIfAbsent(r.get("userCode") + "|" + r.get("ym").toString().substring(5, 7), k -> new ArrayList<>())
                    .add(new String[]{r.get("gb").toString(), String.valueOf(r.get("studentId"))});

        for (Map.Entry<String, McMonth> e : members.entrySet()) {
            String[] key = e.getKey().split("\\|");
            String name = names.get(key[0]);
            if (name == null || MC_EXCLUDED_TEACHERS.contains(name)) continue;
            if (userCodes != null && !userCodes.contains(key[0])) continue;
            mcAddTeacherMonth(rows, centerCode, Integer.parseInt(key[1]), year, name, e.getValue(),
                    stats.getOrDefault(e.getKey(), List.of()), gradeRule, hoho, graduates);
        }
        return rows;
    }

    // 선생님 1명·1개월 → 원시 행 (빠른 방식·화면 코드 방식 공통)
    // 입회·탈퇴·전입·전출 과목 : 그 달 그 선생님 시간표에서 학생이 속한 과목 → (남천점) 초2 이하면 호호 → 선생님 과목
    private static void mcAddTeacherMonth(List<AdminRespDTO.MemberChangeRowDTO> rows, String centerCode, int m,
                                          String year, String name, McMonth month, List<String[]> stats,
                                          boolean gradeRule, Set<String> hoho, Set<String> graduates) {
        Map<String, Double> memberBySubject = month.members();
        for (Map.Entry<String, Double> e : memberBySubject.entrySet())
            rows.add(mcRow(m, e.getKey(), name, "member", e.getValue()));
        // 선생님 과목 : 센터에 과목별 선생님 목록이 있으면(남천점) 그것, 없으면 그 달 학생이 가장 많은 과목
        Map<String, List<String>> bySubject = MC_TEACHER_SUBJECTS.getOrDefault(centerCode, Map.of());
        String teacherSubject = bySubject.getOrDefault("han", List.of()).contains(name) ? "han"
                : bySubject.getOrDefault("book", List.of()).contains(name) ? "book"
                : month.studentSubject.values().stream()
                        .collect(java.util.stream.Collectors.groupingBy(s -> s, java.util.stream.Collectors.counting()))
                        .entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("han");
        String ym = year + "-" + String.format("%02d", m);
        for (String[] stat : stats) {
            String kind = MC_STAT_KIND.get(stat[0]);
            if (kind == null) continue; // 1-3주 목록
            if ("withdraw".equals(kind) && graduates.contains(stat[1] + "|" + ym)) kind = "graduate";
            String subj = month.studentSubject.get(stat[1]);
            if (subj == null) subj = gradeRule && hoho.contains(stat[1]) ? "hoho" : teacherSubject;
            rows.add(mcRow(m, subj, name, kind, 1));
        }
    }

    /**
     * 검증 : 빠른 방식 결과와 시간표 조회 화면 코드(findTableViewWithStudents)를 선생님×월로 돌린 결과를 비교.
     * 다른 칸만 "과목 월 선생님 항목 : 빠른=x 화면=y" 로 돌려준다 (비어 있으면 일치).
     */
    public List<String> verifyMemberChange(String centerCode, String year) {
        List<AdminRespDTO.MemberChangeSubject> fast = findMemberChange(centerCode, year);
        List<AdminRespDTO.MemberChangeSubject> screen = aggregateMemberChange(mcScreenRows(centerCode, year), centerCode);
        List<String> diffs = new ArrayList<>();
        for (int b = 0; b < fast.size(); b++)
            for (int m = 0; m < 12; m++) {
                AdminRespDTO.MemberChangeMonth f = fast.get(b).months().get(m), sc = screen.get(b).months().get(m);
                Set<String> teachers = new TreeSet<>(f.cells().keySet());
                teachers.addAll(sc.cells().keySet());
                for (String t : teachers)
                    for (String[] k : MC_KINDS) {
                        double fv = f.cells().getOrDefault(t, Map.of()).getOrDefault(k[0], 0d);
                        double sv = sc.cells().getOrDefault(t, Map.of()).getOrDefault(k[0], 0d);
                        if (Math.abs(fv - sv) > 1e-9)
                            diffs.add(fast.get(b).title() + " " + (m + 1) + "월 " + t + " " + k[1] + " : 빠른=" + fv + " 화면=" + sv);
                    }
            }
        return diffs;
    }

    // 화면 코드 방식 (검증용) : 선생님×월마다 findTableViewWithStudents 를 6개씩 병렬 호출
    private List<AdminRespDTO.MemberChangeRowDTO> mcScreenRows(String centerCode, String year) {
        boolean gradeRule = MC_GRADE_RULE_CENTERS.contains(centerCode);
        Set<String> hoho = gradeRule ? new HashSet<>(adminRepository.findHohoStudentIds(centerCode)) : Set.of();
        Set<String> graduates = new HashSet<>(adminRepository.findGraduateKeys(year));
        List<AdminRespDTO.MemberChangeRowDTO> rows = new ArrayList<>();
        List<java.util.concurrent.Callable<List<AdminRespDTO.MemberChangeRowDTO>>> tasks = new ArrayList<>();
        for (Map<String, String> teacher : adminRepository.findMemberChangeTeachers(centerCode, year)) {
            if (MC_EXCLUDED_TEACHERS.contains(teacher.get("userName"))) continue;
            for (int m = 1; m <= 12; m++) {
                int month = m;
                tasks.add(() -> {
                    String mm = String.format("%02d", month);
                    ClassRespDTO.TimeTableViewRespDTO view =
                            classService.findTableViewWithStudents(year, mm, teacher.get("userCode"), centerCode);
                    List<AdminRespDTO.MemberChangeRowDTO> out = new ArrayList<>();
                    if (view.getTables().isEmpty()) return out;
                    McMonth acc = new McMonth();
                    for (ClassRespDTO.TimeTableDTO tt : view.getTables()) {
                        for (ClassRespDTO.TimeTableDTO.StudentDTO st : tt.getStudents()) {
                            if (st.getStudentId() == null || st.getStudentId().isEmpty()
                                    || "\u00A0".equals(st.getStudentName())) continue;
                            int week;
                            try {
                                week = Integer.parseInt(st.getWeek());
                            } catch (Exception e) {
                                week = 0;
                            }
                            acc.add(st.getStudentId(), mcSubjectOf(gradeRule, hoho.contains(st.getStudentId()),
                                    tt.getSubject(), tt.getClassType()), week);
                        }
                    }
                    List<String[]> stats = view.getStats().stream()
                            .map(st -> new String[]{st.getGb(), st.getStudentId()}).toList();
                    mcAddTeacherMonth(out, centerCode, month, year, teacher.get("userName"), acc, stats, gradeRule, hoho, graduates);
                    return out;
                });
            }
        }
        java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(6);
        try {
            for (java.util.concurrent.Future<List<AdminRespDTO.MemberChangeRowDTO>> f : pool.invokeAll(tasks))
                rows.addAll(f.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("검증이 중단되었습니다.", e);
        } catch (java.util.concurrent.ExecutionException e) {
            throw new IllegalStateException("검증 중 오류가 발생했습니다.", e.getCause());
        } finally {
            pool.shutdownNow();
        }
        return rows;
    }

    private static AdminRespDTO.MemberChangeRowDTO mcRow(int mm, String subject, String teacher, String kind, double val) {
        AdminRespDTO.MemberChangeRowDTO r = new AdminRespDTO.MemberChangeRowDTO();
        r.setMm(mm);
        r.setSubject(subject);
        r.setTeacherName(teacher);
        r.setKind(kind);
        r.setVal(val);
        return r;
    }

    // 원시 행 → 과목 블록(선생님 순서·제외 적용) × 12개월
    static List<AdminRespDTO.MemberChangeSubject> aggregateMemberChange(List<AdminRespDTO.MemberChangeRowDTO> rows,
                                                                        String centerCode) {
        List<AdminRespDTO.MemberChangeSubject> result = new ArrayList<>();

        for (String[] subject : MC_SUBJECTS) {
            List<AdminRespDTO.MemberChangeRowDTO> subjectRows = rows.stream()
                    .filter(r -> subject[0].equals(r.getSubject()) && r.getMm() != null)
                    .filter(r -> r.getVal() != 0) // 빈 시간표(회원 0)로 다른 과목에 빈 선생님 칸이 생기지 않게
                    .filter(r -> "consult".equals(r.getKind())
                            || (r.getTeacherName() != null && !MC_EXCLUDED_TEACHERS.contains(r.getTeacherName())))
                    .toList();
            List<String> order = mcOrder(centerCode, subject[0]);
            List<String> teachers = subjectRows.stream()
                    .filter(r -> !"consult".equals(r.getKind()))
                    .map(AdminRespDTO.MemberChangeRowDTO::getTeacherName)
                    .distinct()
                    .sorted(Comparator.comparingInt((String t) -> order.contains(t) ? order.indexOf(t) : order.size())
                            .thenComparing(Comparator.naturalOrder()))
                    .toList();

            List<AdminRespDTO.MemberChangeMonth> months = new ArrayList<>();
            for (int m = 1; m <= 12; m++) {
                double consult = 0, total = 0;
                Map<String, Map<String, Double>> cells = new LinkedHashMap<>();
                for (AdminRespDTO.MemberChangeRowDTO r : subjectRows) {
                    if (r.getMm() != m) continue;
                    if ("consult".equals(r.getKind())) {
                        consult += r.getVal();
                        continue;
                    }
                    cells.computeIfAbsent(r.getTeacherName(), k -> new HashMap<>()).merge(r.getKind(), r.getVal(), Double::sum);
                    if ("member".equals(r.getKind())) total += r.getVal();
                }
                months.add(new AdminRespDTO.MemberChangeMonth(m, consult, total, cells));
            }
            result.add(new AdminRespDTO.MemberChangeSubject(subject[0], subject[1], teachers, months));
        }
        return result;
    }

    // 입탈퇴 조회 "행사 or 이슈" : 월(1~12) → 내용
    @Transactional(readOnly = true)
    public Map<Integer, String> findMemberChangeEvents(String centerCode, String year) {
        Map<Integer, String> events = new TreeMap<>();
        try {
            for (Map<String, Object> r : adminRepository.findMemberChangeEvents(centerCode, year))
                events.put(((Number) r.get("mm")).intValue(), String.valueOf(r.get("content")));
        } catch (org.springframework.dao.DataAccessException e) {
            // 테이블 생성 전(sql/2026-11-16_erp_yearly_event.sql 미실행)에도 조회·엑셀은 동작하게
            log.warn("행사 or 이슈 조회 실패 (erp_yearly_event 테이블 확인): {}", e.getMessage());
        }
        return events;
    }

    // 입력하면 바로 저장, 빈 값이면 삭제
    @Transactional
    public void saveMemberChangeEvent(AdminReqDTO.MemberChangeEventDTO dto, String userCode) {
        if (dto.getCenterCode() == null || dto.getCenterCode().isBlank()
                || dto.getYear() == null || !dto.getYear().matches("\\d{4}")
                || dto.getMonth() < 1 || dto.getMonth() > 12)
            throw new IllegalArgumentException("센터·연도·월을 확인해주세요.");
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (content.length() > 100) throw new IllegalArgumentException("100자 이내로 입력해주세요.");
        if (content.isEmpty())
            adminRepository.deleteMemberChangeEvent(dto.getCenterCode(), dto.getYear(), dto.getMonth());
        else
            adminRepository.upsertMemberChangeEvent(dto.getCenterCode(), dto.getYear(), dto.getMonth(), content, userCode);
    }

    /**
     * 입탈퇴 조회 엑셀 : resources/excel/member-change-template.xlsx(원본 양식) 를 4시트로 나눠 값·수식을 채운다.
     * 1 전체 현황(양식 1~19행) / 2 한스쿨(양식 38행~) / 3 북스쿨(양식 20행~) / 4 호호스쿨(양식 56행~)
     * 스쿨 시트는 양식 블록을 복사해 맨 위로 올린 것. 선생님이 양식 칸 수(북·한 3명, 호호 2명)보다 많으면
     * 첫 선생님 칸 서식을 복사해 오른쪽으로 늘린다. 전체 현황의 회원 = 각 스쿨 시트 전체총원.
     */
    public void writeMemberChangeExcel(List<AdminRespDTO.MemberChangeSubject> report, Map<Integer, String> events,
                                       String centerName, String year, OutputStream out) throws IOException {
        try (InputStream in = new ClassPathResource("excel/member-change-template.xlsx").getInputStream();
             XSSFWorkbook wb = new XSSFWorkbook(in)) {
            String yy = year.length() == 4 ? year.substring(2) : year;
            int[] templateStart = {19, 37, 55};  // report 순서(북·한·호호)별 양식 블록 시작 행 (0-based)
            int[] templateGroups = {3, 3, 2};    // 양식에 서식이 잡힌 선생님 칸 수
            String[] sheetNames = new String[3];

            // 스쿨 시트 : 양식 복사 → 해당 블록만 남겨 맨 위로
            for (int b = 0; b < 3; b++) {
                sheetNames[b] = report.get(b).title() + " 현황";
                Sheet c = wb.cloneSheet(0);
                wb.setSheetName(wb.getSheetIndex(c), sheetNames[b]);
                mcKeepRows(c, templateStart[b], templateStart[b] + 16);
            }
            Sheet sum = wb.getSheetAt(0);
            wb.setSheetName(0, "전체 현황");
            mcKeepRows(sum, 0, 18);
            wb.setSheetOrder("한스쿨 현황", 1);
            wb.setSheetOrder("북스쿨 현황", 2);
            wb.setSheetOrder("호호스쿨 현황", 3);

            // 전체 현황 제목 병합은 표 너비(A~R)까지만
            for (int i = sum.getNumMergedRegions() - 1; i >= 0; i--)
                if (sum.getMergedRegion(i).isInRange(0, 0)) sum.removeMergedRegion(i);
            sum.addMergedRegion(new CellRangeAddress(0, 0, 0, 17));
            mcPut(sum, 0, 0, yy + "년 " + centerName + " 회원 변동");
            mcPut(sum, 18, 4, "1월 입회 제외 \n누적 입회");

            // [블록][월] → 요약표에서 쓸 셀 목록 (다른 시트 참조)
            List<List<String>> joinRefs = new ArrayList<>(), withdrawRefs = new ArrayList<>();

            for (int b = 0; b < 3; b++) {
                AdminRespDTO.MemberChangeSubject subject = report.get(b);
                List<String> teachers = subject.teachers();
                Sheet sh = wb.getSheet(sheetNames[b]);
                String ref = "'" + sheetNames[b] + "'!";
                int lastCol = 21;
                mcPut(sh, 0, 0, yy + "년 " + centerName + " " + subject.title() + " 회원 변동");

                for (int g = 0; g < teachers.size(); g++) {
                    int c0 = 4 + g * MC_KINDS.length;
                    if (g >= templateGroups[b]) {
                        for (int r = 2; r <= 16; r++)
                            for (int k = 0; k < MC_KINDS.length; k++)
                                mcCellAt(sh, r, c0 + k).setCellStyle(mcCellAt(sh, r, 4 + k).getCellStyle());
                        for (int k = 0; k < MC_KINDS.length; k++) {
                            mcPut(sh, 3, c0 + k, MC_KINDS[k][1]);
                            if (c0 + k > 21) sh.setColumnWidth(c0 + k, sh.getColumnWidth(4 + k));
                        }
                        sh.addMergedRegion(new CellRangeAddress(2, 2, c0, c0 + MC_KINDS.length - 1));
                    }
                    mcPut(sh, 2, c0, teachers.get(g));
                    lastCol = Math.max(lastCol, c0 + MC_KINDS.length - 1);
                }

                List<String> joins = new ArrayList<>(), withdraws = new ArrayList<>();
                for (int m = 1; m <= 12; m++) {
                    AdminRespDTO.MemberChangeMonth mon = subject.months().get(m - 1);
                    int r = 3 + m;
                    String row = String.valueOf(r + 1);
                    mcPut(sh, r, 2, mon.consult());
                    List<String> members = new ArrayList<>(), j = new ArrayList<>(), w = new ArrayList<>();
                    for (int g = 0; g < teachers.size(); g++) {
                        int c0 = 4 + g * MC_KINDS.length;
                        Map<String, Double> cell = mon.cells().getOrDefault(teachers.get(g), Map.of());
                        for (int k = 0; k < MC_KINDS.length; k++) {
                            double v = cell.getOrDefault(MC_KINDS[k][0], 0d);
                            // 양식처럼 전입·전출·졸업은 0 이면 빈칸
                            mcPut(sh, r, c0 + k, v == 0 && k >= 2 && k <= 4 ? null : v);
                        }
                        j.add(ref + mcCol(c0) + row);
                        w.add(ref + mcCol(c0 + 1) + row);
                        members.add(mcCol(c0 + 5) + row);
                    }
                    mcFx(sh, r, 3, members);
                    joins.add(mcSumOf(j));
                    withdraws.add(mcSumOf(w));
                }
                joinRefs.add(joins);
                withdrawRefs.add(withdraws);

                // 누적 (17행)
                mcFormula(sh, 16, 2, "SUM(C5:C16)");
                for (int g = 0; g < teachers.size(); g++)
                    for (int k = 0; k < MC_KINDS.length; k++) {
                        int c = 4 + g * MC_KINDS.length + k;
                        if ("member".equals(MC_KINDS[k][0])) mcPut(sh, 16, c, "ㅡ");
                        else mcFormula(sh, 16, c, "SUM(" + mcCol(c) + "5:" + mcCol(c) + "16)");
                    }
                wb.setPrintArea(wb.getSheetIndex(sh), 0, lastCol, 0, 16);
            }

            // 전체 현황 (5~16행) : 양식 열 순서 북/한/호호 = report 0/1/2
            for (int m = 1; m <= 12; m++) {
                int r = 3 + m;
                String row = String.valueOf(r + 1);
                List<String> consult = new ArrayList<>();
                for (int b = 0; b < 3; b++) {
                    String ref = "'" + sheetNames[b] + "'!";
                    consult.add(ref + "C" + row);
                    mcFormula(sum, r, 4 + b, joinRefs.get(b).get(m - 1));     // E,F,G 입회
                    mcFormula(sum, r, 9 + b, withdrawRefs.get(b).get(m - 1)); // J,K,L 탈퇴
                    mcFormula(sum, r, 12 + b * 2, ref + "D" + row);           // M,O,Q 회원 = 스쿨 전체총원
                }
                mcFx(sum, r, 2, consult);
                mcFormula(sum, r, 3, "M" + row + "+O" + row + "+Q" + row);
            }
            mcFormula(sum, 16, 2, "SUM(C5:C16)");
            for (int c : new int[]{4, 5, 6, 9, 10, 11})
                mcFormula(sum, 16, c, "SUM(" + mcCol(c) + "5:" + mcCol(c) + "16)");
            mcFormula(sum, 17, 3, "D16-D5");
            mcFormula(sum, 17, 6, "SUM(E17:G17)");
            mcFormula(sum, 17, 11, "SUM(J17:L17)");
            mcFormula(sum, 18, 6, "SUM(E6:G16)");
            wb.setPrintArea(0, 0, 17, 0, 18);

            // 행사 or 이슈 (A열, 4시트 모두 월 행이 5~16행)
            for (int i = 0; i < wb.getNumberOfSheets(); i++)
                for (int m = 1; m <= 12; m++)
                    mcPut(wb.getSheetAt(i), 3 + m, 0, events.get(m));

            // 인쇄 : 시트마다 A4 가로 1페이지에 맞춤 (가운데 정렬, 좁은 여백)
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet p = wb.getSheetAt(i);
                p.setFitToPage(true);
                p.setHorizontallyCenter(true);
                PrintSetup ps = p.getPrintSetup();
                ps.setPaperSize(PrintSetup.A4_PAPERSIZE);
                ps.setLandscape(true);
                ps.setFitWidth((short) 1);
                ps.setFitHeight((short) 1);
                p.setMargin(PageMargin.LEFT, 0.3);
                p.setMargin(PageMargin.RIGHT, 0.3);
                p.setMargin(PageMargin.TOP, 0.4);
                p.setMargin(PageMargin.BOTTOM, 0.4);
            }

            wb.setActiveSheet(0);
            wb.setSelectedTab(0);
            wb.setForceFormulaRecalculation(true);
            XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            wb.write(out);
        }
    }

    // 시트에서 [from, to] 행만 남기고 맨 위로 올린다 (병합 영역 포함)
    private static void mcKeepRows(Sheet sh, int from, int to) {
        for (int i = sh.getNumMergedRegions() - 1; i >= 0; i--) {
            CellRangeAddress m = sh.getMergedRegion(i);
            if (m.getFirstRow() < from || m.getLastRow() > to) sh.removeMergedRegion(i);
        }
        for (int r = sh.getLastRowNum(); r >= 0; r--) {
            Row row = sh.getRow(r);
            if (row != null && (r < from || r > to)) sh.removeRow(row);
        }
        if (from > 0) sh.shiftRows(from, to, -from);
    }

    private static Cell mcCellAt(Sheet sh, int r, int c) {
        Row row = sh.getRow(r) != null ? sh.getRow(r) : sh.createRow(r);
        return row.getCell(c) != null ? row.getCell(c) : row.createCell(c);
    }

    // 값 쓰기 (서식은 양식 그대로 유지)
    private static void mcPut(Sheet sh, int r, int c, Object v) {
        Cell cell = mcCellAt(sh, r, c);
        if (v instanceof Number n) cell.setCellValue(n.doubleValue());
        else if (v != null) cell.setCellValue(v.toString());
        else cell.setBlank();
    }

    private static void mcFormula(Sheet sh, int r, int c, String f) {
        mcCellAt(sh, r, c).setCellFormula(f);
    }

    // 셀 목록 합계 수식, 비어 있으면 0
    private static void mcFx(Sheet sh, int r, int c, List<String> refs) {
        mcFormula(sh, r, c, mcSumOf(refs));
    }

    private static String mcSumOf(List<String> refs) {
        return refs.isEmpty() ? "0" : "SUM(" + String.join(",", refs) + ")";
    }

    private static String mcCol(int c) {
        return CellReference.convertNumToColString(c);
    }
}
