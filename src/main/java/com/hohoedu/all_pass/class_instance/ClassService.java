package com.hohoedu.all_pass.class_instance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hohoedu.all_pass.class_instance._dto.app.ClassAppRespDTO;
import org.springframework.stereotype.Service;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.vo.Constants;
import com.hohoedu.all_pass.center.Center;
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
import com.hohoedu.all_pass.class_instance.model.AttendanceCode;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.StudentAttendance;
import com.hohoedu.all_pass.class_instance.model.TimeTableCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.class_instance.repository.ClassCodeJpaRepository;
import com.hohoedu.all_pass.class_instance.repository.ClassRepository;
import com.hohoedu.all_pass.class_instance.repository.UnitCodeJpaRepository;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.student.repository.GradeJpaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UnitCodeJpaRepository unitCodeJpaRepository;
    private final ClassCodeJpaRepository classCodeJpaRepository;
    private final GradeJpaRepository gradeJpaRepository;
    private final DateConfig dateConfig;
    // private final TimeTableAssignJpaRepository assignJpaRepository;

    public List<ClassCode> findClassCode() {
        List<ClassCode> classCodes = classCodeJpaRepository.findAll();
        return classCodes;
    }

    public List<UnitCode> findUnitCode() {
        List<UnitCode> unitCodes = unitCodeJpaRepository.findAll();
        return unitCodes;
    }

    public List<GradeCode> findGrade() {
        List<GradeCode> grades = gradeJpaRepository.findAll();
        return grades;
    }

    public List<TimeTableLabelDTO> getClassLabel(String userCode) {
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCode(userCode, yy, mm);
        return labels;
    }

    public String registerClass(ClassReqDTO.ClassRegisterDTO classReqDTO) {

        TimeTableDTO timeTable = classRepository.existsByYearAndMonthAndPeriodNo(
                classReqDTO.getPeriodNo(),
                classReqDTO.getYy(),
                classReqDTO.getMm(),
                classReqDTO.getDayname(),
                classReqDTO.getUserCode());

        boolean isEmpty = timeTable == null;

        if (isEmpty) {
            String label = createTimeTableLabel(classReqDTO);
            String timeTableKey = UUID.randomUUID().toString();
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
            String label = createTimeTableLabel(classReqDTO);
            System.out.println(label);
            int result = classRepository.updateClass(classReqDTO, timeTable.getTimeTableKey(), classReqDTO.getUserCode());
            classRepository.updateLabel(label, timeTable.getTimeTableKey());
            System.out.println("result : " + result);
            if (result <= 0) {
                System.out.println("실패");
                return "fail-update";
            }
            System.out.println("성공");
            return "success-update";

        }
    }

    private String createTimeTableLabel(ClassReqDTO.ClassRegisterDTO dto) {
        UnitCode unitCode = unitCodeJpaRepository.findByUnitKey(dto.getUnitKey()).orElseThrow();
        ClassCode classCode = classCodeJpaRepository.findByClassKey(dto.getClassKey()).orElseThrow();
        String dayName = Constants.DAY_NAME_MAP.getOrDefault(dto.getDayname(), dto.getDayname());

        return String.format("%s %s ~ %s %s %s",
                dayName,
                dto.getStartTime(),
                dto.getEndTime(),
                classCode.getClassName(),
                unitCode.getUnitName());
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

    public boolean addStudent(AddStudentDTO dto) {
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");

        dto.setCenterCode("DAE001");

        int count = classRepository.countByTimeTableKey(dto.getTimeTableKey());
        if (count >= 8) {
            return false;
        }
        classRepository.addStudent(dto);
        classRepository.insertMonthlyScore(dto.getStudentId(), yy, mm, dto.getTimeTableKey());
        classRepository.createAttendance(dto.getStudentId(), dto.getTimeTableKey(), dto.getCenterCode());

        return true;
    }

    public void deleteStudent(String timeTableKey, String studentId) {
        // assignJpaRepository.findById(assignNo).orElseThrow(() -> new
        // Exception404("학생을 찾을 수 없습니다."));

        classRepository.deleteByKeyAndStudentId(timeTableKey, studentId);
        // classRepository.deleteMonthlyScore(assignNo);
    }

    public List<TimeTableDTO> getLastTimeTable(String userCode) {
        List<TimeTableDTO> tables = classRepository.findTimeTableBasic(userCode, "2025", "09");
        return tables;
    }

    ;

    public List<TimeTableCode> findTimeTableCodeByUserNo(Integer userNo) {
        List<TimeTableCode> codes = classRepository.findTimeTableCodeByUserNo(userNo);
        return codes;

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

    public ClassRespDTO.BeforeClassRespDTO getBeforeClassContent(String classKey, String unitKey, String week,
                                                                 String timeTableKey) {
        ClassRespDTO.BeforeClassRespDTO response = classRepository.findBeforeClass(classKey, unitKey, week, timeTableKey);
        return response;
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
    public List<TimeTableLabelDTO> getLabelsByYM(String yy, String mm) {
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCode("all", yy, mm);

        return labels.stream()
                .map(dto -> {
                    String label = dto.getClassLabel();
                    String classTime = label;
                    String classSubject = "";
                    if (label != null) {
                        int idx = label.indexOf(" ", label.indexOf("~") + 2); // "~" 뒤 첫 번째 공백
                        if (idx > -1) {
                            classTime = label.substring(0, idx).trim();
                            classSubject = label.substring(idx + 1).trim();
                        }
                    }
                    dto.setClassTime(classTime);
                    dto.setClassSubject(classSubject);
                    return dto;
                })
                .toList();
    }

    public List<TimeTableLabelDTO> getLabelsByUserNoAndYM(String userCode, String yy, String mm) {
        List<TimeTableLabelDTO> labels = classRepository.findClassLabelByUserCode(userCode, yy, mm);

        return labels.stream()
                .map(dto -> {
                    String label = dto.getClassLabel();
                    String classTime = label;
                    String classSubject = "";
                    if (label != null) {
                        int idx = label.indexOf(" ", label.indexOf("~") + 2); // "~" 뒤 첫 번째 공백
                        if (idx > -1) {
                            classTime = label.substring(0, idx).trim();
                            classSubject = label.substring(idx + 1).trim();
                        }
                    }
                    dto.setClassTime(classTime);
                    dto.setClassSubject(classSubject);
                    return dto;
                })
                .toList();
    }

    public List<TimeTableLabelDTO> getMonthlyClassList(String userNo, String yy, String mm) {
        return classRepository.findClassLabelByUserCode(userNo, yy, mm)
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
    }

    public List<MonthlyStudentDTO> getMonthlyClassDetail(String classCode) {
        List<MonthlyStudentDTO> students = classRepository.findStudentByClassCode(classCode);
        return students;
    }

    public boolean updateMonthlyScore(ClassMonthlyScoreDTO dto) {
        if (dto.getScores() == null || dto.getScores().isEmpty()) {
            return false;
        }
        ClassMonthlyScoreDTO.MonthlyScoreDTO score = dto.getScores().get(0);

        int rows = classRepository.updateMonthlyScore(
                dto.getStudentId(),
                dto.getClassCode(),
                dto.getYy(),
                dto.getMm(),
                score);

        return rows > 0;
    }

    public List<ClassAppRespDTO.ClassInfoRespDTO> getClassInfo(String studentId, String yy, String mm) {

        List<ClassAppRespDTO.ClassInfoRespDTO> respDTOs = classRepository.findClassInfoByStudentId(studentId, yy, mm);

        return respDTOs;

    }
}
