package com.hohoedu.all_pass.class_instance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import com.hohoedu.all_pass.class_instance.model.*;
import com.hohoedu.all_pass.class_instance.model.base_data.MonthlyFeedback;
import com.hohoedu.all_pass.class_instance.repository.ClassUnitMapJpaRepository;
import com.hohoedu.all_pass.payment.Payment;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment.model.PaymentDetail;
import com.hohoedu.all_pass.payment.repository.PaymentRepository;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.StudentClass;
import com.hohoedu.all_pass.student.repository.StudentRepository;
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
    private final DateConfig dateConfig;
    private final ClassUnitMapJpaRepository classUnitMapJpaRepository;
    private final StudentService studentService;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;

    public List<ClassRespDTO.MainClassSummaryDTO> getClassSummary(String centerCode, String userCode) {

        String today = dateConfig.currentYearMonth().get("today");
        String year = dateConfig.currentYearMonth().get("currentYear");
        String month = dateConfig.currentYearMonth().get("currentMonth");
        String dayname = dateConfig.currentYearMonth().get("currentDayName");

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

    public void createClassWeek(ClassReqDTO.SetWeekDTO dto, String centerCode) {
        dto.setCenterCode(centerCode);

        // 기본값 null 넣기 위한 초기화
        String ju1Start = null, ju1End = null;
        String ju2Start = null, ju2End = null;
        String ju3Start = null, ju3End = null;
        String ju4Start = null, ju4End = null;

        for (ClassReqDTO.SetWeekDTO.WeekDTO w : dto.getWeeks()) {
            if (w == null) continue;

            switch (w.getWeekNo()) {
                case 1 -> {
                    ju1Start = w.getStart();
                    ju1End = w.getEnd();
                }
                case 2 -> {
                    ju2Start = w.getStart();
                    ju2End = w.getEnd();
                }
                case 3 -> {
                    ju3Start = w.getStart();
                    ju3End = w.getEnd();
                }
                case 4 -> {
                    ju4Start = w.getStart();
                    ju4End = w.getEnd();
                }
            }
        }

        ClassWeek week = ClassWeek.builder()
                .year(dto.getYear())
                .month(dto.getMonth())
                .ju1Start(ju1Start)
                .ju1End(ju1End)
                .ju2Start(ju2Start)
                .ju2End(ju2End)
                .ju3Start(ju3Start)
                .ju3End(ju3End)
                .ju4Start(ju4Start)
                .ju4End(ju4End)
                .center(Center.builder().centerCode(dto.getCenterCode()).build())
                .build();

        classRepository.insertClassWeek(week);
    }

    public ClassRespDTO.ClassWeekDTO findClassWeek(ClassReqDTO.GetWeekDTO dto) {

        return classRepository.findClassWeek(dto.getYear(), dto.getMonth(), dto.getCenterCode());
    }

    public void updateClassWeek(ClassReqDTO.SetWeekDTO dto, String centerCode) {
        dto.setCenterCode(centerCode);


        classRepository.updateClassWeek(dto);

    }

    // 수업 코드 테이블 조회 서비스 (시간표 등록)
    public List<ClassCode> findClassCode() {
        List<ClassCode> classCodes = classCodeJpaRepository.findAll();
        return classCodes;
    }

    // 수업 코드 - 유닛 매핑 테이블 조회 서비스 (시간표 등록)
    public Map<String, List<UnitCode>> findClassUnits() {
        Map<String, List<UnitCode>> result = new HashMap<>();

        List<ClassUnitMap> allMappings = classUnitMapJpaRepository.findAllWithUnitCode();

        result = allMappings.stream()
                .collect(Collectors.groupingBy(
                        map -> map.getClassCode().getClassKey(),
                        Collectors.mapping(ClassUnitMap::getUnitCode, Collectors.toList())
                ));

        return result;
    }

    // 학년 및 연령 테이블 조회 서비스 (시간표 등록)
    public List<GradeCode> findGrade() {
        List<GradeCode> grades = gradeJpaRepository.findAll();
        return grades;
    }

    public List<TimeTableLabelDTO> getAllClassLabel(String userCode) {
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCode(userCode, yy, mm, "0");
        return labels;
    }

    // 클래스 라벨 테이블 조회 (학생 정보 메인, 전입 전출 메인)
    public List<TimeTableLabelDTO> getClassLabel(String userCode) {
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");
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
            System.out.println("유닛코드" + classReqDTO.getUnitKey());
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

    public List<TimeTableDTO> findTableViewWithStudents(String year, String month, String userCode) {

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
        return tables;
    }

    // 학생 수업 등록
    public boolean addStudent(AddStudentDTO dto, String userCode, String centerCode) {
        try {

            int count = classRepository.countByTimeTableKey(dto.getTimeTableKey());

            if (count >= 8) {
                return false;
            }

            classRepository.addStudent(dto);

            classRepository.insertMonthlyScore(dto.getStudentId(), dto.getYy(), dto.getMm(), dto.getTimeTableKey());

            classRepository.createAttendance(dto.getStudentId(), dto.getTimeTableKey(), centerCode, dto.getYy(), dto.getMm());


        } catch (Exception e) {
            System.out.println("=====================" + e.getMessage() + "====================================");
        }
        return true;
    }

    public ClassRespDTO.ClassInfoDTO findClassInfoByTimeTableKeyAndStudentId(String timeTableKey, String studentId, String centerCode) {
        ClassRespDTO.ClassInfoDTO classInfo = classRepository.findClassInfoByTimeTableKeyAndStudentId(timeTableKey, studentId, centerCode);
        return classInfo;
    }

    public void deleteStudent(String timeTableKey, String studentId) {
        classRepository.deleteByKeyAndStudentId(timeTableKey, studentId);
    }

    public List<TimeTableDTO> getLastTimeTable(String userCode) {

        LocalDate now = LocalDate.now();
        LocalDate prevMonth = now.minusMonths(1);
        String lastYear = String.valueOf(prevMonth.getYear());
        String lastMonth = String.format("%02d", prevMonth.getMonthValue());

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, lastYear, lastMonth);
        return tables;
    }

    public List<TimeTableCode> findTimeTableCodeByUserNo(Integer userNo) {
        List<TimeTableCode> codes = classRepository.findTimeTableCodeByUserNo(userNo);
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

    public int updateTimeTableAssign(ClassReqDTO.AssignUpdateDTO dto, String userCode, String centerCode) {

        String timeTableKey = dto.getTimeTableKey();
        int result = 0;

        for (ClassReqDTO.AssignUpdateDTO.StudentInfo info : dto.getStudentInfos()) {

            int updated = classRepository.updateTimeTableAssign(
                    timeTableKey,
                    info.getStudentId(),
                    info.getClassKey(),
                    info.getUnitKey()
            );
            Integer hanMaterialFee = 20000;
            Integer hanFee = paymentRepository.findFeeByClassKey(info.getClassKey(), centerCode);

            boolean existsClass = studentRepository.existsStudentClass(info.getStudentId(), info.getYy(), info.getMm()) > 0;

            StudentClass studentClass = StudentClass.builder()
                    .student(Student.builder().studentId(info.getStudentId()).build())
                    .yy(info.getYy())
                    .mm(info.getMm())
                    .hanClassCode(ClassCode.builder().classKey(info.getClassKey()).build())
                    .hanUser(User.builder().userCode(userCode).build())
                    .hanFee(hanFee)
                    .hanMaterialFee(hanMaterialFee)
                    .build();

            if (!existsClass) {
                studentRepository.insertStudentClass(studentClass);
            } else {
                studentRepository.updateStudentClass(studentClass);
            }

            String paymentKey = paymentRepository.findLatestPaymentKeyByStudent(info.getStudentId(), info.getYy(), info.getMm());

            if (paymentKey != null) {
                PaymentDetail eduDetail = PaymentDetail.builder()
                        .payment(Payment.builder().paymentKey(paymentKey).build())
                        .amount(hanFee)
                        .classType("1") // 한자 수업
                        .itemType("edu")
                        .user(User.builder().userCode(userCode).build())
                        .build();
                paymentRepository.createPaymentDetail(eduDetail);

                PaymentDetail bookDetail = PaymentDetail.builder()
                        .payment(Payment.builder().paymentKey(paymentKey).build())
                        .amount(hanMaterialFee)
                        .classType("1")
                        .itemType("book")
                        .user(User.builder().userCode(userCode).build())
                        .build();
                paymentRepository.createPaymentDetail(bookDetail);

                paymentRepository.updateAmount(paymentKey);
            }
            if (updated > 0) {
                result += updated;
            }

        }

        return result;
    }


    // ================ 수업 일지 서비스 =====================//
    public List<ClassRespDTO.RecordLabelDTO> getTimeTableByUserCode(String yy, String mm, String dayName, String userCode, String centerCode) {
        List<ClassRespDTO.RecordLabelDTO> response = classRepository.findTimeTableByUserCode(yy, mm, dayName, userCode, centerCode);
        return response;
    }

    public ClassRespDTO.RecordBundleDTO getTimeTableByKey(String timeTableKey, String week, String classKey, String unitKey) {
        List<RecordStudentDTO> students = classRepository.findRecordStudentByKey(timeTableKey, week);
        ClassRespDTO.AfterClassRespDTO afterClassContent = classRepository.findAfterClass(classKey, unitKey, week, timeTableKey);

        ClassRespDTO.RecordBundleDTO response = new ClassRespDTO.RecordBundleDTO(students, afterClassContent);

        return response;
    }

    public ClassRespDTO.BeforeClassRespDTO getBeforeClassContent(String classKey, String unitKey, String week, String timeTableKey) {
        ClassRespDTO.BeforeClassRespDTO response = classRepository.findBeforeClass(classKey, unitKey, week, timeTableKey);
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
            insertDTO.setClassType(typeLabel);
            insertDTO.setClassLabel(classLabel);
            classRepository.insertAfterClassNotice(insertDTO);

        }

    }

    public void updateAfterSend(String studentId, String timeTableKey, String week) {
        classRepository.updateAfterSend(studentId, timeTableKey, week);
    }

    // ================ 보강 관리 서비스 =====================//
    public List<RemedialDTO> findRemedialByUserNo(String year, String month) {
        System.out.println(year);
        System.out.println(month);
        List<RemedialDTO> remedials = classRepository.findRemedialByUserNo(year, month);
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
    public List<TimeTableLabelDTO> getMonthlyClassList(String userCode, String yy, String mm, String dayname) {

        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCodeAndDayname(userCode, yy, mm, dayname)
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
//        if (dto.getScores() == null || dto.getScores().isEmpty()) {
//            return false;
//        }
        ClassMonthlyScoreDTO.MonthlyScoreDTO score = dto.getScores().get(0);

        classRepository.updateMonthlyScore(dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm(), score);
        Map<String, String> monthlyFeedback = classRepository.getMonthlyFeedback(dto.getStudentId(), dto.getTimeTableKey(), dto.getYy(), dto.getMm());

        String feedback =
                Optional.ofNullable(monthlyFeedback.get("topCorrectMent")).orElse("")
                        + "\n" +
                        Optional.ofNullable(monthlyFeedback.get("topWrongMent")).orElse("");

        ClassRespDTO.ScoreResultDTO response = new ClassRespDTO.ScoreResultDTO();
        response.setTimeTableKey(dto.getTimeTableKey());
        response.setStudentId(dto.getStudentId());
        response.setStudentName(monthlyFeedback.get("studentName"));
        response.setScoreResult(feedback);

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
                log.warn("InfantBookTO 조회 결과 없음. classKey={}, unitKey={}, year={}",
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
            int cnt = 0;
            if (reqDTO.getType().equals("HAN")) {
                cnt = classRepository.countInfantHan(
                        s.getStudentId(),
                        reqDTO.getTimeTableKey());
            }
            if (reqDTO.getType().equals("BOOK")) {
                cnt = classRepository.countInfantBook(
                        s.getStudentId(),
                        reqDTO.getTimeTableKey());
            }

            log.info("cnt={}", cnt);
            if (cnt > 0) continue;

            Integer sendId = classRepository.findInfantSendId(
                    s.getStudentId(),
                    reqDTO.getTimeTableKey()
            );

            if (sendId == null) {
                throw new IllegalStateException("sendHistory가 없습니다.");
            }

            // HAN or BOOK 분기하여 insert
            if ("HAN".equals(reqDTO.getType())) {
                classRepository.insertInfantHanNotice(
                        reqDTO, centerCode, userCode, s.getStudentId(), sendId
                );
            } else {
                classRepository.insertInfantBookNotice(
                        reqDTO, centerCode, userCode, s.getStudentId(), sendId
                );
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

}

