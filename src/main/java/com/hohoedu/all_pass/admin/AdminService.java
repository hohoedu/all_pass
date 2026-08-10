package com.hohoedu.all_pass.admin;

import com.hohoedu.all_pass.admin._dto.AdminReqDTO;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryEbookRepository;
import com.hohoedu.all_pass.secondary.repository.SecondaryLogisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final AdminRepository adminRepository;
    private final SecondaryLogisticsRepository secondaryLogisticsRepository;
    private final SecondaryEbookRepository secondaryEbookRepository;

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
                    AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO m =
                            new AdminReqDTO.PersonSettingDTO.EbookClassDTO.EbookMonthDTO();
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
                .orElseThrow(() -> new IllegalArgumentException("유곡점 저장 실패 - " + SECONDARY_BASE_CLASS_KEY + " 반 데이터가 없습니다."));

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
            return findSecondaryKeycodeList(req);
        }

        List<AdminRespDTO.KeycodeDTO> list =
                adminRepository.selectKeycodeList(req.getCenterCode(), req.getYear(), req.getMonth());

        // 교재 코드는 지점과 무관하게 같은 체계이므로, 매핑을 역방향으로 읽어 Ggubun / ocode 를 채운다
        Map<String, Map<String, String>> mapByClassKey =
                adminRepository.selectSecondaryClassMap(SECONDARY_CENTER_CODE).stream()
                        .collect(Collectors.toMap(m -> m.get("classKey"), m -> m, (a, b) -> a));

        list.forEach(d -> {
            Map<String, String> m = mapByClassKey.get(d.getClassKey());
            if (m == null) return;
            d.setGgubun(m.get("extCode"));
            d.setOcode(m.get("ocode"));
        });

        return list;
    }

    /**
     * 유곡점(ULS001)은 시간표가 외부 DB(hohosc_DailyTime)에 있고 class_type 구분이 없다.
     * 교재 코드(ggubun)를 우리 class_key 로 옮기면 교재명/타입/호수 코드는 우리 코드 테이블에서 그대로 따라온다.
     */
    private List<AdminRespDTO.KeycodeDTO> findSecondaryKeycodeList(AdminReqDTO.KeycodeFindDTO req) {

        List<SecondaryDTO.KeycodeRawDTO> raws =
                secondaryEbookRepository.findKeycodeList(req.getYear(), req.getMonth());

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
            if (raw == null || raw.getGgubun() == null) continue;

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
            dto.setLevelUnit(raw.getMgubun() != null && raw.getMgubun().startsWith("2"));
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