package com.hohoedu.all_pass.class_instance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;

import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass._core.handler.exception.Exception400;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassMonthlyByClassCodeDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassMonthlyScoreDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.ClassRecordReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.EduTimeTableCheckReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.UpdateRemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO.UpdateRemedialDateDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.BeforeClassRespDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RecordLabelDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableLabelDTO;

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
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        List<ClassRespDTO.ClassWeekDTO> list = classService.getClassWeek(reqDTO.getYear(), reqDTO.getMonth(),
                user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(list));
    }

    // 시간표 등록
    @PostMapping("/register")
    public ResponseEntity<?> registerClass(@RequestBody List<ClassReqDTO.ClassRegisterDTO> reqDTO,
            HttpSession session) {
        try {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null)
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/login")
                        .build();

            String msg = null;
            for (ClassReqDTO.ClassRegisterDTO req : reqDTO) {
                if ("COM".equals(req.getClassKey())) {
                    req.setUnitKey(null);
                    req.setGradeKey(null);
                }
                req.setCenterCode(user.getCenterCode());
                msg = classService.registerClass(req);
            }
            return ResponseEntity.ok(ApiUtils.success(msg));

        } catch (Exception e) {
            System.out.println("============" + e.getMessage() + "============");
            return ResponseEntity.ok(ApiUtils.error("시간표 등록 실패", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // 학생 수업 등록
    @PostMapping("/add_student")
    @ResponseBody
    public ResponseEntity<?> timeTableAssginStudent(HttpSession session,
            @RequestBody ClassReqDTO.AddStudentList reqDTO) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {
            String message = null;
            for (ClassReqDTO.AddStudentDTO dto : reqDTO.getAssignments()) {
                String result = classService.registerStudentFullProcess(dto, user.getUserCode(), user.getCenterCode(), false);
                if (result != null) message = result;
            }
            return ResponseEntity.ok(ApiUtils.success(message));

        } catch (Exception400 e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));
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
        List<ClassRespDTO.ComClassStudentDTO> response = classService
                .findComClassStudentsByTimeTableKey(request.get("timeTableKey"), user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    // @PostMapping("/comclass/updateAssign")
    // public ResponseEntity<?> updateTimeTableAssign(@RequestBody
    // ClassReqDTO.AssignUpdateDTO reqDTO, HttpSession session) {
    // UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
    // session.getAttribute("user");
    // if (user == null) {
    // return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
    // .header(HttpHeaders.LOCATION, "/login")
    // .build();
    // }
    //
    // int response = classService.updateTimeTableAssign(reqDTO, user.getUserCode(),
    // user.getCenterCode());
    //
    // return ResponseEntity.ok(ApiUtils.success(response + "건"));
    // }

    @PostMapping("/delete/student")
    public ResponseEntity<?> timeTableDeleteStudent(@RequestBody Map<String, String> request) {
        String timeTableKey = request.get("timeTableKey");
        String studentId = request.get("studentId");
        String msg = classService.deleteStudent(timeTableKey, studentId);
        return ResponseEntity.ok(ApiUtils.success(msg));
    }

    @PostMapping("/api/load_time_table")
    public ResponseEntity<?> loadTimeTable(HttpSession session, @RequestBody Map<String, String> request) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
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

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        String msg = classService.copyLastMonthTimeTableAndStudents(
                user.getUserCode(),
                user.getCenterCode(),
                req.get("year"),
                req.get("month"));
        return ResponseEntity.ok(ApiUtils.success(msg));
    }

    @PostMapping("/api/copy/to-teacher")
    public ResponseEntity<?> copyTimeTableToTeacher(@RequestBody ClassReqDTO.CopyToTeacherDTO req, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        req.setToUserCode(user.getUserCode());

        try {
            classService.copyTimeTableToTeacher(req, user.getCenterCode());
            return ResponseEntity.ok(ApiUtils.success(null));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @PostMapping("/timetable/view")
    public ResponseEntity<?> viewTimeTable(@RequestBody ClassReqDTO.TimeTaleViewReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        String userCode = reqDTO.getUserCode() == null ? user.getUserCode() : reqDTO.getUserCode();
        ClassRespDTO.TimeTableViewRespDTO viewData = classService.findTableViewWithStudents(reqDTO.getYear(),
                reqDTO.getMonth(), userCode, user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(viewData));
    }

    @PostMapping("/api/delete/timetable/row")
    public ResponseEntity<?> deleteRow(@RequestBody ClassReqDTO.DeleteTimeTableDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();

        String msg = classService.deleteTimeTableRow(dto.getTimeTableKey(), user.getUserCode());
        return ResponseEntity.ok(ApiUtils.success(msg));
    }

    @PostMapping("/api/delete/timetable/all")
    public ResponseEntity<?> deleteAll(@RequestBody ClassReqDTO.DeleteTimeTableDTO dto) {
        // classService.deleteTimeTableRow(dto.getTimeTableKey());
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    // 전체 이력 조회 (월 파라미터 없이 전체 반환)
    // @GetMapping("/api/delete/log")
    // public ResponseEntity<?> deleteLog() {
    // List<DeleteBackupDTO> list = backupRepository.findAllBackupList();
    // return ResponseEntity.ok(ApiUtils.success(list));
    // }
    //
    // // 복구
    // @PostMapping("/api/restore")
    // public ResponseEntity<?> restore(@RequestBody Map<String, String> req) {
    // classService.restoreBackup(req.get("backupKey"));
    // return ResponseEntity.ok(ApiUtils.success(true));
    // }

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
                dto.getDate(), // week 대신 date 사용
                dto.getClassKey(),
                dto.getUnitKey(),
                user.getCenterCode() // centerCode 추가
        );
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/api/record/before-class")
    public ResponseEntity<?> findRecordBeforeClass(@RequestBody ClassReqDTO.BeforeClassDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        BeforeClassRespDTO response = classService.getBeforeClassContent(
                dto.getClassKey(),
                dto.getUnitKey(),
                dto.getWeek(),
                dto.getTimeTableKey(),
                user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(response));
    }

    @PostMapping("/api/before/all/count")
    public ResponseEntity<?> countAllBeforeClass(
            @RequestBody ClassReqDTO.BeforeAllCountReqDTO dto, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        ClassRespDTO.BeforeAllCountRespDTO result = classService.countAllBeforeClassStudents(
                dto.getDate(), dto.getUserCode(), user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/api/before/all")
    public ResponseEntity<?> sendAllBeforeClass(
            @RequestBody ClassReqDTO.BeforeAllSendReqDTO dto, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        ClassRespDTO.BeforeAllSendRespDTO result = classService.sendAllBeforeClassNotice(
                dto.getDate(), dto.getUserCode(), user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    // 알림 발송 이후 내용 저장
    @PostMapping("/api/before-notice/insert")
    public ResponseEntity<?> insertBeforeClassNotice(@RequestBody List<ClassReqDTO.BeforeClassNoticeDTO> dtoList,
            HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        classService.insertBeforeClassNoticeList(dtoList, user.getUserCode());

        return ResponseEntity.ok("ok");
    }

    // 알림발송 이후 출결 업데이트
    @PostMapping("/api/attendance/insert")
    public ResponseEntity<?> updateStudentAttendance(@RequestBody List<ClassReqDTO.updateAttendanceDTO> dtos) {

        for (ClassReqDTO.updateAttendanceDTO dto : dtos) {
            classService.updateAttendance(
                    dto.getStudentId(),
                    dto.getTimeTableKey(),
                    dto.getAttendanceDate(),
                    dto.getWeek());
        }
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @PostMapping("/api/after-notice/insert")
    public ResponseEntity<?> insertAfterClassNotice(@RequestBody List<ClassReqDTO.AfterClassNoticeDTO> dtoList,
            HttpSession session) {
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
                    dto.getWeek());
        }
        return ResponseEntity.ok(ApiUtils.success(true));
    }

    @PostMapping("/api/record/unsent-check")
    public ResponseEntity<?> checkUnsentAfterNotification(
            @RequestBody Map<String, String> req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        String userCode = (req.get("userCode") != null && !req.get("userCode").isBlank())
                ? req.get("userCode")
                : user.getUserCode();
        List<String> result = classService.findUnsentAfterNotification(userCode, user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(result));
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

        List<RemedialDTO> remedials = classService.findRemedialByUserNo(dto.getYear(), dto.getMonth(), userCode);
        List<ClassRespDTO.AbsentFlagDTO> absentFlags = classService.findAbsentFlags(dto.getYear(), dto.getMonth(),
                userCode);

        Map<String, Object> result = new HashMap<>();
        result.put("remedials", remedials);
        result.put("absentFlags", absentFlags);

        return ResponseEntity.ok(ApiUtils.success(result));
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
        classService.updateRemedialAction(dto);
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

        List<TimeTableLabelDTO> label = classService.getMonthlyClassList(dto.getUserCode(), dto.getYy(), dto.getMm(),
                dto.getDayname(), user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(label));
    }

    // 클래스 코드로 데이터 가져오기
    @PostMapping("/api/monthly/timeTableKey")
    public ResponseEntity<?> findStudentByClassCode(@RequestBody ClassMonthlyByClassCodeDTO dto) {

        List<MonthlyStudentDTO> students = classService.getMonthlyClassDetail(dto.getTimeTableKey());

        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @PostMapping("/api/monthly/update_score")
    public ResponseEntity<?> updateMonthlyScore(@RequestBody ClassMonthlyScoreDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        ClassRespDTO.ScoreResultDTO response = classService.updateMonthlyScore(dto, user.getCenterCode());

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
                    "message", students.size() + "명의 코멘트가 저장되었습니다.")));

        } catch (Exception e) {
            log.error("월간평가 코멘트 저장 오류", e);
            return ResponseEntity.badRequest().body(ApiUtils.error(
                    "저장 중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST));
        }
    }

    // 월간 학습내용 (유아)
    @PostMapping("/infant/labels")
    public ResponseEntity<?> getInfantClassLabel(@RequestBody ClassReqDTO.InfantClassLabelsDTO dto) {
        log.info(dto.getUserCode());
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
            log.info("reqDTO = {}", reqDTO.toString());

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

    @PostMapping("/edu-check")
    public ResponseEntity<?> checkEduData(@RequestBody EduTimeTableCheckReqDTO reqDTO, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        List<String> existingUserCodes = classService.findExistingEduUserCodes(
                List.of(reqDTO.getUserCode()), reqDTO.getYy(), reqDTO.getMm(), user.getCenterCode());

        Map<String, Object> result = new HashMap<>();
        result.put("existingUserCodes", existingUserCodes);
        return ResponseEntity.ok(ApiUtils.success(result));
    }

    @PostMapping("/edu-generate")
    public ResponseEntity<?> generateEdu(@RequestBody EduTimeTableCheckReqDTO reqDTO, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        classService.executeEduGenerateProcedure(
                reqDTO.getYy(), reqDTO.getMm(), user.getCenterCode(), reqDTO.getUserCode());

        List<ClassRespDTO.TimeTableDTO> tables = classService.findEduTableViewWithStudents(
                reqDTO.getYy(), reqDTO.getMm(), reqDTO.getUserCode(), user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(tables));
    }

    @PostMapping("/edu-timeview/data")
    public ResponseEntity<?> getEduData(@RequestBody EduTimeTableCheckReqDTO reqDTO, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<ClassRespDTO.TimeTableDTO> tables = classService.findEduTableViewWithStudents(
                reqDTO.getYy(), reqDTO.getMm(), reqDTO.getUserCode(), user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(tables));
    }

}
