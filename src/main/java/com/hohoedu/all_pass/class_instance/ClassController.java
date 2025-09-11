package com.hohoedu.all_pass.class_instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO;

import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.BeforeClassDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassMonthlyByClassCodeDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassMonthlyByMonthDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassMonthlyScoreDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.ClassRecordReqDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.StudentAttendanceDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.UpdateRemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassReqDTO.UpdateRemedialDateDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.BeforeClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RecordLabelDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RecordStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.class_instance.model.TimeTableCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final DateConfig dateConfig;
    private final ClassService classService;
    private final StudentService studentService;

    @PostMapping("/register")
    public ResponseEntity<?> registerClass(@RequestBody List<ClassReqDTO.ClassRegisterDTO> reqDTO, HttpSession session) {
        try {


            for (ClassReqDTO.ClassRegisterDTO req : reqDTO) {
                classService.registerClass(req);
            }

            return ResponseEntity.ok(ApiUtils.success("200"));

        } catch (Exception e) {
            System.out.println("================오류============");
            System.out.println("================" + e.getMessage() + "============");
            return ResponseEntity.ok(ApiUtils.error("시간표 등록 실패", HttpStatus.INTERNAL_SERVER_ERROR));

        }
    }

    @PostMapping("/add_student")
    @ResponseBody
    public ResponseEntity<?> timeTableAssginStudent(@RequestBody ClassReqDTO.AddStudentList reqDTO) {

        try {
            boolean isSuccess = reqDTO.getAssignments().stream()
                    .allMatch(dto -> classService.addStudent(dto));
            if (isSuccess) {
                return ResponseEntity
                        .ok(ApiUtils.success(true));
            } else {
                return ResponseEntity
                        .ok(ApiUtils.error("최대 8명까지 등록 가능합니다.", HttpStatus.OK));
            }
        } catch (DataIntegrityViolationException ex) {
            System.out.println("====================================================================");
            System.out.println("==          " + ex.getMessage());
            System.out.println("====================================================================");
            return ResponseEntity
                    .ok(ApiUtils.error("오류가 발생했습니다.", HttpStatus.OK));
        }
    }

    @PostMapping("/delete_student")
    public ResponseEntity<?> timeTableDeleteStudent(@RequestBody Map<String, String> request) {
        String timeTableKey = request.get("timeTableKey");
        String studentId = request.get("studentId");
        System.out.println(timeTableKey);
        System.out.println(studentId);
        classService.deleteStudent(timeTableKey, studentId);
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @GetMapping("/api/load_time_table")
    public ResponseEntity<?> loadTimeTable() {
        List<TimeTableDTO> tables = classService.getLastTimeTable();
        return ResponseEntity.ok(ApiUtils.success(tables));
    }

    @GetMapping("/api/timetable/{userNo}")
    public ResponseEntity<?> findTimeTableCodeByUserNo(@PathVariable("userNo") Integer userNo) {
        List<TimeTableCode> codes = classService.findTimeTableCodeByUserNo(userNo);
        return ResponseEntity.ok(ApiUtils.success(codes));
    }

    // ================ 수업 일지 컨트롤러 =====================//
    @PostMapping("/api/record/label")
    public ResponseEntity<?> findRecordLabel(@RequestBody ClassRecordReqDTO dto) {
        List<RecordLabelDTO> labels = classService.getTimeTableByUserCode(dto.getYy(), dto.getMm(), dto.getDay(), dto.getUserCode());
        System.out.println("=================================================");
        System.out.println("==          " + labels);
        System.out.println("=================================================");
        return ResponseEntity.ok(ApiUtils.success(labels));
    }

    @PostMapping("/api/record/student")
    public ResponseEntity<?> findRecordByClass(@RequestBody ClassRecordReqDTO dto) {
        List<RecordStudentDTO> students = classService.getTimeTableByKey(dto.getTimeTableKey(), dto.getWeek());
        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @PostMapping("/api/record/before-class")
    public ResponseEntity<?> findRecordBeforeClass(@RequestBody BeforeClassDTO dto) {
        BeforeClassRespDTO response = classService.getBeforeClassContent(dto.getClassKey(), dto.getUnitKey(),
                dto.getWeek(),
                dto.getTimeTableKey());
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    // ================ 보강 관리 컨트롤러 =====================//
    @PostMapping("/remedial/update")
    public ResponseEntity<?> updateRemedial(@RequestBody UpdateRemedialDTO dto,
                                            @RequestParam(value = "year") String year,
                                            @RequestParam(value = "month") String month) {
        int response = classService.updateRemedialAction(dto);
        System.out.println(response);
        List<RemedialDTO> remedials = classService.findRemedialByUserNo(year, month);
        List<RemedialDTO> rightRemedials = remedials.stream()
                .filter(RemedialDTO::isAction)
                .toList();

        List<RemedialDTO> leftRemedials = remedials.stream()
                .filter(r -> !r.isAction())
                .toList();
        Map<String, Object> result = new HashMap<>();
        result.put("leftRemedials", leftRemedials);
        result.put("rightRemedials", rightRemedials);

        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/remedial/updateDate")
    public ResponseEntity<?> updateRemedialDate(@RequestBody UpdateRemedialDateDTO dto) {
        classService.updateRemedialDate(dto);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> studentAttendance(@RequestBody StudentAttendanceDTO dto) {

        Student studentInfo = studentService.findByAppId(dto.getAppId());

        if ("come".equals(dto.getAttendType())) {
            boolean inserted = classService.insertStudentAttendance(dto, studentInfo);
            if (inserted) {
                return ResponseEntity.ok("등원 완료");
            } else {
                return ResponseEntity.badRequest().body("이미 오늘 출석 기록이 있습니다.");
            }
        } else {
            boolean updated = classService.updateStudentAttendance(dto, studentInfo);
            if (updated) {
                return ResponseEntity.ok("하원 완료");
            } else {
                return ResponseEntity.badRequest().body("출석 기록이 없어 하원 처리 불가");
            }
        }
    }

    // 월간 평가 (초등)
    @PostMapping("/api/monthly/by-month")
    public ResponseEntity<?> findTimeTableLabel(@RequestBody ClassMonthlyByMonthDTO dto) {

        List<TimeTableLabelDTO> label = classService.getLabelsByUserNoAndYM("2", dto.getYy(), dto.getMm());

        return ResponseEntity.ok(ApiUtils.success(label));
    }

    @GetMapping("/api/monthly/{teacherNo}")
    public ResponseEntity<?> findTimeTableLabel(@PathVariable("teacherNo") String userNo) {

        List<TimeTableLabelDTO> label = classService.getMonthlyClassList(
                userNo,
                dateConfig.currentYearMonth().get("currentYear"),
                dateConfig.currentYearMonth().get("currentMonth"));

        return ResponseEntity.ok(ApiUtils.success(label));
    }

    @PostMapping("/api/monthly/by-classCode")
    public ResponseEntity<?> findStudentByClassCode(@RequestBody ClassMonthlyByClassCodeDTO dto) {

        List<MonthlyStudentDTO> students = classService.getMonthlyClassDetail(dto.getClassCode());

        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @PostMapping("/api/monthly/update_score")
    public ResponseEntity<?> updateMonthlyScore(@RequestBody ClassMonthlyScoreDTO dto) {

        classService.updateMonthlyScore(dto);
        return ResponseEntity.ok().body(ApiUtils.success("hello"));
    }

}
