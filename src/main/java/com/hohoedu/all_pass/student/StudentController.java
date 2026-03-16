package com.hohoedu.all_pass.student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.google.api.Http;
import com.google.protobuf.Api;
import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass._core.utils.FileUploadService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.student._dto.app.StudentAppReqDTO;
import com.hohoedu.all_pass.student._dto.app.StudentAppRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO.StudentJoinDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.GradeCode;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    final private StudentService studentService;
    final private ClassService classService;
    final private FileUploadService fileUploadService;


    @GetMapping("/api/label")
    public ResponseEntity<?> getLabels(@RequestParam("userCode") String userCode) {

        System.out.println("userCode = " + userCode);
        List<TimeTableLabelDTO> labels = classService.getClassLabel(userCode);
        return ResponseEntity.ok(ApiUtils.success(labels));
    }

    @GetMapping(value = "/api/students", params = "userCode")
    public ResponseEntity<?> getStudentsByUSerCode(@RequestParam("userCode") String userCode, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<StudentWebRespDTO.MainStudentDTO> students = studentService.getStudentsByUserCode(userCode, user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @GetMapping(value = "/api/students", params = {"timeTableKey", "userCode"})
    public ResponseEntity<?> getStudentsByClassCode(@RequestParam("timeTableKey") String timeTableKey, @RequestParam("userCode") String userCode, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<MainStudentDTO> students = studentService.getStudentsByKey(timeTableKey, userCode, user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(students));
    }


    // 학생 개별 데이터 불러오기
    @GetMapping("/{studentId}")
    public ResponseEntity<?> findStudentByStudentNo(@PathVariable("studentId") String studentId) {

        StudentWebRespDTO.StudentDTO student = studentService.getStudentDetailByStudentId(studentId);

        return ResponseEntity.ok(ApiUtils.success(student));
    }


    // 학생 등록
    @PostMapping("/join")
    public ResponseEntity<?> studentJoin(@ModelAttribute StudentJoinDTO studentDTO, @ModelAttribute StudentWebReqDTO.ParentJoinDTO parentDTO, HttpSession session) {

        if (studentDTO.getInviteCode() == null || studentDTO.getInviteCode().isEmpty()) {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user"); // 세션 키 이름은 맞게 수정
            studentDTO.setUserCode(user.getUserCode());
        }

        String studentId = studentService.studentInsert(studentDTO, parentDTO);

        return ResponseEntity.ok(ApiUtils.success(Map.of("studentId", studentId)));
    }


    // 학생 상태 변경
    @PostMapping("/status")
    public ResponseEntity<?> statusUpdate(@RequestBody StatusHistoryDTO historyDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        StudentWebRespDTO.StudentStatusDTO response = studentService.statusInsert(historyDTO, user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(response));
    }

    // 학생 정보 수정
    @PostMapping("/update/info")
    public ResponseEntity<String> updateStudentInfo(@RequestBody StudentWebReqDTO.StudentUpdateDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        reqDTO.setUserCode(user.getUserCode());

        studentService.updateStudentInfo(reqDTO);

        return ResponseEntity.ok("ok");
    }

    @PostMapping("/update/payment")
    public ResponseEntity<?> updatePayment(@RequestBody StudentWebReqDTO.StudentPaymentUpdateDTO req) {
        log.info("req = {}", req);
        studentService.updatePaymentInfo(req);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @PostMapping("/update/course-status")
    public ResponseEntity<?> updateCourse(@RequestBody StudentWebReqDTO.StudentCourseUpdateDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        try {
            log.info(req.getEntryBookDate());
            studentService.updateCourseStatus(req, user.getUserCode());

            return ResponseEntity.ok(ApiUtils.success("수강상태가 성공적으로 변경되었습니다."));
        } catch (Exception e) {

            log.error("수강상태 수정 실패", e);

            return ResponseEntity.status(500).body("수강상태 변경 중 오류가 발생했습니다.");
        }

    }

    @GetMapping("/gradeCodes")
    public ResponseEntity<?> getGradeCode() {
        List<GradeCode> gradeCodes = studentService.findGrade();
        return ResponseEntity.ok(ApiUtils.success(gradeCodes));
    }

    @PostMapping("/api/transfer/list")
    public ResponseEntity<?> getTransferStudentList(@RequestBody StudentWebReqDTO.TransferStudentListReqDTO reqDTO, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        log.info("reqDTO = {}", reqDTO);


        List<StudentWebRespDTO.StudentInOutDTO> studentList = studentService.findAllInOut(user.getCenterCode(), reqDTO.getUserCode());

        return ResponseEntity.ok(ApiUtils.success(studentList));
    }

    @GetMapping("/transfer/{studentId}")
    public ResponseEntity<?> getTransferByStudentId(@PathVariable("studentId") Integer studentId) {

        List<StudentTransferDTO> transferDTO = studentService.findInOutByStudentId(studentId);
        return ResponseEntity.ok(ApiUtils.success(transferDTO));
    }

    @PostMapping("/inout")
    @ResponseBody
    public ResponseEntity<?> studentInOut(@RequestBody StudentWebReqDTO.StudentTransferDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        studentService.reserveTransfer(dto, user.getUserCode());

        return ResponseEntity.ok(ApiUtils.success("RESERVED"));
    }

    @PostMapping("/app_token")
    public ResponseEntity<?> getStudentAppToken(@RequestBody StudentAppReqDTO.AttendanceTokenDTO attendanceTokenDTO) {
        StudentAppRespDTO.AppTokenRespDTO respDTO = studentService.findAppTokenByAppId(attendanceTokenDTO.getAppId());
        return ResponseEntity.ok(ApiUtils.success(respDTO));
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> studentAttendance(@RequestBody StudentAppReqDTO.StudentAttendanceDTO dto) {

        Student studentInfo = studentService.findByStudentId(dto.getStudentId());
        log.info("studentInfo = {}", studentInfo);

        if ("1".equals(dto.getAttendType())) {   // 등원
            String checkedIn = studentService.checkinStudent(dto, studentInfo);
            return ResponseEntity.ok(ApiUtils.success(checkedIn));
        } else {   // 하원
            String checkedOut = studentService.checkoutStudent(dto, studentInfo);
            return ResponseEntity.ok(ApiUtils.success(checkedOut));

        }
    }

    @GetMapping("/overview/data")
    public List<StudentSnapshotRespDTO> getSnapshotData(
            @RequestParam(value = "period", required = false) String period,
            @RequestParam(value = "startYm", required = false) String startYm,
            @RequestParam(value = "endYm", required = false) String endYm,
            @RequestParam(value = "userNo", required = false) Integer userNo) {

        if (startYm != null && endYm != null) {
            return studentService.getSnapshot(startYm, endYm, userNo);
        }
        String nowYm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

        switch (period) {
            case "6m":
                startYm = LocalDate.now().minusMonths(5).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                break;
            case "3m":
                startYm = LocalDate.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                break;
            case "1y":
            default:
                startYm = LocalDate.now().minusYears(1).plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                break;
        }


        return studentService.getSnapshot(startYm, nowYm, userNo);
    }


    @PostMapping("/upload/signature")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String uploadedPath = fileUploadService.uploadSignature(file);
            return ResponseEntity.ok(ApiUtils.success(uploadedPath));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("UPLOAD_FAIL");
        }
    }

    @PostMapping("/update/attendance")
    public ResponseEntity<?> updateStudentAttendance(HttpSession session, @RequestBody StudentWebReqDTO.StudentAttendanceUpdateDTO requestDTO) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        try {
            studentService.updateAttendance(requestDTO, user.getUserCode());
            return ResponseEntity.ok(Map.of("success", true, "message", "출석 정보가 업데이트되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "출석 정보 업데이트 실패: " + e.getMessage()));
        }
    }

    @PostMapping("/api/withdraw/counts")
    public ResponseEntity<?> getWithdrawCounts(@RequestBody StudentWebReqDTO.WithdrawReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("response", studentService.findWithdrawCounts(req)));
    }

    @PostMapping("/api/withdraw/join")
    public ResponseEntity<?> getJoinList(@RequestBody StudentWebReqDTO.WithdrawReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("response", studentService.findJoinList(req)));
    }

    @PostMapping("/api/withdraw/withdraw")
    public ResponseEntity<?> getWithdrawList(@RequestBody StudentWebReqDTO.WithdrawReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("response", studentService.findWithdrawList(req)));
    }

    @PostMapping("/api/withdraw/transfer-in")
    public ResponseEntity<?> getTransferInList(@RequestBody StudentWebReqDTO.WithdrawReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("response", studentService.findTransferInList(req)));
    }

    @PostMapping("/api/withdraw/transfer-out")
    public ResponseEntity<?> getTransferOutList(@RequestBody StudentWebReqDTO.WithdrawReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("response", studentService.findTransferOutList(req)));
    }

    @PostMapping("/api/withdraw/graduate")
    public ResponseEntity<?> getGraduateList(@RequestBody StudentWebReqDTO.WithdrawReqDTO req, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        return ResponseEntity.ok(Map.of("response", studentService.findGraduateList(req)));
    }
//================================================================================================================================================================================================================================================================//

    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam String studentName) {
        try {
            List<StudentWebRespDTO.SearchRespDTO> result = studentService.searchByName(studentName);
            return ResponseEntity.ok(ApiUtils.success(result));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            log.error("[AppId] 학생 검색 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/app_id/update")
    public ResponseEntity<?> save(@RequestBody StudentWebReqDTO.UpdateAppIdLogDTO req, HttpSession session) {
        if (session.getAttribute("user") != null) {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            log.info("user = {}", user.toString());
            req.setUserCode(user.getUserName());
        }
        try {
            studentService.updateAppIdLog(req);
            return ResponseEntity.ok(Map.of("success", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            log.error("[AppId] App ID 저장 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "서버 오류가 발생했습니다."));
        }
    }

    @PostMapping("/pending/cancel")
    public ResponseEntity<Map<String, Object>> cancelPending(@RequestBody Map<String, Object> body, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        Map<String, Object> result = new HashMap<>();
        try {
            Integer id = Integer.valueOf(body.get("id").toString());
            studentService.cancelPending(id, user.getUserCode());
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

}
