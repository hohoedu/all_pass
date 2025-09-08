package com.hohoedu.all_pass.class_instance;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.hohoedu.all_pass.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.vo.Constants;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.AddStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassMonthlyScoreDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.StudentAttendanceDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.UpdateRemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.UpdateRemedialDateDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.InitRecordDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RecordStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableLabelDTO;
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
import com.hohoedu.all_pass.user.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class ClassService {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;
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
                classReqDTO.getDayname());

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
            classRepository.createTimeTableCode(entity);
            System.out.println("set 전" + classReqDTO.getTimeTableCode());
            classReqDTO.setTimeTableCode(timeTableKey);
            System.out.println("set 후" + classReqDTO.getTimeTableCode());
            classRepository.registerClass(classReqDTO);

            return "success-register";
        } else {

            classRepository.updateClass(classReqDTO, timeTable.getTimeTableNo());
            return "success-update";

        }

    }

    private String createTimeTableLabel(ClassReqDTO.ClassRegisterDTO dto) {
        UnitCode unitCode = unitCodeJpaRepository.findById(dto.getUnitNo()).orElseThrow();
        ClassCode classCode = classCodeJpaRepository.findById(dto.getClassNo()).orElseThrow();
        String dayName = Constants.DAY_NAME_MAP.getOrDefault(dto.getDayname(), dto.getDayname());

        return String.format("%s %s ~ %s %s %s",
                dayName,
                dto.getStartTime(),
                dto.getEndTime(),
                classCode.getClassName(),
                unitCode.getUnitName());
    }

    public List<TimeTableDTO> findTimeTableWithStudents(String year, String month) {

        List<TimeTableDTO> tables = classRepository.findTimeTableBasic("DAE001cos", year, month);

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
        int count = classRepository.countByTimeTableKey(dto.getTimeTableKey());
        if (count >= 8) {
            return false;
        }
        classRepository.addStudent(dto);
        classRepository.insertMonthlyScore(dto, yy, mm);
        return true;
    }

    public void deleteStudent(Integer assignNo) {
        // assignJpaRepository.findById(assignNo).orElseThrow(() -> new
        // Exception404("학생을 찾을 수 없습니다."));

        classRepository.deleteByAssignNo(assignNo);
        // classRepository.deleteMonthlyScore(assignNo);
    }

    public List<TimeTableDTO> getLastTimeTable() {
        List<TimeTableDTO> tables = classRepository.findTimeTableBasic("DAE001cos", "2025", "09");
        return tables;
    }

    ;

    public List<TimeTableCode> findTimeTableCodeByUserNo(Integer userNo) {
        List<TimeTableCode> codes = classRepository.findTimeTableCodeByUserNo(userNo);
        return codes;

    }

    // ================ 수업 일지 서비스 =====================//
    public List<InitRecordDTO> getTimeTableByDate(String yy, String mm, String dayName, String userCode) {
        List<InitRecordDTO> response = classRepository.findTimeTableByDate(yy, mm, dayName, userCode);
        return response;
    }

    public List<RecordStudentDTO> getTimeTableByClassCode(String classCode, String date) {
        List<RecordStudentDTO> response = classRepository.findTimeTableByClassCode(classCode, date);
        return response;
    }

    // ================ 보강 관리 서비스 =====================//
    public List<RemedialDTO> findRemedialByUserNo(String year, String month) {
        System.out.println(year);
        System.out.println(month);
        List<RemedialDTO> remedials = classRepository.findRemedialByUserNo(year, month);
        return remedials;
    }

    public void updateRemedialAction(UpdateRemedialDTO dto) {
        classRepository.updateRemedialAction(dto.getRemedialNo(), dto.isAction());
    }

    public void updateRemedialDate(UpdateRemedialDateDTO dto) {
        classRepository.updateRemedialDate(dto.getRemedialNo(), dto.getRemedialDate());
    }

    public boolean insertStudentAttendance(StudentAttendanceDTO dto, Student student) {

        Integer count = classRepository.countByStudentAndDate(student.getStudentId(), dto.getYmd());
        if (count != null && count > 0) {
            return false;
        }

        StudentAttendance sa = StudentAttendance.builder()
                .inTime(dto.getHhmm())
                .attendanceDate(dto.getYmd())
                .student(Student.builder().id(student.getId()).build())
                .attendanceCode(AttendanceCode.builder().attendanceKey("").build())
                .center(Center.builder().centerCode(dto.getCenterCode()).build())
                .build();

        classRepository.insertStudentAttendance(sa);
        return true;
    }

    public boolean updateStudentAttendance(StudentAttendanceDTO dto, Student student) {
        StudentAttendance sa = classRepository.findByStudentAndDate(student.getStudentId(), dto.getYmd());
        if (sa == null) {
            System.out.println("등원 기록 없음");
            return false;
        }
        if (sa.getOutTime() != null) {
            System.out.println("이미 처리 됨");
            return false;
        }
        int updated = classRepository.updateStudentAttendance(student.getStudentId(), dto.getYmd(), dto.getHhmm());
        return updated > 0;
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
}
