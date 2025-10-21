package com.hohoedu.all_pass.class_instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;


import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassMonthlyByClassCodeDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassMonthlyScoreDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassRecordReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.UpdateRemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.UpdateRemedialDateDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.BeforeClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RecordLabelDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.class_instance.model.TimeTableCode;
import com.hohoedu.all_pass.student.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final DateConfig dateConfig;
    private final ClassService classService;
    private final StudentService studentService;

    @GetMapping("/classCodes")
    public ResponseEntity<?> createClass() {
        List<ClassCode> classCode = classService.findClassCode();
        return ResponseEntity.ok(ApiUtils.success(classCode));
    }

    // 시간표 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerClass(@RequestBody List<ClassReqDTO.ClassRegisterDTO> reqDTO, HttpSession session) {
        try {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                        .header(HttpHeaders.LOCATION, "/login")
                        .build();
            }

            for (ClassReqDTO.ClassRegisterDTO req : reqDTO) {
                req.setCenterCode(user.getCenterCode());
                classService.registerClass(req);
            }

            return ResponseEntity.ok(ApiUtils.success("200"));

        } catch (Exception e) {
            System.out.println("============" + e.getMessage() + "============");
            return ResponseEntity.ok(ApiUtils.error("시간표 등록 실패", HttpStatus.INTERNAL_SERVER_ERROR));

        }
    }

    // 학생 수업 등록
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

            return ResponseEntity
                    .ok(ApiUtils.error("오류가 발생했습니다.", HttpStatus.OK));
        }
    }

    @PostMapping("/delete/student")
    public ResponseEntity<?> timeTableDeleteStudent(@RequestBody Map<String, String> request) {
        String timeTableKey = request.get("timeTableKey");
        String studentId = request.get("studentId");
        System.out.println(timeTableKey);
        System.out.println(studentId);
        classService.deleteStudent(timeTableKey, studentId);
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @PostMapping("/api/load_time_table")
    public ResponseEntity<?> loadTimeTable(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        List<TimeTableDTO> tables = classService.getLastTimeTable(user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(tables));
    }

    @GetMapping("/api/timetable/{userNo}")
    public ResponseEntity<?> findTimeTableCodeByUserNo(@PathVariable("userNo") Integer userNo) {
        List<TimeTableCode> codes = classService.findTimeTableCodeByUserNo(userNo);
        return ResponseEntity.ok(ApiUtils.success(codes));
    }

    @PostMapping("/api/delete/timetable/row")
    public ResponseEntity<?> deleteRow(@RequestBody ClassReqDTO.DeleteTimeTableDTO dto) {
        classService.deleteTimeTableRow(dto.getTimeTableKey());
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @PostMapping("/api/delete/timetable/all")
    public ResponseEntity<?> deleteAll(@RequestBody ClassReqDTO.DeleteTimeTableDTO dto) {
        // classService.deleteTimeTableRow(dto.getTimeTableKey());
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    // ================ 수업 일지 컨트롤러 =====================//
    @PostMapping("/api/record/label")
    public ResponseEntity<?> findRecordLabel(@RequestBody ClassRecordReqDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<RecordLabelDTO> labels = classService.getTimeTableByUserCode(dto.getYy(), dto.getMm(), dto.getDay(), dto.getUserCode(), user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(labels));
    }

    @PostMapping("/api/record/student")
    public ResponseEntity<?> findRecordByClass(@RequestBody ClassRecordReqDTO dto) {
        ClassRespDTO.RecordBundleDTO response = classService.getTimeTableByKey(dto.getTimeTableKey(), dto.getWeek(), dto.getClassKey(), dto.getUnitKey());

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/api/record/before-class")
    public ResponseEntity<?> findRecordBeforeClass(@RequestBody ClassReqDTO.BeforeClassDTO dto) {
        BeforeClassRespDTO response = classService.getBeforeClassContent(
                dto.getClassKey(),
                dto.getUnitKey(),
                dto.getWeek(),
                dto.getTimeTableKey());
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    // 알림 발송 이후 내용 저장
    @PostMapping("/api/before-notice/insert")
    public ResponseEntity<?> insertBeforeClassNotice(@RequestBody List<ClassReqDTO.BeforeClassNoticeDTO> dtoList, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        classService.insertBeforeClassNoticeList(dtoList, user.getUserCode());

        return ResponseEntity.ok("ok");
    }

    //알림발송 이후 출결 업데이트
    @PostMapping("/api/attendance/insert")
    public ResponseEntity<?> updateStudentAttendance(@RequestBody List<ClassReqDTO.updateAttendanceDTO> dtos) {
        for (ClassReqDTO.updateAttendanceDTO dto : dtos) {
            classService.updateAttendance(
                    dto.getStudentId(),
                    dto.getTimeTableKey(),
                    dto.getAttendanceDate(),
                    dto.getWeek()
            );
        }
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @PostMapping("/api/after-notice/insert")
    public ResponseEntity<?> insertAfterClassNotice(@RequestBody List<ClassReqDTO.AfterClassNoticeDTO> dtoList, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        classService.insertAfterClassNoticeList(dtoList, user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(true));
    }

    // 수업 후 알림 발송 저장
    @PostMapping("/api/afterSend/update")
    public ResponseEntity<?> updateAfterSend(@RequestBody List<ClassReqDTO.updateAttendanceDTO> dtos) {
        for (ClassReqDTO.updateAttendanceDTO dto : dtos) {
            classService.updateAfterSend(
                    dto.getStudentId(),
                    dto.getTimeTableKey(),
                    dto.getWeek()
            );
        }
        return ResponseEntity.ok(ApiUtils.success(true));
    }


    // ================ 보강 관리 컨트롤러 =====================//
    @PostMapping("/remedial/update")
    public ResponseEntity<?> updateRemedial(@RequestBody UpdateRemedialDTO dto,
                                            @RequestParam(value = "year") String year,
                                            @RequestParam(value = "month") String month) {
        int response = classService.updateRemedialAction(dto);
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


    // 월간 평가 (초등)
    // 월별 / 선생님 별 테이블 라벨 가져오기
    @PostMapping("/api/monthly/classes")
    public ResponseEntity<?> findTimeTableLabel(@RequestBody ClassReqDTO.ClassMonthlyDTO dto) {

        List<TimeTableLabelDTO> label = classService.getMonthlyClassList(dto.getUserCode(), dto.getYy(), dto.getMm(), dto.getDayname());

        return ResponseEntity.ok(ApiUtils.success(label));
    }

    // 클래스 코드로 데이터 가져오기
    @PostMapping("/api/monthly/timeTableKey")
    public ResponseEntity<?> findStudentByClassCode(@RequestBody ClassMonthlyByClassCodeDTO dto) {

        List<MonthlyStudentDTO> students = classService.getMonthlyClassDetail(dto.getTimeTableKey());

        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @PostMapping("/api/monthly/update_score")
    public ResponseEntity<?> updateMonthlyScore(@RequestBody ClassMonthlyScoreDTO dto) {

        classService.updateMonthlyScore(dto);
        return ResponseEntity.ok().body(ApiUtils.success("hello"));
    }

}
