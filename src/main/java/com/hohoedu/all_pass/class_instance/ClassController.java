package com.hohoedu.all_pass.class_instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.protobuf.Api;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;


import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.ClassWeek;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @GetMapping("/classCodes")
    public ResponseEntity<?> createClass() {
        List<ClassCode> classCode = classService.findClassCode();
        return ResponseEntity.ok(ApiUtils.success(classCode));
    }

    @PostMapping("/week/save")
    public ResponseEntity<?> saveWeek(@RequestBody ClassReqDTO.WeekReqDTO reqDTO, HttpSession session) {
        try {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/login")
                        .build();
            }

            classService.saveClassWeek(reqDTO, user.getCenterCode());

            return ResponseEntity.ok(ApiUtils.success("success"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/week/get")
    public ResponseEntity<?> getWeekData(@RequestBody ClassReqDTO.GetWeekDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user =
                (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        List<ClassRespDTO.ClassWeekDTO> list = classService.getClassWeek(reqDTO.getYear(), reqDTO.getMonth(), user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(list));
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
            String msg = "";
            for (ClassReqDTO.ClassRegisterDTO req : reqDTO) {
                if ("COM".equals(req.getClassKey())) {
                    req.setUnitKey(null);
                    req.setGradeKey(null);
                }
                req.setCenterCode(user.getCenterCode());
                msg = classService.registerClass(req);
            }
            System.out.println(msg);

            return ResponseEntity.ok(ApiUtils.success("200"));

        } catch (Exception e) {
            System.out.println("============" + e.getMessage() + "============");
            return ResponseEntity.ok(ApiUtils.error("시간표 등록 실패", HttpStatus.INTERNAL_SERVER_ERROR));

        }
    }

    // 학생 수업 등록
    @PostMapping("/add_student")
    @ResponseBody
    public ResponseEntity<?> timeTableAssginStudent(HttpSession session, @RequestBody ClassReqDTO.AddStudentList reqDTO) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {
            for (ClassReqDTO.AddStudentDTO dto : reqDTO.getAssignments()) {
                classService.registerStudentFullProcess(dto, user.getUserCode(), user.getCenterCode());
            }
            return ResponseEntity.ok(ApiUtils.success(true));

        } catch (Exception e) {
            return ResponseEntity.ok(ApiUtils.error("오류가 발생했습니다.", HttpStatus.OK));
        }
    }

    @PostMapping("/comclass/students")
    public ResponseEntity<?> getComClassStudent(@RequestBody Map<String, String> request, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        List<ClassRespDTO.ComClassStudentDTO> response = classService.findComClassStudentsByTimeTableKey(request.get("timeTableKey"), user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(response));
    }

//    @PostMapping("/comclass/updateAssign")
//    public ResponseEntity<?> updateTimeTableAssign(@RequestBody ClassReqDTO.AssignUpdateDTO reqDTO, HttpSession session) {
//        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
//        if (user == null) {
//            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
//                    .header(HttpHeaders.LOCATION, "/login")
//                    .build();
//        }
//
//        int response = classService.updateTimeTableAssign(reqDTO, user.getUserCode(), user.getCenterCode());
//
//        return ResponseEntity.ok(ApiUtils.success(response + "건"));
//    }


    @PostMapping("/delete/student")
    public ResponseEntity<?> timeTableDeleteStudent(@RequestBody Map<String, String> request) {
        String timeTableKey = request.get("timeTableKey");
        String studentId = request.get("studentId");
        classService.deleteStudent(timeTableKey, studentId);
        return ResponseEntity.ok(ApiUtils.success("삭제되었습니다. 교재 주문 수량을 수정해주세요."));
    }

    @PostMapping("/api/load_time_table")
    public ResponseEntity<?> loadTimeTable(HttpSession session, @RequestBody Map<String, String> request) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<TimeTableDTO> tables = classService.getLastTimeTable(user.getUserCode(), request);

        return ResponseEntity.ok(ApiUtils.success(tables));
    }


    @PostMapping("/api/copy/last-timetable")
    public ResponseEntity<?> copyLastTimetable(@RequestBody Map<String, String> req, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        classService.copyLastMonthTimeTableAndStudents(
                user.getUserCode(),
                user.getCenterCode(),
                req.get("year"),
                req.get("month")
        );
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @PostMapping("/timetable/view")
    public ResponseEntity<?> viewTimeTable(@RequestBody ClassReqDTO.TimeTaleViewReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        String userCode = reqDTO.getUserCode() == null ? user.getUserCode() : reqDTO.getUserCode();
        ClassRespDTO.TimeTableViewRespDTO viewData = classService.findTableViewWithStudents(reqDTO.getYear(), reqDTO.getMonth(), userCode);

        return ResponseEntity.ok(ApiUtils.success(viewData));
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

    // 전체 이력 조회 (월 파라미터 없이 전체 반환)
//    @GetMapping("/api/delete/log")
//    public ResponseEntity<?> deleteLog() {
//        List<DeleteBackupDTO> list = backupRepository.findAllBackupList();
//        return ResponseEntity.ok(ApiUtils.success(list));
//    }
//
//    // 복구
//    @PostMapping("/api/restore")
//    public ResponseEntity<?> restore(@RequestBody Map<String, String> req) {
//        classService.restoreBackup(req.get("backupKey"));
//        return ResponseEntity.ok(ApiUtils.success(true));
//    }


    // ================ 수업 일지 컨트롤러 =====================//
    @PostMapping("/api/record/label")
    public ResponseEntity<?> findRecordLabel(@RequestBody ClassRecordReqDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        // ✅ date 기준으로 서비스에서 월 판단
        List<RecordLabelDTO> labels = classService.getTimeTableByUserCode(
                dto.getDate(), dto.getDay(), dto.getUserCode(), user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(labels));
    }

    @PostMapping("/api/record/student")
    public ResponseEntity<?> findRecordByClass(@RequestBody ClassRecordReqDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        log.info(dto.getDate());

        // date를 사용하여 백엔드에서 week 계산
        ClassRespDTO.RecordBundleDTO response = classService.getTimeTableByKey(
                dto.getUserCode(),
                dto.getTimeTableKey(),
                dto.getDate(),  // week 대신 date 사용
                dto.getClassKey(),
                dto.getUnitKey(),
                user.getCenterCode()  // centerCode 추가
        );
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

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        classService.insertBeforeClassNoticeList(dtoList, user.getUserCode());

        return ResponseEntity.ok("ok");
    }

    //알림발송 이후 출결 업데이트
    @PostMapping("/api/attendance/insert")
    public ResponseEntity<?> updateStudentAttendance(@RequestBody List<ClassReqDTO.updateAttendanceDTO> dtos) {
        log.info("이거지?");
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
        log.info(dtoList.get(0).getWord());
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        log.info(dtoList.get(0).getYear());
        log.info(dtoList.get(0).getMonth());
        log.info(dtoList.get(0).getWeek());
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
    @PostMapping("/remedial/list")
    public ResponseEntity<?> getRemedialList(HttpSession session, @RequestBody ClassReqDTO.GetRemedialDTO dto) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        String userCode = (dto.getUserCode() != null && !dto.getUserCode().isBlank())
                ? dto.getUserCode()
                : user.getUserCode();

        List<RemedialDTO> response = classService.findRemedialByUserNo(dto.getYear(), dto.getMonth(), userCode);

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/remedial/update")
    public ResponseEntity<?> updateRemedial(@RequestBody UpdateRemedialDTO dto,
                                            @RequestParam(value = "year") String year,
                                            @RequestParam(value = "month") String month,
                                            HttpSession session) {


        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        int response = classService.updateRemedialAction(dto);
        List<RemedialDTO> remedials = classService.findRemedialByUserNo(year, month, user.getUserCode());
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

    @PostMapping("/remedial/updateTime")
    public ResponseEntity<?> updateRemedialTime(@RequestBody ClassReqDTO.UpdateRemedialTimeDTO dto) {
        classService.updateRemedialTime(dto);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/remedial/delete")
    public ResponseEntity<?> deleteRemedial(@RequestBody ClassReqDTO.DeleteRemedialTimeDTO dto) {
        classService.deleteRemedial(dto);
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    // 월간 평가 (초등)
    // 월별 / 선생님 별 테이블 라벨 가져오기
    @PostMapping("/api/monthly/classes")
    public ResponseEntity<?> findTimeTableLabel(@RequestBody ClassReqDTO.ClassMonthlyDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<TimeTableLabelDTO> label = classService.getMonthlyClassList(dto.getUserCode(), dto.getYy(), dto.getMm(), dto.getDayname(), user.getCenterCode());

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

        ClassRespDTO.ScoreResultDTO response = classService.updateMonthlyScore(dto);

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/api/monthly/preview")
    public ResponseEntity<?> getMonthlyPreview(@RequestBody ClassReqDTO.MonthlyPreviewDTO dto) {

        ClassRespDTO.MonthlyPreviewRespDTO response = classService.getMonthlyPreview(dto);

        return ResponseEntity.ok(ApiUtils.success(response));
    }


    @PostMapping("/api/monthly/save")
    public ResponseEntity<?> saveMonthlyReport(@RequestBody ClassReqDTO.MonthlySaveRequestDTO dto) {
        try {
            List<ClassReqDTO.MonthlySaveRequestDTO.MonthlySaveDTO> students = dto.getStudents();

            students.forEach(s -> log.info("학생: {}", s));

            classService.saveMonthlyComments(students);

            return ResponseEntity.ok(ApiUtils.success(Map.of(
                    "message", students.size() + "명의 코멘트가 저장되었습니다."
            )));

        } catch (Exception e) {
            log.error("월간평가 코멘트 저장 오류", e);
            return ResponseEntity.badRequest().body(ApiUtils.error(
                    "저장 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST
            ));
        }
    }

    // 월간 평가 (유아)
    @PostMapping("/infant/labels")
    public ResponseEntity<?> getInfantClassLabel(@RequestBody ClassReqDTO.InfantClassLabelsDTO dto) {

        List<TimeTableLabelDTO> response = classService.findInfantClassLabel(dto);

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/infant/details")
    public ResponseEntity<?> getInfantClassDetail(@RequestBody ClassReqDTO.InfantDetailDTO dto) {
        TimeTableLabelDTO labelDTO = new TimeTableLabelDTO();
        labelDTO.setClassKey(dto.getClassKey());
        labelDTO.setUnitKey(dto.getUnitKey());
        labelDTO.setTimeTableKey(dto.getTimeTableKey());
        labelDTO.setYy(dto.getYy());

        Map<String, Object> response = new HashMap<>();

        Set<String> hanKeys = Set.of("Y", "P", "S");
        Set<String> bookKeys = Set.of("K", "M", "J");

        try {
            if (hanKeys.contains(dto.getClassKey())) {

                ClassRespDTO.InfantHanDTO hanDTO = classService.findInfantHan(labelDTO);
                hanDTO.setClassLabel(dto.getClassSubject());
                response.put("type", "HAN");
                response.put("data", hanDTO);

                return ResponseEntity.ok(ApiUtils.success(response));
            }

            if (bookKeys.contains(dto.getClassKey())) {

                ClassRespDTO.InfantBookDTO bookDTO = classService.findInfantBook(labelDTO);
                response.put("type", "BOOK");
                response.put("data", bookDTO);
                bookDTO.setClassLabel(dto.getClassSubject());
                return ResponseEntity.ok(ApiUtils.success(response));
            }

        } catch (Exception e) {
            response.put("type", "ERROR");
            response.put("message", "데이터 조회 중 오류가 발생했습니다.");
            return ResponseEntity.ok(ApiUtils.error("조회 실패", HttpStatus.INTERNAL_SERVER_ERROR));
        }

        // 둘 다 해당되지 않을 때
        response.put("type", "NONE");
        response.put("data", null);

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/infant/save")
    public ResponseEntity<?> saveInfantNotice(@RequestBody ClassReqDTO.InfantSaveReqDTO reqDTO, HttpSession session) {
        try {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiUtils.error("UNAUTHORIZED", HttpStatus.UNAUTHORIZED));
            }

            String centerCode = user.getCenterCode();
            String userCode = user.getUserCode();

            classService.saveInfantNotice(reqDTO, centerCode, userCode);

            return ResponseEntity.ok(ApiUtils.success("success"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(ApiUtils.error("SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/api/remarks/list")
    public ResponseEntity<?> getRemarksList(@RequestBody ClassReqDTO.RemarksRequestDTO dto) {
        List<ClassRespDTO.RemarksCategoryDTO> result = classService.getRemarksList(dto);
        return ResponseEntity.ok(Map.of("response", result));
    }

    @PostMapping("/api/remarks/save")
    public ResponseEntity<?> saveRemarks(@RequestBody ClassReqDTO.RemarksSaveDTO dto) {
        classService.saveRemarks(dto);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
