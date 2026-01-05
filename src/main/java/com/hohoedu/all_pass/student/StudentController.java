package com.hohoedu.all_pass.student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


import com.google.protobuf.Api;
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

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
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
    public ResponseEntity<?> studentJoin(@ModelAttribute StudentJoinDTO studentDTO, @ModelAttribute StudentWebReqDTO.ParentJoinDTO parentDTO) {
        log.info("parentDTO = {}", parentDTO);
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

    @GetMapping("/gradeCodes")
    public ResponseEntity<?> getGradeCode() {
        List<GradeCode> gradeCodes = studentService.findGrade();
        return ResponseEntity.ok(ApiUtils.success(gradeCodes));
    }

    @GetMapping("/transfer/{studentId}")
    public ResponseEntity<?> getTransferByStudentId(@PathVariable("studentId") Integer studentId) {

        List<StudentTransferDTO> transferDTO = studentService.findInOutByStudentId(studentId);
        return ResponseEntity.ok(ApiUtils.success(transferDTO));
    }

    @PostMapping("/inout")
    @ResponseBody
    public ResponseEntity<?> studentInOut(@RequestBody StudentWebReqDTO.StudentTransferDTO studentInOutDTO) {
        // 1. 권한 확인
        // 2. 유효성 검사
        // 3-1. studentId를 이용해서 시간표에서 제외
        // 3-2. erp_student_class의 선생님 코드 업데이트
        // 3-3. erp_student_transfer_history 인서트
        studentService.transferStudent(studentInOutDTO);
//        studentService.insertTransferHistory(studentInOutDTO);

        return ResponseEntity.ok(ApiUtils.success("okay"));
    }

    @PostMapping("/app_token")
    public ResponseEntity<?> getStudentAppToken(@RequestBody StudentAppReqDTO.AttendanceTokenDTO attendanceTokenDTO) {
        log.info("attendanceTokenDTO = {}", attendanceTokenDTO);
        StudentAppRespDTO.AppTokenRespDTO respDTO = studentService.findAppTokenByAppId(attendanceTokenDTO.getAppId());
        return ResponseEntity.ok(ApiUtils.success(respDTO));
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> studentAttendance(@RequestBody StudentAppReqDTO.StudentAttendanceDTO dto) {

        Student studentInfo = studentService.findByStudentId(dto.getStudentId());
        if ("1".equals(dto.getAttendType())) {   // 등원
            boolean checkedIn = studentService.checkinStudent(dto, studentInfo);
            System.out.println("checkedIn = " + checkedIn);
            if (checkedIn) {
                return ResponseEntity.ok(ApiUtils.success("0000"));
            } else {
                return ResponseEntity.ok(ApiUtils.success("7777"));
            }
        } else {   // 하원
            int checkedOut = studentService.checkoutStudent(dto, studentInfo);

            return ResponseEntity.ok(ApiUtils.success("0000"));

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


}
