package com.hohoedu.all_pass.class_instance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.hohoedu.all_pass._core.firebase.FcmDTO;
import com.hohoedu.all_pass._core.firebase.FcmService;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppReqDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import com.hohoedu.all_pass.class_instance.model.*;
import com.hohoedu.all_pass.manage._dto.ManageReqDTO;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.manage.repository.ManageRepository;
import com.hohoedu.all_pass.notice._dto.web.NoticeReqDTO;
import com.hohoedu.all_pass.notice.repository.NoticeRepository;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.user.User;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.vo.Constants;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.AddStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassMonthlyScoreDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.UpdateRemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.UpdateRemedialDateDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RecordStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.class_instance.repository.ClassCodeJpaRepository;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import com.hohoedu.all_pass.class_instance.repository.UnitCodeJpaRepository;
import com.hohoedu.all_pass._core.handler.exception.Exception400;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.student.model.TeacherAssign;
import com.hohoedu.all_pass.student.repository.GradeJpaRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.threeten.bp.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UnitCodeJpaRepository unitCodeJpaRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;
    private final ManageRepository manageRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final StudentService studentService;
    private final PaymentService paymentService;
    private final FcmService fcmService;
    private final NoticeRepository noticeRepository;

    public List<ClassRespDTO.MainClassSummaryDTO> getClassSummary(String centerCode, String userCode, String roleKey) {

        String year = DateConfig.currentYearMonth().get("currentYear");
        String month = DateConfig.currentYearMonth().get("currentMonth");
        String dayname = DateConfig.currentYearMonth().get("currentDayName");

        List<ClassRespDTO.MainClassSummaryDTO> responseDTO = classRepository.findClassSummary(year, month, dayname,
                userCode, centerCode, roleKey);

        try {
            if (responseDTO != null && !responseDTO.isEmpty()) {

                for (ClassRespDTO.MainClassSummaryDTO dto : responseDTO) {

                    // 유효성 검사
                    if (dto == null || dto.getCountStudent() == null)
                        continue;

                    int currentCount;

                    try {
                        currentCount = Integer.parseInt(dto.getCountStudent());
                    } catch (NumberFormatException e) {
                        continue;
                    }

                    int maxCount = "COM".equalsIgnoreCase(dto.getClassKey()) ? 10 : 8;

                    String converted = currentCount + "/" + maxCount;

                    dto.setCountStudent(converted);
                }
            }
        } catch (Exception e) {
            log.error("수업 요약 countStudent 가공 중 오류 발생", e.getMessage());
        }

        return responseDTO;
    }

    public List<ClassRespDTO.MainRemedialSummaryDTO> getRemedialSummary(String centerCode) {
        String today = DateConfig.currentYearMonth().get("today");

        List<ClassRespDTO.MainRemedialSummaryDTO> responseDTO = classRepository.findRemedialSummary(today, centerCode);

        return responseDTO;
    }

    public ClassRespDTO.MainAbsentSummaryDTO getAbsentSummary(
            String centerCode, String userCode, String roleKey, String year, String month) {
        return classRepository.findAbsentSummary(centerCode, userCode, roleKey, year, month);
    }

    public void saveClassWeek(ClassReqDTO.WeekReqDTO dto, String centerCode) {

        List<ClassRespDTO.ClassWeekDTO> existing = classRepository.getClassWeek(dto.getYear(), dto.getMonth(),
                centerCode);

        for (int i = 1; i <= 4; i++) {

            ClassReqDTO.WeekReqDTO.WeekDetailDTO detail = dto.getWeek().get(String.valueOf(i));
            if (detail == null)
                continue;

            String weekCode = "ju_" + i;

            ClassReqDTO.SetWeekDTO set = new ClassReqDTO.SetWeekDTO();
            set.setYear(dto.getYear());
            set.setMonth(dto.getMonth());
            set.setWeek(weekCode);
            set.setMon(detail.getMon());
            set.setTue(detail.getTue());
            set.setWed(detail.getWed());
            set.setThu(detail.getThu());
            set.setFri(detail.getFri());
            set.setSat(detail.getSat());
            set.setSun(detail.getSun());
            set.setCenterCode(centerCode);

            boolean exists = existing.stream()
                    .anyMatch(e -> e.getWeek().equals(weekCode));

            if (exists) {
                classRepository.updateClassWeek(set);
            } else {
                classRepository.insertClassWeek(set);
            }
        }
    }

    public List<ClassRespDTO.ClassWeekDTO> getClassWeek(String year, String month, String centerCode) {
        return classRepository.getClassWeek(year, month, centerCode);
    }

    public void updateClassWeek(ClassReqDTO.SetWeekDTO dto, String centerCode) {
        dto.setCenterCode(centerCode);

        classRepository.updateClassWeek(dto);

    }

    // 수업 코드 테이블 조회 서비스 (시간표 등록)
    public List<ClassCode> findClassCode() {
        List<ClassCode> classCodes = classCodeJpaRepository.findAll();

        return classCodes.stream()
                .filter(c -> !Set.of("HL").contains(c.getClassKey()))
                .toList();
    }

    public List<UnitCode> findHanLevelCode() {
        List<UnitCode> unitCodes = classRepository.findHanLevelCode();
        return unitCodes;
    }

    public List<ClassCode> findClassCodeExcludeMid() {
        List<ClassCode> classCodes = classRepository.findClassListExcludeMid();
        return classCodes;
    }

    // 수업 코드 - 유닛 매핑 테이블 조회 서비스 (시간표 등록)
    public Map<String, List<ClassRespDTO.ClassUnitDTO>> findClassUnits(String centerCode, String year, String month) {

        Map<String, List<ClassRespDTO.ClassUnitDTO>> result = new HashMap<>();

        List<ClassRespDTO.ClassUnitDTO> baseList = classRepository.findClassUnitMap();

        baseList.stream()
                .collect(Collectors.groupingBy(ClassRespDTO.ClassUnitDTO::getClassKey))
                .forEach(result::put);

        List<String> specialClasses = List.of("K", "M", "J");

        for (String classKey : specialClasses) {
            List<ClassRespDTO.ClassUnitDTO> personUnits = classRepository.findPersonUnit(centerCode, year, month,
                    classKey);

            if (!personUnits.isEmpty()) {
                result.put(classKey, personUnits);
            }
        }

        return result;
    }

    public Map<String, List<ClassRespDTO.ClassUnitDTO>> findClassUnitsOverPeriod(String centerCode, String startYear,
                                                                                 String startMonth, String endYear, String endMonth) {
        Map<String, List<ClassRespDTO.ClassUnitDTO>> result = new HashMap<>();

        List<ClassRespDTO.ClassUnitDTO> baseList = classRepository.findClassUnitMap();
        baseList.stream()
                .collect(Collectors.groupingBy(ClassRespDTO.ClassUnitDTO::getClassKey))
                .forEach(result::put);

        List<String> specialClasses = List.of("K", "M", "J");

        for (String classKey : specialClasses) {
            List<ClassRespDTO.ClassUnitDTO> units = classRepository.findPersonUnitsInRange(
                    centerCode,
                    startYear, startMonth,
                    endYear, "12",
                    classKey);

            if (!units.isEmpty()) {
                result.put(classKey, units);
            }
        }

        return result;
    }

    // 학년 및 연령 테이블 조회 서비스 (시간표 등록)
    public List<GradeCode> findGrade() {
        List<GradeCode> grades = gradeJpaRepository.findAll();
        return grades;
    }

    public List<TimeTableLabelDTO> getAllClassLabel(String userCode) {
        String yy = DateConfig.currentYearMonth().get("currentYear");
        String mm = DateConfig.currentYearMonth().get("currentMonth");
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCode(userCode, yy, mm, "0");
        return labels;
    }

    // 클래스 라벨 테이블 조회 (학생 정보 메인, 전입 전출 메인)
    public List<TimeTableLabelDTO> getClassLabel(String userCode) {
        String yy = DateConfig.currentYearMonth().get("currentYear");
        String mm = DateConfig.currentYearMonth().get("currentMonth");
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCode(userCode, yy, mm, "1");
        return labels;
    }

    // 수업 등록
    public String registerClass(ClassReqDTO.ClassRegisterDTO classReqDTO) {

        TimeTableDTO timeTable = classRepository.existsByYearAndMonthAndPeriodNo(
                classReqDTO.getPeriodNo(),
                classReqDTO.getYy(),
                classReqDTO.getMm(),
                classReqDTO.getDayname(),
                classReqDTO.getUserCode());

        String label = createTimeTableLabel(classReqDTO);

        if (timeTable == null) {
            String timeTableKey = UUID.randomUUID().toString();
            System.out.println("timeTableKey = " + timeTableKey);
            LocalDateTime dateTime = LocalDateTime.now();
            String ym = dateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
            TimeTableCode entity = TimeTableCode.builder()
                    .timeTableKey(timeTableKey)
                    .timeTableLabel(label)
                    .timeTableYm(ym)
                    .build();

            classRepository.createTimeTableKey(entity);

            classReqDTO.setTimeTableKey(timeTableKey);

            classRepository.registerClass(classReqDTO);

            return autoRegisterOrder(classReqDTO.getUserCode(), classReqDTO.getCenterCode(),
                    classReqDTO.getYy(), classReqDTO.getMm()); //
        } else {

            int result = classRepository.updateClass(classReqDTO, timeTable.getTimeTableKey(),
                    classReqDTO.getUserCode());

            classRepository.updateLabel(label, timeTable.getTimeTableKey());

            if (result <= 0) {
                return "fail-update";
            }

            return autoRegisterOrder(classReqDTO.getUserCode(), classReqDTO.getCenterCode(),
                    classReqDTO.getYy(), classReqDTO.getMm()); //
        }
    }

    private String autoRegisterOrder(String userCode, String centerCode, String yy, String mm) {

        // 대상 월(yy/mm)의 자동 주문 마감일 = 전달 deadlineDay일
        // 마감일이 지난 달은 자동 주문 안 됨
        String deadlineDay = manageRepository.findOrderDeadline(centerCode);
        if (deadlineDay != null) {
            LocalDate today = LocalDate.now();
            LocalDate targetFirstDay = LocalDate.of(Integer.parseInt(yy), Integer.parseInt(mm), 1);
            LocalDate orderDeadline = targetFirstDay.minusMonths(1).withDayOfMonth(Integer.parseInt(deadlineDay));

            if (today.isAfter(orderDeadline)) {
                // 이번 달에 마감된 경우(방금 마감) → 안내 메시지
                if (today.getYear() == orderDeadline.getYear() && today.getMonthValue() == orderDeadline.getMonthValue()) {
                    return "마감일이 지났습니다. 수량 변동이 있다면 추가 주문 / 반품을 이용해주세요.";
                }
                // 이미 지난 달 이전에 마감된 시간표 → 조용히 스킵
                return null;
            }
        }

        // 시간표 기준 base_count 조회
        List<ManageRespDTO.BasicOrderListDTO> basicList =
                manageRepository.findBasicOrderList(centerCode, userCode, yy, mm);

        if (basicList.isEmpty()) return null;

        // DTO 변환
        List<ManageReqDTO.InsertOrderHistoryDTO.InsertOrder> insertOrders = basicList.stream()
                .map(b -> {
                    ManageReqDTO.InsertOrderHistoryDTO.InsertOrder order =
                            new ManageReqDTO.InsertOrderHistoryDTO.InsertOrder();
                    order.setClassKey(b.getClassKey());
                    order.setUnitKey(b.getUnitKey());
                    order.setBaseCount(b.getBaseCount());
                    order.setTotalCount(b.getBaseCount());
                    order.setAddCount(0);
                    return order;
                })
                .collect(Collectors.toList());

        ManageReqDTO.InsertOrderHistoryDTO orderDto = new ManageReqDTO.InsertOrderHistoryDTO();
        orderDto.setCenterCode(centerCode);
        orderDto.setUserCode(userCode);
        orderDto.setYy(yy);
        orderDto.setMm(mm);
        orderDto.setInsertOrders(insertOrders);

        manageRepository.deleteOrderByCondition(userCode, centerCode, yy, mm);
        manageRepository.insertOrder(orderDto);
        manageRepository.insertOrderHistory(orderDto);

        return null; // 정상 처리
    }

    private String createTimeTableLabel(ClassReqDTO.ClassRegisterDTO dto) {
        ClassCode classCode = classCodeJpaRepository.findByClassKey(dto.getClassKey()).orElseThrow();
        String dayName = Constants.DAY_NAME_MAP.getOrDefault(dto.getDayname(), dto.getDayname());
        String label = "";
        if ("COM".equals(dto.getClassKey())) {
            label = String.format("%s %s ~ %s %s",
                    dayName,
                    dto.getStartTime(),
                    dto.getEndTime(),
                    classCode.getClassName());

        } else {
            UnitCode unitCode = unitCodeJpaRepository.findByUnitKey(dto.getUnitKey()).orElseThrow();
            label = String.format("%s %s ~ %s %s %s",

                    dayName,
                    dto.getStartTime(),
                    dto.getEndTime(),
                    classCode.getClassName(),
                    unitCode.getUnitName());
        }

        return label;

    }

    public List<TimeTableDTO> findTimeTableWithStudents(String userCode, String year, String month) {
        String ym = year + "-" + month;

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, year, month);

        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = classRepository
                    .findStudentsByTimeTableKey(userCode, tt.getTimeTableKey(), ym);

            tt.setStudents(students);
        }
        return tables;
    }

    public ClassRespDTO.TimeTableViewRespDTO findTableViewWithStudents(String year, String month, String userCode,
                                                                       String centerCode) {

        String ym = year + "-" + month;

        // 1. 시간표 조회 (1번)
        List<ClassRespDTO.TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, year, month);
        if (tables.isEmpty()) {
            return new ClassRespDTO.TimeTableViewRespDTO(tables, List.of(), 0L, 0.0);
        }

        // 2. 전체 학생 한 번에 조회 (1번)
        List<String> keys = tables.stream()
                .map(ClassRespDTO.TimeTableDTO::getTimeTableKey)
                .collect(Collectors.toList());

        List<ClassRespDTO.TimeTableDTO.StudentDTO> allStudents = classRepository.findStudentsByTimeTables(keys,
                userCode, ym);

        // 3. timeTableKey 기준으로 그룹핑
        Map<String, List<ClassRespDTO.TimeTableDTO.StudentDTO>> studentMap = allStudents.stream()
                .collect(Collectors.groupingBy(ClassRespDTO.TimeTableDTO.StudentDTO::getTimeTableKey));

        // 4. 시간표에 학생 세팅 + 빈 자리 패딩
        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = new ArrayList<>(
                    studentMap.getOrDefault(tt.getTimeTableKey(), List.of()));

            while (students.size() < 8) {
                ClassRespDTO.TimeTableDTO.StudentDTO empty = new ClassRespDTO.TimeTableDTO.StudentDTO();
                empty.setStudentName("\u00A0");
                students.add(empty);
            }
            tt.setStudents(students);
        }

        // 5. studentStat 조회 (1번)
        List<ClassRespDTO.StudentStatRespDTO> studentStat = classRepository.findStudentStat(userCode, ym, centerCode);

        // 6. 통계 계산
        Long totalStudentsLong = tables.stream()
                .flatMap(t -> t.getStudents().stream())
                .filter(s -> s.getStudentId() != null && !s.getStudentId().isEmpty())
                .filter(s -> !"\u00A0".equals(s.getStudentName()))
                .map(ClassRespDTO.TimeTableDTO.StudentDTO::getStudentId)
                .distinct()
                .count();
        log.info(totalStudentsLong.toString());

        double totalStudentsDouble = tables.stream()
                .flatMap(t -> t.getStudents().stream())
                .filter(s -> s.getStudentId() != null && !s.getStudentId().isEmpty()
                        && !"\u00A0".equals(s.getStudentName()))
                .collect(Collectors.groupingBy(
                        ClassRespDTO.TimeTableDTO.StudentDTO::getStudentId,
                        Collectors.summingInt(s -> {
                            try {
                                return Integer.parseInt(s.getWeek());
                            } catch (Exception e) {
                                return 0;
                            }
                        })))
                .values().stream()
                .mapToDouble(weekSum -> weekSum * 0.25)
                .sum();

        log.info(totalStudentsLong.toString());

        return new ClassRespDTO.TimeTableViewRespDTO(tables, studentStat, totalStudentsLong, totalStudentsDouble);
    }

    // 학생 수업 등록
    public boolean addStudent(AddStudentDTO dto) {
        int duplicated = classRepository.existsSameClassType(dto);

        if (duplicated > 0) {
            throw new RuntimeException("이미 동일한 클래스 타입의 수업을 수강 중입니다.");
        }

        int count = classRepository.countByTimeTableKey(dto.getTimeTableKey());
        if (count >= 10)
            return false;

        classRepository.addStudent(dto);
        return true;
    }

    @Transactional
    public String registerStudentFullProcess(AddStudentDTO dto, String userCode, String centerCode, boolean skipAutoOrder) {

        // 시간표 기본 정보 먼저 조회
        ClassRespDTO.BasicTimeTableInfo info = classRepository.findBasicTimeTableInfo(dto.getTimeTableKey(), centerCode);

        // 전입 pending 체크
        TeacherAssign assign = studentService.getTeacherAssign(dto.getStudentId());
        if (assign != null) {
            String classType = info.getClassType();
            boolean isPendingHan = info.getTeacherCode().equals(assign.getPendingHanTeacher());
            boolean isPendingBook = info.getTeacherCode().equals(assign.getPendingBookTeacher());

            // 전입 대기 중인 학생을 전입 선생님이 아닌 다른 선생님이 추가하려는 경우 차단 (핑퐁 방지)
            boolean hasPendingHan = assign.getPendingHanTeacher() != null;
            boolean hasPendingBook = assign.getPendingBookTeacher() != null;
            if (("1".equals(classType) && hasPendingHan && !isPendingHan)
                    || ("2".equals(classType) && hasPendingBook && !isPendingBook)) {
                throw new Exception400("전입/전출 대기 중인 학생입니다.\n전입/전출 취소 후 재등록해주세요.");
            }

            if (isPendingHan || isPendingBook) {
                boolean classTypeMatch = ("1".equals(classType) && isPendingHan)
                        || ("2".equals(classType) && isPendingBook);

                if (!classTypeMatch) {
                    String typeName = "1".equals(classType) ? "한자" : "독서";
                    throw new Exception400("해당 학생의 " + typeName + " 수업은 전입 대상이 아닙니다.");
                }

                // 전출 선생님 시간표에서 자동 제거
                StudentWebRespDTO.TransferTimeTableInfoDTO transferInfo = classRepository.findTimeTableKeyByStudentId(
                        dto.getStudentId(), classType, dto.getYy(), dto.getMm());
                if (transferInfo != null) {
                    classRepository.deleteByKeyAndStudentId(transferInfo.getTimeTableKey(), dto.getStudentId());
                    paymentService.deleteDetail(transferInfo.getTimeTableKey(), dto.getStudentId());
                }
            }
        }

        // 히스토리 먼저 확인
        boolean restored = classRepository.existsAssignHistory(dto.getTimeTableKey(), dto.getStudentId()) > 0;

        if (restored) {
            // 정원 체크만 하고 히스토리에서 복구 (assign 포함)
            int count = classRepository.countByTimeTableKey(dto.getTimeTableKey());
            if (count >= 10)
                throw new IllegalStateException("정원 초과");

            classRepository.restoreStudent(dto.getTimeTableKey(), dto.getStudentId(), dto.getWeekNo());

        } else {
            // 신규 등록 기존 로직 그대로
            boolean success = addStudent(dto);
            if (!success)
                throw new IllegalStateException("정원 초과");

            classRepository.insertMonthlyScore(dto.getStudentId(), dto.getYy(), dto.getMm(), dto.getTimeTableKey());
            classRepository.createAttendance(dto.getStudentId(), dto.getTimeTableKey(), centerCode, dto.getYy(),
                    dto.getMm());
        }

        // 공통 실행
        studentService.assignTeacher(dto.getStudentId(), info);

        ClassRespDTO.ClassInfoDTO classInfo = findClassInfoByTimeTableKeyAndStudentId(dto.getTimeTableKey(),
                dto.getStudentId(), centerCode);

        Integer baseFee = classInfo.getClassFee();
        if (baseFee == null)
            throw new IllegalArgumentException("설정된 교육비가 없습니다.");

        int weekNo = Integer.parseInt(dto.getWeekNo());
        BigDecimal result = BigDecimal.valueOf(baseFee)
                .multiply(BigDecimal.valueOf(weekNo))
                .divide(BigDecimal.valueOf(4), 0, RoundingMode.DOWN);
        classInfo.setClassFee(result.intValue());

        if (!classInfo.getClassKey().equals("HL")) {
            String paymentKey = paymentService.createPayment(dto.getStudentId(), dto.getYy(), dto.getMm(), centerCode,
                    userCode);
            paymentService.createPaymentDetail(paymentKey, classInfo, userCode);
            paymentService.processPresetPaymentIfExists(dto.getStudentId(), paymentKey, classInfo.getClassFee(),
                    userCode, centerCode, dto.getYy(), dto.getMm());
        }

        if (!skipAutoOrder) {
          return  autoRegisterOrder(info.getTeacherCode(), centerCode, dto.getYy(), dto.getMm());
        }
        return null;
    }

    @Transactional
    public String copyLastMonthTimeTableAndStudents(String userCode, String centerCode, String year, String month) {

        int count = classRepository.existsTimeTable(userCode, year, month);
        if (count > 0) throw new RuntimeException("이번 달 시간표 등록 내역이 있습니다.");

        Map<String, String> req = Map.of("year", year, "month", month);
        List<TimeTableDTO> lastTables = getLastTimeTable(userCode, req);
        if (lastTables.isEmpty()) return null;

        Map<String, String> keyMap = new HashMap<>();

        for (TimeTableDTO old : lastTables) {

            ClassReqDTO.ClassRegisterDTO dto = new ClassReqDTO.ClassRegisterDTO();
            dto.setYy(year);
            dto.setMm(month);
            dto.setDayname(old.getDayname());
            dto.setPeriodNo(old.getPeriodNo());
            dto.setStartTime(old.getStartTime());
            dto.setEndTime(old.getEndTime());
            dto.setGradeKey(old.getGradeKey());
            dto.setUserCode(userCode);
            dto.setCenterCode(centerCode);

            String classKey = old.getClassKey();
            String unitKey = old.getUnitKey();

            if (Constants.UNIT_INCREMENT_BOOK_CLASS_KEYS.contains(classKey)) {
                String prefix = unitKey.replaceAll("[0-9]", "");
                int prevMonth = Integer.parseInt(unitKey.replaceAll("[^0-9]", ""));
                boolean isRollover = (prevMonth == 12);
                int newMonth = isRollover ? 1 : prevMonth + 1;

                dto.setUnitKey(prefix + String.format("%02d", newMonth));
                dto.setClassKey(isRollover
                        ? Constants.CLASS_KEY_ROLLOVER.getOrDefault(classKey, classKey)
                        : classKey);

            } else if (Constants.UNIT_INCREMENT_HAN_CLASS_KEYS.contains(classKey)
                    && unitKey != null
                    && !unitKey.startsWith("L")) {
                Map<String, Object> next = resolveNextUnit(classKey, unitKey);

                if (next != null) {
                    dto.setClassKey(resolveNextClassKey(classKey, next));
                    dto.setUnitKey((String) next.get("unit_key"));
                } else {
                    dto.setClassKey(classKey);
                    dto.setUnitKey(unitKey);
                }

            } else if (Constants.PERSON_YEAR_CLASS_KEYS.contains(classKey)) {
                List<ClassRespDTO.ClassUnitDTO> personUnits =
                        classRepository.findPersonUnit(centerCode, year, month, classKey);

                if (!personUnits.isEmpty()) {
                    dto.setUnitKey(personUnits.get(0).getUnitKey());
                } else {
                    dto.setUnitKey(unitKey);
                }
                dto.setClassKey(classKey);

            } else {
                dto.setClassKey(classKey);
                dto.setUnitKey(unitKey);
            }

            registerClass(dto);
            keyMap.put(old.getTimeTableKey(), dto.getTimeTableKey());
        }

        for (TimeTableDTO old : lastTables) {
            String newTimeTableKey = keyMap.get(old.getTimeTableKey());
            if (newTimeTableKey == null) continue;

            for (TimeTableDTO.StudentDTO stu : old.getStudents()) {
                ClassReqDTO.AddStudentDTO addDto = new ClassReqDTO.AddStudentDTO();
                addDto.setTimeTableKey(newTimeTableKey);
                addDto.setStudentId(stu.getStudentId());
                addDto.setWeekNo("4");
                addDto.setYy(year);
                addDto.setMm(month);

                registerStudentFullProcess(addDto, userCode, centerCode, true);
            }
        }
       return autoRegisterOrder(userCode, centerCode, year, month);
    }

    @Transactional
    public void copyTimeTableToTeacher(ClassReqDTO.CopyToTeacherDTO req, String centerCode) {

        int count = classRepository.existsTimeTable(req.getToUserCode(), req.getToYy(), req.getToMm());
        if (count > 0) throw new RuntimeException("해당 월에는 이미 시간표가 존재합니다.");

        List<TimeTableDTO> rows = classRepository.findTimeTableBasic(req.getFromUserCode(), req.getFromYy(), req.getFromMm());
        if (rows.isEmpty()) throw new RuntimeException("가져올 시간표가 없습니다.");

        for (TimeTableDTO old : rows) {
            ClassReqDTO.ClassRegisterDTO dto = new ClassReqDTO.ClassRegisterDTO();
            dto.setYy(req.getToYy());
            dto.setMm(req.getToMm());
            dto.setDayname(old.getDayname());
            dto.setPeriodNo(old.getPeriodNo());
            dto.setStartTime(old.getStartTime());
            dto.setEndTime(old.getEndTime());
            dto.setClassKey(old.getClassKey());
            dto.setUnitKey(old.getUnitKey());
            dto.setGradeKey(old.getGradeKey());
            dto.setUserCode(req.getToUserCode());
            dto.setCenterCode(centerCode);

            registerClass(dto);
        }
    }

    private String resolveNextClassKey(String classKey, Map<String, Object> next) {
        String nextClassKey = (String) next.get("class_key");
        boolean isNE = Constants.NE_TO_E.containsKey(classKey);
        if (isNE && Constants.E_TO_NE.containsKey(nextClassKey)) {
            return Constants.E_TO_NE.get(nextClassKey);
        }
        return nextClassKey;
    }

    private Map<String, Object> resolveNextUnit(String classKey, String unitKey) {
        boolean isNE = Constants.NE_TO_E.containsKey(classKey);
        String lookupKey = isNE ? Constants.NE_TO_E.get(classKey) : classKey;

        Map<String, Object> current = classRepository.findUnitByClassAndUnit(lookupKey, unitKey);
        if (current == null) return null;

        int nextId = ((Number) current.get("id")).intValue() + 1;
        return classRepository.findNextUnitById(nextId);
    }

    public ClassRespDTO.ClassInfoDTO findClassInfoByTimeTableKeyAndStudentId(String timeTableKey, String studentId,
                                                                             String centerCode) {
        ClassRespDTO.ClassInfoDTO classInfo = classRepository.findClassInfoByTimeTableKeyAndStudentId(timeTableKey,
                studentId, centerCode);
        return classInfo;
    }

    @Transactional
    public String deleteStudent(String timeTableKey, String studentId) {
        ClassRespDTO.TimeTableMetaDTO meta = classRepository.findTimeTableMeta(timeTableKey);
        classRepository.deleteByKeyAndStudentId(timeTableKey, studentId);
        paymentService.deleteDetail(timeTableKey, studentId);

        return autoRegisterOrder(meta.getUserCode(), meta.getCenterCode(), meta.getYy(), meta.getMm());
    }

    public List<TimeTableDTO> getLastTimeTable(String userCode, Map<String, String> req) {

        LocalDate selected = LocalDate.of(
                Integer.parseInt(req.get("year")),
                Integer.parseInt(req.get("month")),
                1);
        // 이전 달
        LocalDate prev = selected.minusMonths(1);

        String lastYear = String.valueOf(prev.getYear());
        String lastMonth = String.format("%02d", prev.getMonthValue());
        // List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode,
        // lastYear, lastMonth);
        List<TimeTableDTO> rows = classRepository.findTimeTableBasic(userCode, lastYear, lastMonth);
        if (rows.isEmpty()) {
            return rows;
        }

        List<String> timeTableKeys = rows.stream()
                .map(TimeTableDTO::getTimeTableKey)
                .distinct()
                .toList();

        List<TimeTableDTO.StudentDTO> students = classRepository.findTimeTableStudents(userCode, timeTableKeys);

        Map<String, List<TimeTableDTO.StudentDTO>> studentMap = students.stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO.StudentDTO::getTimeTableKey));

        rows.forEach(tt -> tt.setStudents(
                studentMap.getOrDefault(
                        tt.getTimeTableKey(),
                        new ArrayList<>())));

        return rows;
    }

    public List<TimeTableCode> findTimeTableCodeByUserCode(String userCode) {
        List<TimeTableCode> codes = classRepository.findTimeTableCodeByUserCode(userCode);
        return codes;

    }

    @Transactional
    public String deleteTimeTableRow(String timeTableKey, String userCode) {

        // 삭제 전 메타 조회 (삭제 후엔 조회 불가)
        ClassRespDTO.TimeTableMetaDTO meta = classRepository.findTimeTableMeta(timeTableKey);

        int result = classRepository.deleteTimeTableRow(timeTableKey, userCode);
        // ✅ 수업 삭제 후 주문 재계산 (해당 수업 학생들이 빠지므로)
        if (result != 0 && meta != null) {
            return autoRegisterOrder(meta.getUserCode(), meta.getCenterCode(), meta.getYy(), meta.getMm());
        }
        return null;
    }

    public List<ClassRespDTO.ComClassStudentDTO> findComClassStudentsByTimeTableKey(String timeTableKey,
                                                                                    String userCode) {
        List<ClassRespDTO.ComClassStudentDTO> students = classRepository
                .findComClassStudentsByTimeTableKey(timeTableKey, userCode);

        return students;
    }

    public List<ClassRespDTO.ComClassStudentDTO> findComClassStudentsByUserCode(String userCode, String yy, String mm) {
        List<ClassRespDTO.ComClassStudentDTO> students = classRepository.findComClassStudentsByUserCode(userCode, yy,
                mm);

        return students;
    }

    // TODO:종합반 수업 추가
    // public int updateTimeTableAssign(ClassReqDTO.AssignUpdateDTO dto, String
    // userCode, String centerCode) {
    //
    // String timeTableKey = dto.getTimeTableKey();
    // int result = 0;
    //
    // for (ClassReqDTO.AssignUpdateDTO.StudentInfo info : dto.getStudentInfos()) {
    //
    // int updated = classRepository.updateTimeTableAssign(
    // timeTableKey,
    // info.getStudentId(),
    // info.getClassKey(),
    // info.getUnitKey()
    // );
    // Integer hanFee = paymentRepository.findFeeByClassKey(info.getClassKey(),
    // centerCode);
    //
    // boolean existsClass =
    // studentRepository.existsStudentClass(info.getStudentId(), info.getYy(),
    // info.getMm()) > 0;
    //
    // TeacherAssign studentClass = TeacherAssign.builder()
    // .student(Student.builder().studentId(info.getStudentId()).build())
    // .assignHanClass(ClassCode.builder().classKey(info.getClassKey()).build())
    // .assignHanTeacher(User.builder().userCode(userCode).build())
    // .build();
    //
    // if (!existsClass) {
    // studentRepository.insertStudentClass(studentClass);
    // } else {
    // studentRepository.updateStudentClass(studentClass);
    // }
    //
    // String paymentKey =
    // paymentRepository.findLatestPaymentKeyByStudent(info.getStudentId(),
    // info.getYy(), info.getMm());
    //
    // if (paymentKey != null) {
    // PaymentDetail eduDetail = PaymentDetail.builder()
    // .payment(Payment.builder().paymentKey(paymentKey).build())
    // .amount(hanFee)
    // .classType("1") // 한자 수업
    // .itemType("edu")
    // .user(User.builder().userCode(userCode).build())
    // .build();
    // paymentRepository.createPaymentDetail(eduDetail);
    //
    // PaymentDetail bookDetail = PaymentDetail.builder()
    // .payment(Payment.builder().paymentKey(paymentKey).build())
    // .classType("1")
    // .itemType("book")
    // .user(User.builder().userCode(userCode).build())
    // .build();
    // paymentRepository.createPaymentDetail(bookDetail);
    //
    // paymentRepository.updateAmount(paymentKey);
    // }
    // if (updated > 0) {
    // result += updated;
    // }
    //
    // }
    //
    // return result;
    // }

    // ================ 수업 일지 서비스 =====================//
    public List<ClassRespDTO.RecordLabelDTO> getTimeTableByUserCode(String dateStr, String dayName, String userCode,
                                                                    String centerCode) {
        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);

        // ✅ 현월 → 전월 → 익월 순으로 매칭 확인
        java.time.LocalDate[] candidates = {
                date,
                date.minusMonths(1),
                date.plusMonths(1)
        };

        for (java.time.LocalDate candidate : candidates) {
            String yy = String.valueOf(candidate.getYear());
            String mm = String.format("%02d", candidate.getMonthValue());

            List<ClassRespDTO.ClassWeekDTO> weeks = classRepository.getClassWeek(yy, mm, centerCode);
            boolean isMatch = weeks.stream()
                    .anyMatch(w -> isDateMatch(w.getMon(), date) || isDateMatch(w.getTue(), date) ||
                            isDateMatch(w.getWed(), date) || isDateMatch(w.getThu(), date) ||
                            isDateMatch(w.getFri(), date) || isDateMatch(w.getSat(), date) ||
                            isDateMatch(w.getSun(), date));

            if (isMatch) {
                log.info("[라벨조회] {}-{}월 기준으로 매칭됨", yy, mm);
                List<ClassRespDTO.RecordLabelDTO> response = classRepository.findTimeTableByUserCode(yy, mm, dayName, userCode, centerCode);
                log.info("[라벨조회] {}-{}월 결과: {}건", yy, mm, response.size());
                return response;
            }

            log.info("[라벨조회] {}-{}월 매칭 없음", yy, mm);
        }

        log.info("[라벨조회] 현월/전월/익월 모두 매칭 없음 → 빈 리스트 반환");
        return Collections.emptyList();
    }

    private boolean isDateMatch(Object dateObj, java.time.LocalDate target) {
        if (dateObj == null || target == null)
            return false;
        String dateStr = target.toString();
        if (dateObj instanceof java.time.LocalDate)
            return dateObj.toString().equals(dateStr);
        if (dateObj instanceof org.threeten.bp.LocalDate)
            return dateObj.toString().equals(dateStr);
        if (dateObj instanceof String)
            return dateObj.equals(dateStr);
        return String.valueOf(dateObj).equals(dateStr);
    }

    public ClassRespDTO.RecordBundleDTO getTimeTableByKey(String userCode, String timeTableKey, String date,
                                                          String classKey, String unitKey, String centerCode) {

        String week = calculateWeekFromDate(date, centerCode);

        List<RecordStudentDTO> students = classRepository.findRecordStudentByKey(timeTableKey, week);

        String noticeWeek = week.split("_")[1];

        List<ClassRespDTO.AfterClassRespDTO> afterClassList = new ArrayList<>();

        String currentYear = DateConfig.currentYearMonth().get("currentYear");
        String yy = currentYear.substring(2, 4);

        ClassRespDTO.AfterClassRespDTO variant = classRepository.findAfterClassVariant(
                userCode, centerCode, classKey, unitKey, week, timeTableKey, currentYear);

        for (RecordStudentDTO s : students) {

            ClassRespDTO.AfterClassRespDTO notice = classRepository.findAfterClassNotice(
                    s.getStudentId(),
                    timeTableKey,
                    noticeWeek);

            if (notice != null) {

                afterClassList.add(notice);

            } else if (variant != null) {
                afterClassList.add(variant);

            } else {
                ClassRespDTO.AfterClassRespDTO base = classRepository.findAfterClass(
                        userCode, classKey, unitKey, week, timeTableKey, yy);

                afterClassList.add(base);

            }
        }

        ClassRespDTO.RecordBundleDTO bundle = new ClassRespDTO.RecordBundleDTO();
        bundle.setStudents(students);
        bundle.setAfterClass(afterClassList);
        bundle.setWeek(week);

        return bundle;
    }

    // 날짜로부터 주차 계산 메서드
    private String calculateWeekFromDate(String dateStr, String centerCode) {
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            String year = String.valueOf(date.getYear());
            String month = String.format("%02d", date.getMonthValue());

            log.info("[주차계산] 입력 날짜: {}, year: {}, month: {}", dateStr, year, month);

            String result = findWeekFromRepository(dateStr, date, year, month, centerCode);
            if (result != null) {
                log.info("[주차계산] 현재 월({}-{})에서 찾음 → {}", year, month, result);
                return result;
            }

            log.info("[주차계산] 현재 월({}-{})에서 매칭 없음 → 전월 조회", year, month);

            // 현재 월에서 못 찾으면 → 전월로 재조회
            java.time.LocalDate prevMonth = date.minusMonths(1);
            String prevYear = String.valueOf(prevMonth.getYear());
            String prevMonthStr = String.format("%02d", prevMonth.getMonthValue());

            log.info("[주차계산] 전월 조회: year: {}, month: {}", prevYear, prevMonthStr);

            String prevResult = findWeekFromRepository(dateStr, date, prevYear, prevMonthStr, centerCode);
            if (prevResult != null) {
                log.info("[주차계산] 전월({}-{})에서 찾음 → {}", prevYear, prevMonthStr, prevResult);
                return prevResult;
            }

            // 전월도 없으면 전월 첫 주차 반환
            List<ClassRespDTO.ClassWeekDTO> prevWeeks = classRepository.getClassWeek(prevYear, prevMonthStr,
                    centerCode);
            String fallback = prevWeeks.isEmpty() ? "ju_1" : prevWeeks.get(0).getWeek();
            log.info("[주차계산] 전월에서도 매칭 없음 → fallback: {}", fallback);
            return fallback;

        } catch (Exception e) {
            log.error("[주차계산] 오류 발생: dateStr={}, error={}", dateStr, e.getMessage());
            return "ju_1";
        }
    }

    // 탐색 로직 분리
    private String findWeekFromRepository(String dateStr, java.time.LocalDate date, String year, String month,
                                          String centerCode) {
        List<ClassRespDTO.ClassWeekDTO> weeks = classRepository.getClassWeek(year, month, centerCode);
        if (weeks.isEmpty()) {
            return null;
        }

        for (ClassRespDTO.ClassWeekDTO week : weeks) {
            List<Object> dates = Arrays.asList(
                    week.getMon(), week.getTue(), week.getWed(),
                    week.getThu(), week.getFri(), week.getSat(), week.getSun());

            for (Object dateObj : dates) {
                if (dateObj == null)
                    continue;

                String weekDateStr;
                if (dateObj instanceof java.time.LocalDate) {
                    weekDateStr = dateObj.toString();
                } else if (dateObj instanceof org.threeten.bp.LocalDate) {
                    weekDateStr = dateObj.toString();
                } else if (dateObj instanceof String) {
                    weekDateStr = (String) dateObj;
                } else {
                    weekDateStr = String.valueOf(dateObj);
                }

                if (dateStr.equals(weekDateStr)) {
                    return week.getWeek();
                }
            }
        }

        return null; // 매칭 없음
    }

    public ClassRespDTO.BeforeClassRespDTO getBeforeClassContent(String classKey, String unitKey, String week,
                                                                 String timeTableKey, String centerCode) {

        String currentYear = DateConfig.currentYearMonth().get("currentYear");
        String yy = currentYear.substring(2, 4);
        ClassRespDTO.BeforeClassRespDTO variant = classRepository.findBeforeClassVariant(centerCode, classKey, unitKey, week, timeTableKey, currentYear);
        if (variant != null) {
            return variant;
        }

        ClassRespDTO.BeforeClassRespDTO response = classRepository.findBeforeClass(classKey, unitKey, week,
                timeTableKey, yy);
        return response;
    }

    public ClassRespDTO.BeforeAllCountRespDTO countAllBeforeClassStudents(String date, String userCode, String centerCode) {
        return classRepository.countStudentsByDateAndUserCode(date, userCode, centerCode);
    }

    public ClassRespDTO.BeforeAllSendRespDTO sendAllBeforeClassNotice(String date, String userCode, String centerCode) {

        List<ClassRespDTO.BeforeAllStudentDTO> students =
                classRepository.selectStudentsByDateAndUserCode(date, userCode, centerCode);

        Map<String, String> weekMap = Map.of("ju_1", "1주", "ju_2", "2주", "ju_3", "3주", "ju_4", "4주");
        Map<String, String> typeMap = Map.of("1", "S", "2", "I");

        int sentCount = 0, skippedCount = 0;

        for (ClassRespDTO.BeforeAllStudentDTO s : students) {

            // 1. FCM 발송
            if (s.getAppToken() != null && !s.getAppToken().isBlank()) {
                fcmService.sendMessage(s.getAppToken(), "수업 안내", "수업 전 안내가 등록되었습니다.");
                sentCount++;
            } else {
                skippedCount++;
            }

            // 2. 출결 업데이트
            classRepository.updateAttendance(s.getStudentId(), s.getTimeTableKey(), date, s.getWeek());

            // 3. before_class_notice insert (기존 메서드 재사용)
            String weekLabel = weekMap.getOrDefault(s.getWeek(), s.getWeek());
            String typeLabel = typeMap.getOrDefault(s.getClassType(), s.getClassType());
            String classLabel = String.format("%s %s %s | %s 선생님",
                    s.getClassName(), s.getUnitName(), weekLabel, s.getUserName());

            ClassReqDTO.BeforeClassNoticeDTO dto = new ClassReqDTO.BeforeClassNoticeDTO();
            dto.setStudentId(s.getStudentId());
            dto.setUserCode(userCode);
            dto.setTimeTableKey(s.getTimeTableKey());
            dto.setClassDate(date);
            dto.setWeek(weekLabel);
            dto.setClassType(typeLabel);
            dto.setDayname(s.getDayname());
            dto.setClassTime(s.getStartTime());
            dto.setContent(s.getContent());
            dto.setClassLabel(classLabel);

            classRepository.insertBeforeClassNotice(dto);
        }

        ClassRespDTO.BeforeAllSendRespDTO resp = new ClassRespDTO.BeforeAllSendRespDTO();
        resp.setSentCount(sentCount);
        resp.setSkippedCount(skippedCount);
        return resp;
    }

    public void insertBeforeClassNoticeList(List<ClassReqDTO.BeforeClassNoticeDTO> dtoList, String userCode) {

        for (ClassReqDTO.BeforeClassNoticeDTO dto : dtoList) {

            ClassReqDTO.BeforeClassNoticeDTO insertDTO = new ClassReqDTO.BeforeClassNoticeDTO();
            ClassRespDTO.RawClassDTO rawDTO = classRepository.findClassByTimeTableKey(dto.getTimeTableKey());
            Map<String, String> weekMap = Map.of("ju_1", "1주", "ju_2", "2주", "ju_3", "3주", "ju_4", "4주");
            String weekLabel = weekMap.getOrDefault(dto.getWeek(), dto.getWeek());
            Map<String, String> typeMap = Map.of("1", "S", "2", "I");
            String typeLabel = typeMap.getOrDefault(rawDTO.getClassType(), rawDTO.getClassType());
            String classLabel = String.format("%s %s %s | %s 선생님", rawDTO.getClassName(), rawDTO.getUnitName(),
                    weekLabel, rawDTO.getUserName());

            insertDTO.setStudentId(dto.getStudentId());
            insertDTO.setUserCode(userCode);
            insertDTO.setTimeTableKey(dto.getTimeTableKey());
            insertDTO.setClassDate(dto.getClassDate());
            insertDTO.setWeek(weekLabel);
            insertDTO.setClassType(typeLabel);
            insertDTO.setDayname(rawDTO.getDayname());
            insertDTO.setClassTime(rawDTO.getStartTime());
            insertDTO.setContent(dto.getContent());
            insertDTO.setClassLabel(classLabel);

            classRepository.insertBeforeClassNotice(insertDTO);

        }
    }

    // 알림 발송 이후 출결 업데이트
    public void updateAttendance(String studentId, String timeTableKey, String attendanceDate, String week) {
        classRepository.updateAttendance(studentId, timeTableKey, attendanceDate, week);
    }

    public void insertAfterClassNoticeList(List<ClassReqDTO.AfterClassNoticeDTO> dtoList, String userCode) {
        for (ClassReqDTO.AfterClassNoticeDTO dto : dtoList) {

            ClassReqDTO.AfterClassNoticeDTO insertDTO = new ClassReqDTO.AfterClassNoticeDTO();

            ClassRespDTO.RawClassDTO rawDTO = classRepository.findClassByTimeTableKey(dto.getTimeTableKey());

            Map<String, String> weekMap = Map.of("ju_1", "1주", "ju_2", "2주", "ju_3", "3주", "ju_4", "4주");
            String weekLabel = weekMap.getOrDefault(dto.getWeek(), dto.getWeek());
            String rawWeek = dto.getWeek().substring(dto.getWeek().length() - 1);
            Map<String, String> typeMap = Map.of("1", "S", "2", "I");
            String typeLabel = typeMap.getOrDefault(rawDTO.getClassType(), rawDTO.getClassType());
            String classLabel = String.format("%s %s %s ", rawDTO.getClassName(), rawDTO.getUnitName(), weekLabel);
            LocalDateTime now = LocalDateTime.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM월 dd일 E요일 HH:mm", Locale.KOREAN);

            String time = now.format(formatter);

            insertDTO.setStudentId(dto.getStudentId());
            insertDTO.setUserCode(userCode);
            insertDTO.setTimeTableKey(dto.getTimeTableKey());
            insertDTO.setAfterClassKey(dto.getAfterClassKey());
            insertDTO.setYear(rawDTO.getYy());
            insertDTO.setMonth(rawDTO.getMm());
            insertDTO.setWeek(rawWeek);
            insertDTO.setDayname(time);
            insertDTO.setContent(dto.getContent());
            insertDTO.setWord(dto.getWord());
            insertDTO.setReview(dto.getReview());
            insertDTO.setClassType(typeLabel);
            insertDTO.setClassLabel(classLabel);
            insertDTO.setCounselType(dto.getCounselType());
            insertDTO.setCounselContent(dto.getCounselContent());

            classRepository.upsertAfterClassNotice(insertDTO);
        }
    }

    public void updateAfterSend(String studentId, String timeTableKey, String week) {
        classRepository.updateAfterSend(studentId, timeTableKey, week);
    }

    public List<String> findUnsentAfterNotification(String userCode, String centerCode) {
        return classRepository.findUnsentAfterNotification(userCode, centerCode);
    }

    // ================ 보강 관리 서비스 =====================//
    public List<RemedialDTO> findRemedialByUserNo(String year, String month, String userCode) {
        System.out.println(year);
        System.out.println(month);
        List<RemedialDTO> remedials = classRepository.findRemedialByUserCode(year, month, userCode);
        return remedials;
    }

    public List<ClassRespDTO.AbsentFlagDTO> findAbsentFlags(String year, String month, String userCode) {
        return classRepository.findAbsentFlags(year, month, userCode);
    }


    public int updateRemedialAction(UpdateRemedialDTO dto) {
        int result = classRepository.updateRemedialAction(dto.getRemedialKey(), dto.isAction());
        return result;
    }

    public void updateRemedialDate(UpdateRemedialDateDTO dto) {
        classRepository.updateRemedialDate(dto.getRemedialKey(), dto.getRemedialDate());
    }

    @Transactional
    public void updateRemedialTime(ClassReqDTO.UpdateRemedialTimeDTO dto) {
        classRepository.updateRemedialTime(dto.getRemedialKey(), dto.getStartTime());
        // 보강 알림 발송
        ClassRespDTO.RemedialSendDTO response = classRepository.findAppTokenByRemedialKey(dto.getRemedialKey());
        if (response != null
                && StringUtils.hasText(response.getAppToken())
                && StringUtils.hasText(response.getStudentname())
                && StringUtils.hasText(response.getRemedialDate())
                && !response.getRemedialDate().equals("9999-12-31")
                && StringUtils.hasText(response.getSTime())) {
            String month = String.valueOf(Integer.parseInt(response.getRemedialDate().substring(5, 7)));
            String day = String.valueOf(Integer.parseInt(response.getRemedialDate().substring(8, 10)));
            String pushContent = response.getStudentname() + " 학생의 보강 수업이 "
                    + month + "월 "
                    + day + "일 "
                    + response.getSTime() + "에 진행될 예정이에요.\n⏰시간에 맞춰 도착해 주세요.";

            String content = response.getStudentname() + " 학생의 보강 수업이<br>"
                    + month + "월 "
                    + day + "일 "
                    + response.getSTime() + "에 진행될 예정이에요.<br>⏰시간에 맞춰 도착해 주세요.";

            boolean isSend = fcmService.sendMessage(response.getAppToken(), "보강 안내", pushContent);

            if (isSend) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                String centerNoticeKey = response.getUserCode() + now.format(formatter);
                NoticeReqDTO.RemedialNoticeDTO remedialNoticeDTO = NoticeReqDTO.RemedialNoticeDTO.builder()
                        .centerNoticeKey(centerNoticeKey)
                        .title("보강 안내")
                        .content(content)
                        .subTitle(month + "월 "
                                + day + "일 보강 안내")
                        .icon("1")
                        .centerCode(response.getCenterCode())
                        .userCode(response.getUserCode())
                        .build();
                noticeRepository.insertRemedialNotice(remedialNoticeDTO);
                noticeRepository.insertRemedialNoticeMap(remedialNoticeDTO.getCenterNoticeKey(),
                        response.getStudentId());
            }
        }
    }

    public void deleteRemedial(ClassReqDTO.DeleteRemedialTimeDTO dto) {
        classRepository.deleteRemedial(dto.getRemedialKey());
    }

    // ================ 월간 평가 서비스 =====================//

    // 수업 주차 조회
    public ClassRespDTO.ClassWeekInfoDTO findWeekInfoByCenter(String today, String dayname, String centerCode) {

        List<ClassRespDTO.ClassWeekInfoDTO> weekInfos = classRepository.findWeekInfoAllCenters(today, dayname);

        return weekInfos.stream().filter(w -> w.getCenterCode().equals(centerCode)).findFirst().orElse(null);

    }

    public List<TimeTableLabelDTO> getMonthlyClassList(String userCode, String yy, String mm, String dayname,
                                                       String centerCode) {
        List<TimeTableLabelDTO> labels = classRepository
                .findClassLabelByUserCodeAndDayname(userCode, yy, mm, dayname, centerCode)
                .stream()
                .map(c -> {
                    String label = c.getClassLabel();
                    if (label != null && !label.isBlank()) {
                        int idx = label.indexOf(" ", label.indexOf("~") + 2);
                        if (idx > 0) {
                            String time = label.substring(0, idx).trim();
                            String subject = label.substring(idx + 1).trim();
                            c.setClassTime(time);
                            c.setClassSubject(subject);
                        } else {
                            c.setClassSubject(label);
                        }
                    }
                    return c;
                })
                .collect(Collectors.toList());

        return labels;
    }

    public List<MonthlyStudentDTO> getMonthlyClassDetail(String timeTableKey) {
        List<MonthlyStudentDTO> students = classRepository.findStudentByClassCode(timeTableKey);
        return students;
    }

    public ClassRespDTO.ScoreResultDTO updateMonthlyScore(ClassMonthlyScoreDTO dto, String centerCode) {
        ClassMonthlyScoreDTO.MonthlyScoreDTO score = dto.getScores().get(0);

        classRepository.updateMonthlyScore(dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm(), score);

        int wrongCount = 0;
        wrongCount += (score.isQuestion1()) ? 0 : 1;
        wrongCount += (score.isQuestion2()) ? 0 : 1;
        wrongCount += (score.isQuestion3()) ? 0 : 1;
        wrongCount += (score.isQuestion4()) ? 0 : 1;
        wrongCount += (score.isQuestion5()) ? 0 : 1;
        wrongCount += (score.isQuestion6()) ? 0 : 1;
        wrongCount += (score.isQuestion7()) ? 0 : 1;
        wrongCount += (score.isQuestion8()) ? 0 : 1;

        String currentYear = DateConfig.currentYearMonth().get("currentYear");

        boolean feedbackVariantExists = classRepository.existsMonthlyFeedbackVariant(
                dto.getTimeTableKey(), centerCode, currentYear);
        boolean commentVariantExists = classRepository.existsMonthlyCommentVariant(
                dto.getTimeTableKey(), centerCode, currentYear);
        log.info(String.valueOf(feedbackVariantExists));
        log.info(String.valueOf(commentVariantExists));
        Map<String, String> monthlyFeedback = classRepository.getMonthlyFeedback(
                dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm(),
                centerCode, currentYear, feedbackVariantExists, commentVariantExists);

        String feedback;

        if (wrongCount == 0) {
            List<String> correctComments = classRepository.getMonthlyAllCorrectFeedback(
                    dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm(),
                    centerCode, currentYear, feedbackVariantExists);

            if (correctComments.size() >= 2) {
                Collections.shuffle(correctComments);
                feedback = correctComments.get(0) + "\n또한 " + correctComments.get(1);
            } else {
                feedback = String.join("\n", correctComments);
            }

        } else {
            feedback = Optional.ofNullable(monthlyFeedback.get("topCorrectMent")).orElse("")
                    + "\n" +
                    Optional.ofNullable(monthlyFeedback.get("topWrongMent")).orElse("");
        }

        ClassRespDTO.ScoreResultDTO response = new ClassRespDTO.ScoreResultDTO();
        response.setTimeTableKey(dto.getTimeTableKey());
        response.setStudentId(dto.getStudentId());
        response.setStudentName(monthlyFeedback.get("studentName"));
        response.setScoreResult(feedback);
        response.setBottomComment(monthlyFeedback.get("bottomComment"));

        log.info("response={}", response);

        MonthlyResult exist = classRepository.findMonthlyResult(dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm());

        if (exist == null) {
            MonthlyResult insertResult = MonthlyResult.builder()
                    .topComment(monthlyFeedback.get("topComment"))
                    .bottomComment(monthlyFeedback.get("bottomComment"))
                    .feedback(feedback)
                    .isSend(false)
                    .yy(dto.getYy())
                    .mm(dto.getMm())
                    .timeTable(TimeTable.builder().timeTableKey(dto.getTimeTableKey()).build())
                    .student(Student.builder().studentId(dto.getStudentId()).build())
                    .build();

            classRepository.insertMonthlyResult(insertResult);

        } else {
            MonthlyResult updateResult = MonthlyResult.builder()
                    .id(exist.getId())
                    .topComment(monthlyFeedback.get("topComment"))
                    .bottomComment(monthlyFeedback.get("bottomComment"))
                    .feedback(feedback)
                    .build();

            classRepository.updateMonthlyResult(updateResult);
        }
        return response;
    }

    public ClassRespDTO.MonthlyPreviewRespDTO getMonthlyPreview(ClassReqDTO.MonthlyPreviewDTO dto) {

        List<Map<String, Object>> rows = classRepository.findMonthlyPreview(dto);

        if (rows == null || rows.isEmpty()) {
            throw new NoSuchElementException("월간 평가 데이터가 없습니다.");
        }

        ClassRespDTO.MonthlyPreviewRespDTO resp = new ClassRespDTO.MonthlyPreviewRespDTO();

        List<String> scores = new ArrayList<>();
        List<String> competency = new ArrayList<>();
        List<String> difficultly = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {

            Map<String, Object> r = rows.get(i);

            if (i == 0) {
                resp.setStudentId(String.valueOf(r.get("student_id")));
                resp.setStudentName(String.valueOf(r.get("student_name")));
                resp.setTimeTableKey(String.valueOf(r.get("time_table_key")));
                resp.setTopComment(String.valueOf(r.get("top_comment")));
                resp.setBottomComment(String.valueOf(r.get("bottom_comment")));
                resp.setFeedback(String.valueOf(r.get("feedback")));
            }

            scores.add(String.valueOf(r.get("score")));
            competency.add(String.valueOf(r.get("competency")));
            difficultly.add(String.valueOf(r.get("difficultly")));
        }

        resp.setScores(scores);
        resp.setCompetency(competency);
        resp.setDifficultly(difficultly);

        return resp;
    }

    public ClassAppRespDTO.MonthlyReportRespDTO getMonthlyReport(ClassAppReqDTO.MonthlyResultReqDTO reqDTO) {
        return classRepository.findMonthlyReport(reqDTO);
    }

    @Transactional
    public void updateMonthlySendStatus(
            List<String> successStudentIds,
            List<FcmDTO.MonthlyFcmDTO.StudentDTO> students,
            String yy,
            String mm) {
        for (FcmDTO.MonthlyFcmDTO.StudentDTO student : students) {
            // 발송 성공한 학생만 업데이트
            if (successStudentIds.contains(student.getStudentId())) {
                int updatedRows = classRepository.updateMonthlySendStatus(
                        student.getStudentId(),
                        student.getTimeTableKey(),
                        yy,
                        mm);

                if (updatedRows > 0) {
                    log.info("is_send 업데이트 성공. studentId: {}", student.getStudentId());
                }
            }
        }
    }

    @Transactional
    public void saveMonthlyComments(List<ClassReqDTO.MonthlySaveRequestDTO.MonthlySaveDTO> students) {
        for (ClassReqDTO.MonthlySaveRequestDTO.MonthlySaveDTO student : students) {

            int updatedRows = classRepository.updateMonthlyComment(student);

            if (updatedRows == 0) {
                log.warn("업데이트된 행이 없습니다. studentId: {}, timeTableKey: {}, yy: {}, mm: {}",
                        student.getStudentId(), student.getTimeTableKey(), student.getYy(), student.getMm());
            }
        }

        log.info("{}명의 월간평가 코멘트 저장 완료", students.size());
    }

    public ClassAppRespDTO.MonthlyHaniRespDTO findAppInfantHani(ClassAppReqDTO.MonthlyResultReqDTO reqDTO) {

        String studentId = reqDTO.getId();
        String yy = reqDTO.getYm().substring(0, 4);
        String mm = reqDTO.getYm().substring(4, 6);

        return classRepository.findAppInfantHani(studentId, yy, mm);

    }

    public ClassAppRespDTO.MonthlyBukiRespDTO findAppInfantBuki(ClassAppReqDTO.MonthlyResultReqDTO reqDTO) {
        String studentId = reqDTO.getId();
        String yy = reqDTO.getYm().substring(0, 4);
        String mm = reqDTO.getYm().substring(4, 6);

        return classRepository.findAppInfantBuki(studentId, yy, mm);
    }

    public List<TimeTableLabelDTO> findInfantClassLabel(ClassReqDTO.InfantClassLabelsDTO dto) {

        List<TimeTableLabelDTO> labels = classRepository
                .findInfantClassLabel(dto.getUserCode(), dto.getYy(), dto.getMm())
                .stream()
                .map(c -> {
                    String label = c.getClassLabel();
                    if (label != null && !label.isBlank()) {
                        int idx = label.indexOf(" ", label.indexOf("~") + 2);
                        if (idx > 0) {
                            String time = label.substring(0, idx).trim();
                            String subject = label.substring(idx + 1).trim();
                            c.setClassTime(time);
                            c.setClassSubject(subject);
                        } else {
                            c.setClassSubject(label);
                        }
                    }
                    return c;
                })
                .collect(Collectors.toList());

        return labels;
    }

    public ClassRespDTO.InfantHanDTO findInfantHan(TimeTableLabelDTO dto) {

        try {
            ClassRespDTO.InfantHanDTO infantHanDTO = classRepository.findInfantHan(dto.getClassKey(), dto.getUnitKey(),
                    dto.getYy());

            if (infantHanDTO == null) {
                log.warn("InfantHanDTO 조회 결과 없음. classKey={}, unitKey={}, year={}",
                        dto.getClassKey(), dto.getUnitKey(), dto.getYy());
                return null;
            }

            List<ClassRespDTO.InfantHanDTO.StudentInfo> students = classRepository
                    .findInfantHanStudents(dto.getTimeTableKey());

            if (students == null) {
                students = new ArrayList<>();
            }

            infantHanDTO.setStudents(students);

            return infantHanDTO;

        } catch (Exception e) {
            log.error("유아 한자 조회 중 오류 발생", e);
            return null;
        }
    }

    public ClassRespDTO.InfantBookDTO findInfantBook(TimeTableLabelDTO dto) {

        try {
            ClassRespDTO.InfantBookDTO infantBookDTO = classRepository.findInfantBook(dto.getClassKey(),
                    dto.getUnitKey(), dto.getYy());

            if (infantBookDTO == null) {
                log.warn("InfantBookDTO 조회 결과 없음. classKey={}, unitKey={}, year={}",
                        dto.getClassKey(), dto.getUnitKey(), dto.getYy());
                return null;
            }

            List<ClassRespDTO.InfantBookDTO.StudentInfo> students = classRepository
                    .findInfantBookStudents(dto.getTimeTableKey());

            if (students == null) {
                students = new ArrayList<>();
            }

            infantBookDTO.setStudents(students);

            return infantBookDTO;

        } catch (Exception e) {
            log.error("유아 독서 조회 중 오류 발생", e);
            return null;
        }
    }

    public void saveInfantSendHistory(String classType, String timeTableKey, String senderUser, String centerCode,
                                      List<String> studentIds) {

        for (String studentId : studentIds) {

            if (classRepository.existsInfantSendHistory(studentId, timeTableKey) > 0) {
                continue;
            }

            InfantSendHistory history = InfantSendHistory.builder()
                    .classType(classType)
                    .student(Student.builder().studentId(studentId).build())
                    .timeTable(TimeTable.builder().timeTableKey(timeTableKey).build())
                    .center(Center.builder().centerCode(centerCode).build())
                    .senderUser(User.builder().userCode(senderUser).build())
                    .build();

            classRepository.createInfantSendHistory(history);
        }
    }

    @Transactional
    public void saveInfantNotice(ClassReqDTO.InfantSaveReqDTO reqDTO, String centerCode, String userCode) {

        for (ClassReqDTO.InfantSaveReqDTO.StudentDTO s : reqDTO.getStudents()) {

            // ✅ 1. 기존 데이터 존재 여부 확인
            int cnt = 0;
            if ("HAN".equals(reqDTO.getType())) {
                cnt = classRepository.countInfantHan(s.getStudentId(), reqDTO.getTimeTableKey());
            } else if ("BOOK".equals(reqDTO.getType())) {
                cnt = classRepository.countInfantBook(s.getStudentId(), reqDTO.getTimeTableKey());
            }

            // ✅ 2. sendId 조회 → 없으면 무조건 생성 (저장/발행 관계없이)
            Integer sendId = classRepository.findInfantSendId(s.getStudentId(), reqDTO.getTimeTableKey());

            if (sendId == null) {
                InfantSendHistory history = InfantSendHistory.builder()
                        .classType(reqDTO.getType())
                        .student(Student.builder().studentId(s.getStudentId()).build())
                        .timeTable(TimeTable.builder().timeTableKey(reqDTO.getTimeTableKey()).build())
                        .center(Center.builder().centerCode(centerCode).build())
                        .senderUser(User.builder().userCode(userCode).build())
                        .build();

                classRepository.createInfantSendHistory(history);
                sendId = classRepository.findInfantSendId(s.getStudentId(), reqDTO.getTimeTableKey());
            }

            // ✅ 3. INSERT or UPDATE (로직 동일)
            if (cnt > 0) {
                // UPDATE
                Boolean existingIsSend = null;
                if ("HAN".equals(reqDTO.getType())) {
                    existingIsSend = classRepository.findInfantHanIsSend(
                            s.getStudentId(), reqDTO.getTimeTableKey());
                } else {
                    existingIsSend = classRepository.findInfantBookIsSend(
                            s.getStudentId(), reqDTO.getTimeTableKey());
                }

                // ✅ 발행이면 무조건 true, 저장이면 기존 값 유지
                Boolean finalIsSend = reqDTO.getIsSend() ? true : (existingIsSend != null && existingIsSend);

                if ("HAN".equals(reqDTO.getType())) {
                    classRepository.updateInfantHanNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, finalIsSend);
                } else {
                    classRepository.updateInfantBookNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, finalIsSend);
                }

            } else {
                // INSERT
                if ("HAN".equals(reqDTO.getType())) {
                    classRepository.insertInfantHanNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, reqDTO.getIsSend());
                } else {
                    classRepository.insertInfantBookNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, reqDTO.getIsSend());
                }
            }
        }
    }

    // ======================================== APP
    // ======================================== //
    public List<ClassAppRespDTO.ClassInfoRespDTO> getClassInfo(String studentId, String yy, String mm) {

        List<ClassAppRespDTO.ClassInfoRespDTO> respDTOs = classRepository.findClassInfoByStudentId(studentId, yy, mm);

        return respDTOs;

    }

    public List<ClassAppRespDTO.BeforeClassRespDTO> getBeforeClass(String studentId, int count) {

        List<ClassAppRespDTO.BeforeClassRespDTO> respDTOS = classRepository.findBeforeClassByStudentId(studentId,
                count);

        return respDTOS;
    }

    public List<ClassAppRespDTO.AfterClassRespDTO> getAfterClass(String studentId, int count) {

        List<ClassAppRespDTO.AfterClassRespDTO> respDTOS = classRepository.findAfterClassByStudentId(studentId, count);

        respDTOS.forEach(dto -> {
            if (dto.getSnote() != null) {
                // <span> 태그 및 다른 HTML 태그 제거
                String cleaned = dto.getSnote().replaceAll("<[^>]*>", "");
                dto.setSnote(cleaned.trim());
            }
        });

        return respDTOS;
    }

    public List<UnitCode> findUnitCodeForPerson() {
        List<UnitCode> unitCodes = classRepository.findUnitCodeForPerson();
        return unitCodes;
    }

    public List<ClassAppRespDTO.ClinicListRespDTO> findClinicList(ClassAppReqDTO.ClinicBookListReqDTO dto) {
        return classRepository.findClinicList(dto.getId(), dto.getYm());
    }

    public List<ClassAppRespDTO.ClinicResultRespDTO> findClinicResult(ClassAppReqDTO.ClinicBookResultReqDTO dto) {
        String yy = dto.getYm().substring(0, 4);
        String mm = dto.getYm().substring(4, 6);

        return classRepository.findClinicResult(dto.getId(), yy, mm);
    }

    public List<ClassAppRespDTO.ClinicTotalRespDTO> findClinicTotal(ClassAppReqDTO.ClinicBookTotalListReqDTO dto) {
        String sYear = dto.getSym().substring(0, 4);
        String sMonth = dto.getSym().substring(4, 6);
        String eYear = dto.getSym().substring(0, 4);
        String eMonth = dto.getEym().substring(4, 6);
        return classRepository.findClinicTotal(dto.getId(), sYear, sMonth, eYear, eMonth);
    }

    public List<ClassAppRespDTO.AfterClassDetailRespDTO> getAfterClassDetail(
            ClassAppReqDTO.LearningContentDetailReqDTO dto) {
        List<ClassAppRespDTO.AfterClassDetailRespDTO> response = classRepository.findAfterClassDetail(dto.getId(),
                dto.getGamok(), dto.getYyyy(), dto.getMm(), dto.getJu());
        return response;

    }

    public List<ClassRespDTO.RemarksCategoryDTO> getRemarksList(ClassReqDTO.RemarksRequestDTO dto) {
        // 1. 전체 12개 항목 조회
        List<ClassRespDTO.RemarksItemDTO> allItems = classRepository.selectAllItems();

        // 2. 해당 학생의 체크된 항목 조회
        List<String> checkedKeys = classRepository.selectCheckedKeys(dto);

        // 3. checked 세팅
        allItems.forEach(item -> item.setChecked(checkedKeys.contains(item.getRemarksKey())));

        // 4. 카테고리별로 그룹핑
        Map<String, List<ClassRespDTO.RemarksItemDTO>> grouped = allItems.stream()
                .collect(Collectors.groupingBy(ClassRespDTO.RemarksItemDTO::getCategoryName, LinkedHashMap::new,
                        Collectors.toList()));

        return grouped.entrySet().stream()
                .map(e -> {
                    ClassRespDTO.RemarksCategoryDTO cat = new ClassRespDTO.RemarksCategoryDTO();
                    cat.setCategoryName(e.getKey());
                    cat.setItems(e.getValue());
                    return cat;
                })
                .collect(Collectors.toList());
    }

    public void saveRemarks(ClassReqDTO.RemarksSaveDTO dto) {
        // 1. 기존 데이터 삭제
        classRepository.deleteRemarks(dto);

        // 2. 체크된 항목만 INSERT
        if (dto.getRemarksKeys() != null && !dto.getRemarksKeys().isEmpty()) {
            classRepository.insertRemarks(dto);
        }
    }

    public List<String> findExistingEduUserCodes(
            List<String> userCodes, String yy, String mm, String centerCode) {
        return classRepository.findExistingEduUserCodes(userCodes, yy, mm, centerCode);
    }

    public void executeEduGenerateProcedure(String yy, String mm, String centerCode, String userCode) {
        if (centerCode.equals("PUS002")) {
            classRepository.generateProcedureNamCheon(yy, mm, centerCode, userCode);
        } else if (centerCode.equals("DAE001")) {
            classRepository.generateProcedureWolSung(yy, mm, centerCode, userCode);
        }

    }

    public List<ClassRespDTO.TimeTableDTO> findEduTableViewWithStudents(
            String yy, String mm, String userCode, String centerCode) {

        List<ClassRespDTO.TimeTableDTO> tables = classRepository.findEduTimeTableBasic(
                userCode, yy, mm, centerCode);
        if (tables.isEmpty())
            return tables;

        // periodNo 재정렬 (요일별로 startTime 순 1,2,3...)
        Map<String, List<ClassRespDTO.TimeTableDTO>> byDay = tables.stream()
                .collect(Collectors.groupingBy(ClassRespDTO.TimeTableDTO::getDayname));

        byDay.forEach((day, list) -> {
            list.sort(Comparator.comparing(ClassRespDTO.TimeTableDTO::getStartTime));
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setPeriodNo(String.valueOf(i + 1));
            }
        });

        // 학생 조회 및 세팅 (기존 로직 동일)
        List<String> keys = tables.stream()
                .map(ClassRespDTO.TimeTableDTO::getTimeTableKey)
                .collect(Collectors.toList());

        List<ClassRespDTO.TimeTableDTO.StudentDTO> allStudents = classRepository.findEduStudentsByTimeTables(keys);

        Map<String, List<ClassRespDTO.TimeTableDTO.StudentDTO>> studentMap = allStudents.stream()
                .collect(Collectors.groupingBy(ClassRespDTO.TimeTableDTO.StudentDTO::getTimeTableKey));

        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = new ArrayList<>(
                    studentMap.getOrDefault(tt.getTimeTableKey(), List.of()));
            while (students.size() < 8) {
                ClassRespDTO.TimeTableDTO.StudentDTO empty = new ClassRespDTO.TimeTableDTO.StudentDTO();
                empty.setStudentName("\u00A0");
                students.add(empty);
            }
            tt.setStudents(students);
        }

        return tables;
    }

}
