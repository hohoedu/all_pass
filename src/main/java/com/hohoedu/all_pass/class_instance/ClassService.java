package com.hohoedu.all_pass.class_instance;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.hohoedu.all_pass._core.firebase.FcmDTO;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppReqDTO;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import com.hohoedu.all_pass.class_instance.model.*;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment.model.PaymentDetail;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.TeacherAssign;
import com.hohoedu.all_pass.student.repository.StudentRepository;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
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
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.student.repository.GradeJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.threeten.bp.LocalDate;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UnitCodeJpaRepository unitCodeJpaRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final StudentService studentService;
    private final PaymentService paymentService;

    public List<ClassRespDTO.MainClassSummaryDTO> getClassSummary(String centerCode, String userCode) {

        String today = DateConfig.currentYearMonth().get("today");
        String year = DateConfig.currentYearMonth().get("currentYear");
        String month = DateConfig.currentYearMonth().get("currentMonth");
        String dayname = DateConfig.currentYearMonth().get("currentDayName");

        log.info(today);
        List<ClassRespDTO.MainClassSummaryDTO> responseDTO =
                classRepository.findClassSummary(year, month, dayname, userCode, centerCode);

        try {
            if (responseDTO != null && !responseDTO.isEmpty()) {

                for (ClassRespDTO.MainClassSummaryDTO dto : responseDTO) {

                    // 유효성 검사
                    if (dto == null || dto.getCountStudent() == null) continue;

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

    public void saveClassWeek(ClassReqDTO.WeekReqDTO dto, String centerCode) {

        List<ClassRespDTO.ClassWeekDTO> existing = classRepository.getClassWeek(dto.getYear(), dto.getMonth(), centerCode);

        for (int i = 1; i <= 4; i++) {

            ClassReqDTO.WeekReqDTO.WeekDetailDTO detail = dto.getWeek().get(String.valueOf(i));
            if (detail == null) continue;

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
            List<ClassRespDTO.ClassUnitDTO> personUnits = classRepository.findPersonUnit(centerCode, year, month, classKey);

            if (!personUnits.isEmpty()) {
                result.put(classKey, personUnits);
            }
        }

        return result;
    }

    public Map<String, List<ClassRespDTO.ClassUnitDTO>> findClassUnitsOverPeriod(String centerCode, String startYear, String startMonth, String endYear, String endMonth) {
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
                    endYear, endMonth,
                    classKey
            );

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
        boolean isEmpty = timeTable == null;

        String label = createTimeTableLabel(classReqDTO);

        if (isEmpty) {
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

            return "success-register";
        } else {

            int result = classRepository.updateClass(classReqDTO, timeTable.getTimeTableKey(), classReqDTO.getUserCode());

            classRepository.updateLabel(label, timeTable.getTimeTableKey());

            if (result <= 0) {
                return "fail-update";
            }

            return "success-update";
        }
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

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, year, month);

        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = classRepository
                    .findStudentsByTimeTableKey(tt.getTimeTableKey());

            tt.setStudents(students);
        }
        return tables;
    }

    public ClassRespDTO.TimeTableViewRespDTO findTableViewWithStudents(String year, String month, String userCode) {

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, year, month);

        for (ClassRespDTO.TimeTableDTO tt : tables) {
            List<ClassRespDTO.TimeTableDTO.StudentDTO> students = classRepository
                    .findStudentsByTimeTableKey(tt.getTimeTableKey());

            while (students.size() < 8) {
                ClassRespDTO.TimeTableDTO.StudentDTO empty = new ClassRespDTO.TimeTableDTO.StudentDTO();
                empty.setStudentName("\u00A0");
                students.add(empty);
            }

            tt.setStudents(students);
        }

        String ym = year + "-" + month;
        List<ClassRespDTO.StudentStatRespDTO> studentStat = classRepository.findStudentStat(userCode, ym);
        Long totalStudentsLong = tables.stream()
                .flatMap(table -> table.getStudents().stream())
                .filter(student -> student.getStudentId() != null && !student.getStudentId().isEmpty())
                .filter(student -> !"\u00A0".equals(student.getStudentName()))
                .map(TimeTableDTO.StudentDTO::getStudentId)
                .distinct()
                .count();
        double totalStudentsDouble =
                tables.stream()
                        .flatMap(table -> table.getStudents().stream())
                        .filter(student ->
                                student.getStudentId() != null &&
                                        !student.getStudentId().isEmpty() &&
                                        !"\u00A0".equals(student.getStudentName())
                        )
                        .collect(
                                Collectors.groupingBy(
                                        TimeTableDTO.StudentDTO::getStudentId,
                                        Collectors.summingInt(student -> {
                                            try {
                                                return Integer.parseInt(student.getWeek());
                                            } catch (Exception e) {
                                                return 0; // week 값이 이상하면 0주 처리
                                            }
                                        })
                                )
                        )
                        .values()
                        .stream()
                        .mapToDouble(weekSum -> weekSum * 0.25)
                        .sum();
        return new ClassRespDTO.TimeTableViewRespDTO(tables, studentStat, totalStudentsLong, totalStudentsDouble);
    }

    // 학생 수업 등록
    public boolean addStudent(AddStudentDTO dto) {
        int duplicated = classRepository.existsSameClassType(dto);

        if (duplicated > 0) {
            throw new RuntimeException("이미 동일한 클래스 타입의 수업을 수강 중입니다.");
        }

        int count = classRepository.countByTimeTableKey(dto.getTimeTableKey());
        if (count >= 10) return false;

        classRepository.addStudent(dto);
        return true;
    }

    @Transactional
    public void registerStudentFullProcess(AddStudentDTO dto, String userCode, String centerCode) {

        boolean success = addStudent(dto);
        if (!success) {
            throw new IllegalStateException("정원 초과");
        }

        // 월간 평가 생성
        classRepository.insertMonthlyScore(dto.getStudentId(), dto.getYy(), dto.getMm(), dto.getTimeTableKey());

        // 출결 생성
        classRepository.createAttendance(dto.getStudentId(), dto.getTimeTableKey(), centerCode, dto.getYy(), dto.getMm());

        // 기본 수업 정보
        ClassRespDTO.BasicTimeTableInfo info = classRepository.findBasicTimeTableInfo(dto.getTimeTableKey(), centerCode);

        // 담당 선생님 배정 및 수업 변경
        studentService.assignTeacher(dto.getStudentId(), info);

        // student_class 생성
        ClassRespDTO.ClassInfoDTO classInfo = findClassInfoByTimeTableKeyAndStudentId(dto.getTimeTableKey(), dto.getStudentId(), centerCode);

        Integer baseFee = classInfo.getClassFee();
        if (baseFee == null) {
            throw new IllegalArgumentException("classFee가 null 입니다.");
        }
        int weekNo = Integer.parseInt(dto.getWeekNo());
        BigDecimal result = BigDecimal.valueOf(baseFee)
                .multiply(BigDecimal.valueOf(weekNo))
                .divide(BigDecimal.valueOf(4), 0, RoundingMode.DOWN);

        classInfo.setClassFee(result.intValue());

        if (!classInfo.getClassKey().equals("HL")) {
            // 결제 + 상세
            String paymentKey = paymentService.createPayment(dto.getStudentId(), dto.getYy(), dto.getMm(), centerCode, userCode);
            paymentService.createPaymentDetail(paymentKey, classInfo, userCode);

            paymentService.processPresetPaymentIfExists(dto.getStudentId(), paymentKey, classInfo.getClassFee(), userCode, centerCode, dto.getYy(), dto.getMm());
        }

    }



    @Transactional
    public void copyLastMonthTimeTableAndStudents(String userCode, String centerCode, String year, String month) {

        int count = classRepository.existsTimeTable(userCode, year, month);
        if (count > 0) {
            throw new RuntimeException("이번 달 시간표 등록 내역이 있습니다.");
        }
        Map<String, String> req = Map.of("year", year, "month", month);

        List<TimeTableDTO> lastTables = getLastTimeTable(userCode, req);

        if (lastTables.isEmpty()) return;

        Map<String, String> keyMap = new HashMap<>();

        for (TimeTableDTO old : lastTables) {

            ClassReqDTO.ClassRegisterDTO dto = new ClassReqDTO.ClassRegisterDTO();
            dto.setYy(year);
            dto.setMm(month);
            dto.setDayname(old.getDayname());
            dto.setPeriodNo(old.getPeriodNo());
            dto.setStartTime(old.getStartTime());
            dto.setEndTime(old.getEndTime());
            dto.setClassKey(old.getClassKey());
            dto.setUnitKey(old.getUnitKey());
            dto.setGradeKey(old.getGradeKey());
            dto.setUserCode(userCode);
            dto.setCenterCode(centerCode);

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

                // ✅ 전체 학생 등록 프로세스 재사용
                registerStudentFullProcess(addDto, userCode, centerCode);
            }
        }
    }


    public ClassRespDTO.ClassInfoDTO findClassInfoByTimeTableKeyAndStudentId(String timeTableKey, String studentId, String centerCode) {
        ClassRespDTO.ClassInfoDTO classInfo = classRepository.findClassInfoByTimeTableKeyAndStudentId(timeTableKey, studentId, centerCode);
        return classInfo;
    }

    public void deleteStudent(String timeTableKey, String studentId) {
        classRepository.deleteByKeyAndStudentId(timeTableKey, studentId);
        paymentService.deleteDetail(timeTableKey, studentId);
    }

    public List<TimeTableDTO> getLastTimeTable(String userCode, Map<String, String> req) {

        LocalDate selected = LocalDate.of(
                Integer.parseInt(req.get("year")),
                Integer.parseInt(req.get("month")),
                1
        );
        // 이전 달
        LocalDate prev = selected.minusMonths(1);

        String lastYear = String.valueOf(prev.getYear());
        String lastMonth = String.format("%02d", prev.getMonthValue());
//        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, lastYear, lastMonth);
        List<TimeTableDTO> rows = classRepository.findTimeTableBasic(userCode, lastYear, lastMonth);
        if (rows.isEmpty()) {
            return rows;
        }


        List<String> timeTableKeys = rows.stream()
                .map(TimeTableDTO::getTimeTableKey)
                .distinct()
                .toList();

        List<TimeTableDTO.StudentDTO> students =
                classRepository.findTimeTableStudents(userCode, timeTableKeys);

        Map<String, List<TimeTableDTO.StudentDTO>> studentMap =
                students.stream()
                        .collect(Collectors.groupingBy(
                                TimeTableDTO.StudentDTO::getTimeTableKey
                        ));

        rows.forEach(tt ->
                tt.setStudents(
                        studentMap.getOrDefault(
                                tt.getTimeTableKey(),
                                new ArrayList<>()
                        )
                )
        );

        return rows;
    }

    public List<TimeTableCode> findTimeTableCodeByUserCode(String userCode) {
        List<TimeTableCode> codes = classRepository.findTimeTableCodeByUserCode(userCode);
        return codes;

    }

    public int deleteTimeTableRow(String timeTableKey) {
        int result = classRepository.deleteTimeTableRow(timeTableKey);
        return result;
    }

    public List<ClassRespDTO.ComClassStudentDTO> findComClassStudentsByTimeTableKey(String timeTableKey, String userCode) {
        List<ClassRespDTO.ComClassStudentDTO> students = classRepository.findComClassStudentsByTimeTableKey(timeTableKey, userCode);

        return students;
    }

    public List<ClassRespDTO.ComClassStudentDTO> findComClassStudentsByUserCode(String userCode, String yy, String mm) {
        List<ClassRespDTO.ComClassStudentDTO> students = classRepository.findComClassStudentsByUserCode(userCode, yy, mm);

        return students;
    }

    // TODO:종합반 수업 추가
//    public int updateTimeTableAssign(ClassReqDTO.AssignUpdateDTO dto, String userCode, String centerCode) {
//
//        String timeTableKey = dto.getTimeTableKey();
//        int result = 0;
//
//        for (ClassReqDTO.AssignUpdateDTO.StudentInfo info : dto.getStudentInfos()) {
//
//            int updated = classRepository.updateTimeTableAssign(
//                    timeTableKey,
//                    info.getStudentId(),
//                    info.getClassKey(),
//                    info.getUnitKey()
//            );
//            Integer hanFee = paymentRepository.findFeeByClassKey(info.getClassKey(), centerCode);
//
//            boolean existsClass = studentRepository.existsStudentClass(info.getStudentId(), info.getYy(), info.getMm()) > 0;
//
//            TeacherAssign studentClass = TeacherAssign.builder()
//                    .student(Student.builder().studentId(info.getStudentId()).build())
//                    .assignHanClass(ClassCode.builder().classKey(info.getClassKey()).build())
//                    .assignHanTeacher(User.builder().userCode(userCode).build())
//                    .build();
//
//            if (!existsClass) {
//                studentRepository.insertStudentClass(studentClass);
//            } else {
//                studentRepository.updateStudentClass(studentClass);
//            }
//
//            String paymentKey = paymentRepository.findLatestPaymentKeyByStudent(info.getStudentId(), info.getYy(), info.getMm());
//
//            if (paymentKey != null) {
//                PaymentDetail eduDetail = PaymentDetail.builder()
//                        .payment(Payment.builder().paymentKey(paymentKey).build())
//                        .amount(hanFee)
//                        .classType("1") // 한자 수업
//                        .itemType("edu")
//                        .user(User.builder().userCode(userCode).build())
//                        .build();
//                paymentRepository.createPaymentDetail(eduDetail);
//
//                PaymentDetail bookDetail = PaymentDetail.builder()
//                        .payment(Payment.builder().paymentKey(paymentKey).build())
//                        .classType("1")
//                        .itemType("book")
//                        .user(User.builder().userCode(userCode).build())
//                        .build();
//                paymentRepository.createPaymentDetail(bookDetail);
//
//                paymentRepository.updateAmount(paymentKey);
//            }
//            if (updated > 0) {
//                result += updated;
//            }
//
//        }
//
//        return result;
//    }


    // ================ 수업 일지 서비스 =====================//
    public List<ClassRespDTO.RecordLabelDTO> getTimeTableByUserCode(String yy, String mm, String dayName, String userCode, String centerCode) {
        List<ClassRespDTO.RecordLabelDTO> response = classRepository.findTimeTableByUserCode(yy, mm, dayName, userCode, centerCode);
        return response;
    }

    public ClassRespDTO.RecordBundleDTO getTimeTableByKey(String userCode, String timeTableKey, String date, String classKey, String unitKey, String centerCode) {


        String week = calculateWeekFromDate(date, centerCode);

        List<RecordStudentDTO> students = classRepository.findRecordStudentByKey(timeTableKey, week);

        String noticeWeek = week.split("_")[1];

        List<ClassRespDTO.AfterClassRespDTO> afterClassList = new ArrayList<>();

        String currentYear = DateConfig.currentYearMonth().get("currentYear");
        String yy = currentYear.substring(2, 4);

        for (RecordStudentDTO s : students) {

            ClassRespDTO.AfterClassRespDTO notice =
                    classRepository.findAfterClassNotice(
                            s.getStudentId(),
                            timeTableKey,
                            noticeWeek
                    );

            if (notice != null) {
                afterClassList.add(notice);
            } else {
                ClassRespDTO.AfterClassRespDTO base =
                        classRepository.findAfterClass(
                                userCode, classKey, unitKey, week, timeTableKey, yy
                        );

                afterClassList.add(base);
            }
        }

        // 계산된 주차 정보도 함께 반환
        ClassRespDTO.RecordBundleDTO bundle = new ClassRespDTO.RecordBundleDTO();
        bundle.setStudents(students);
        bundle.setAfterClass(afterClassList);
        bundle.setWeek(week);  // 계산된 주차 반환

        return bundle;
    }

    // 날짜로부터 주차 계산 메서드
    private String calculateWeekFromDate(String dateStr, String centerCode) {
        try {

            java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
            String year = String.valueOf(date.getYear());
            String month = String.format("%02d", date.getMonthValue());
            List<ClassRespDTO.ClassWeekDTO> weeks = classRepository.getClassWeek(year, month, centerCode);
            if (weeks.isEmpty()) {
                return "ju_1";
            }

            for (ClassRespDTO.ClassWeekDTO week : weeks) {
                log.info("주차 확인: {}", week.getWeek());
                log.info("  월: {}, 화: {}, 수: {}, 목: {}, 금: {}, 토: {}, 일: {}",
                        week.getMon(), week.getTue(), week.getWed(),
                        week.getThu(), week.getFri(), week.getSat(), week.getSun());

                // 모든 요일 날짜를 리스트로 만들기
                List<Object> dates = Arrays.asList(
                        week.getMon(), week.getTue(), week.getWed(),
                        week.getThu(), week.getFri(), week.getSat(), week.getSun()
                );

                // 날짜 매칭
                for (Object dateObj : dates) {
                    if (dateObj == null) continue;

                    String weekDateStr = null;

                    // 타입별로 문자열 변환
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

            return weeks.get(0).getWeek();

        } catch (Exception e) {
            return "ju_1";
        }
    }

    public ClassRespDTO.BeforeClassRespDTO getBeforeClassContent(String classKey, String unitKey, String week, String timeTableKey) {

        String currentYear = DateConfig.currentYearMonth().get("currentYear");
        String yy = currentYear.substring(2, 4);
        ClassRespDTO.BeforeClassRespDTO response = classRepository.findBeforeClass(classKey, unitKey, week, timeTableKey, yy);
        return response;
    }

    public void insertBeforeClassNoticeList(List<ClassReqDTO.BeforeClassNoticeDTO> dtoList, String userCode) {

        for (ClassReqDTO.BeforeClassNoticeDTO dto : dtoList) {


            ClassReqDTO.BeforeClassNoticeDTO insertDTO = new ClassReqDTO.BeforeClassNoticeDTO();
            ClassRespDTO.RawClassDTO rawDTO = classRepository.findClassByTimeTableKey(dto.getTimeTableKey());
            Map<String, String> weekMap = Map.of("ju_1", "1주", "ju_2", "2주", "ju_3", "3주", "ju_4", "4주");
            String weekLabel = weekMap.getOrDefault(dto.getWeek(), dto.getWeek());
            Map<String, String> typeMap = Map.of("1", "S", "2", "I");
            String typeLabel = typeMap.getOrDefault(rawDTO.getClassType(), rawDTO.getClassType());
            String classLabel = String.format("%s %s %s | %s 선생님", rawDTO.getClassName(), rawDTO.getUnitName(), weekLabel, rawDTO.getUserName());

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

    // ================ 보강 관리 서비스 =====================//
    public List<RemedialDTO> findRemedialByUserNo(String year, String month, String userCode) {
        System.out.println(year);
        System.out.println(month);
        List<RemedialDTO> remedials = classRepository.findRemedialByUserCode(year, month, userCode);
        return remedials;
    }

    public int updateRemedialAction(UpdateRemedialDTO dto) {
        int result = classRepository.updateRemedialAction(dto.getRemedialKey(), dto.isAction());
        return result;
    }

    public void updateRemedialDate(UpdateRemedialDateDTO dto) {
        classRepository.updateRemedialDate(dto.getRemedialKey(), dto.getRemedialDate());
    }


    // ================ 월간 평가 서비스 =====================//
    public List<TimeTableLabelDTO> getMonthlyClassList(String userCode, String yy, String mm, String dayname, String centerCode) {
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCodeAndDayname(userCode, yy, mm, dayname, centerCode)
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

    public ClassRespDTO.ScoreResultDTO updateMonthlyScore(ClassMonthlyScoreDTO dto) {
        ClassMonthlyScoreDTO.MonthlyScoreDTO score = dto.getScores().get(0);

        classRepository.updateMonthlyScore(dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm(), score);

        // 오답 개수 확인
        int wrongCount = 0;
        wrongCount += (score.isQuestion1()) ? 0 : 1;  // false면 오답
        wrongCount += (score.isQuestion2()) ? 0 : 1;
        wrongCount += (score.isQuestion3()) ? 0 : 1;
        wrongCount += (score.isQuestion4()) ? 0 : 1;
        wrongCount += (score.isQuestion5()) ? 0 : 1;
        wrongCount += (score.isQuestion6()) ? 0 : 1;
        wrongCount += (score.isQuestion7()) ? 0 : 1;
        wrongCount += (score.isQuestion8()) ? 0 : 1;

        Map<String, String> monthlyFeedback = classRepository.getMonthlyFeedback(
                dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm());

        String feedback;

        if (wrongCount == 0) {
            // 모두 정답일 경우: 정답 코멘트 리스트에서 랜덤 2개 선택
            List<String> correctComments = classRepository.getMonthlyAllCorrectFeedback(
                    dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm());

            if (correctComments.size() >= 2) {
                Collections.shuffle(correctComments);
                feedback = correctComments.get(0) + "\n또한 " + correctComments.get(1);
            } else {
                feedback = String.join("\n", correctComments);
            }

        } else {
            // 오답이 있을 경우: 기존 로직 (정답 1개 + 오답 1개)
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
            String mm
    ) {
        for (FcmDTO.MonthlyFcmDTO.StudentDTO student : students) {
            // 발송 성공한 학생만 업데이트
            if (successStudentIds.contains(student.getStudentId())) {
                int updatedRows = classRepository.updateMonthlySendStatus(
                        student.getStudentId(),
                        student.getTimeTableKey(),
                        yy,
                        mm
                );

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
        ClassAppRespDTO.MonthlyHaniRespDTO resp = classRepository.findAppInfantHani(studentId, yy, mm);

        return classRepository.findAppInfantHani(studentId, yy, mm);

    }

    public ClassAppRespDTO.MonthlyBukiRespDTO findAppInfantBuki(ClassAppReqDTO.MonthlyResultReqDTO reqDTO) {
        String studentId = reqDTO.getId();
        String yy = reqDTO.getYm().substring(0, 4);
        String mm = reqDTO.getYm().substring(4, 6);

        return classRepository.findAppInfantBuki(studentId, yy, mm);
    }

    public List<TimeTableLabelDTO> findInfantClassLabel(ClassReqDTO.InfantClassLabelsDTO dto) {

        List<TimeTableLabelDTO> labels = classRepository.findInfantClassLabel(dto.getUserCode(), dto.getYy(), dto.getMm())
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
            ClassRespDTO.InfantHanDTO infantHanDTO =
                    classRepository.findInfantHan(dto.getClassKey(), dto.getUnitKey(), dto.getYy());

            if (infantHanDTO == null) {
                log.warn("InfantHanDTO 조회 결과 없음. classKey={}, unitKey={}, year={}",
                        dto.getClassKey(), dto.getUnitKey(), dto.getYy());
                return null;
            }

            List<ClassRespDTO.InfantHanDTO.StudentInfo> students =
                    classRepository.findInfantHanStudents(dto.getTimeTableKey());

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
            ClassRespDTO.InfantBookDTO infantBookDTO =
                    classRepository.findInfantBook(dto.getClassKey(), dto.getUnitKey(), dto.getYy());

            if (infantBookDTO == null) {
                log.warn("InfantBookDTO 조회 결과 없음. classKey={}, unitKey={}, year={}",
                        dto.getClassKey(), dto.getUnitKey(), dto.getYy());
                return null;
            }

            List<ClassRespDTO.InfantBookDTO.StudentInfo> students =
                    classRepository.findInfantBookStudents(dto.getTimeTableKey());

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

    public void saveInfantSendHistory(String classType, String timeTableKey, String senderUser, String centerCode, List<String> studentIds) {

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
                            s.getStudentId(), reqDTO.getTimeTableKey()
                    );
                } else {
                    existingIsSend = classRepository.findInfantBookIsSend(
                            s.getStudentId(), reqDTO.getTimeTableKey()
                    );
                }

                // ✅ 발행이면 무조건 true, 저장이면 기존 값 유지
                Boolean finalIsSend = reqDTO.getIsSend() ? true : (existingIsSend != null && existingIsSend);

                if ("HAN".equals(reqDTO.getType())) {
                    classRepository.updateInfantHanNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, finalIsSend
                    );
                } else {
                    classRepository.updateInfantBookNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, finalIsSend
                    );
                }

            } else {
                // INSERT
                if ("HAN".equals(reqDTO.getType())) {
                    classRepository.insertInfantHanNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, reqDTO.getIsSend()
                    );
                } else {
                    classRepository.insertInfantBookNotice(
                            reqDTO, centerCode, userCode, s.getStudentId(), sendId, reqDTO.getIsSend()
                    );
                }
            }
        }
    }

    // ========================================  APP  ======================================== //
    public List<ClassAppRespDTO.ClassInfoRespDTO> getClassInfo(String studentId, String yy, String mm) {

        List<ClassAppRespDTO.ClassInfoRespDTO> respDTOs = classRepository.findClassInfoByStudentId(studentId, yy, mm);

        return respDTOs;

    }

    public List<ClassAppRespDTO.BeforeClassRespDTO> getBeforeClass(String studentId, int count) {

        List<ClassAppRespDTO.BeforeClassRespDTO> respDTOS = classRepository.findBeforeClassByStudentId(studentId, count);

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
        String eMonth = dto.getSym().substring(4, 6);
        return classRepository.findClinicTotal(dto.getId(), sYear, sMonth, eYear, eMonth);
    }

    public List<ClassAppRespDTO.AfterClassDetailRespDTO> getAfterClassDetail(ClassAppReqDTO.LearningContentDetailReqDTO dto) {
        List<ClassAppRespDTO.AfterClassDetailRespDTO> response = classRepository.findAfterClassDetail(dto.getId(), dto.getGamok(), dto.getYyyy(), dto.getMm(), dto.getJu());
        return response;

    }
}

