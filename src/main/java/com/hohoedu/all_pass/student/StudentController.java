package com.hohoedu.all_pass.student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;
import com.hohoedu.all_pass.student._dto.StudentReqDTO.StatusHistoryDTO;
import com.hohoedu.all_pass.student._dto.StudentReqDTO.StudentJoinDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.model.GradeCode;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    final private StudentService studentService;
    final private ClassService classService;

    @GetMapping("/api/label")
    public ResponseEntity<?> getLabels(@RequestParam("teacherNo") String teacherNo) {

        System.out.println("teacherNo = " + teacherNo);
        List<TimeTableLabelDTO> labels = classService.getClassLabel(teacherNo);
        return ResponseEntity.ok(ApiUtils.success(labels));
    }

    @GetMapping(value = "/api/students", params = "teacherNo")
    public ResponseEntity<?> getStudentsByUSerCode(@RequestParam ("teacherNo") String teacherNo, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<StudentRespDTO.MainStudentDTO> students = studentService.getStudentsByUserCode(teacherNo, user.getCenterCode());
        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @GetMapping(value = "/api/students", params = "timeTableKey")
    public ResponseEntity<?> getStudentsByClassCode(@RequestParam ("timeTableKey")String timeTableKey, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        List<MainStudentDTO> students = studentService.getStudentsByKey(timeTableKey, user.getUserCode());
        return ResponseEntity.ok(ApiUtils.success(students));
    }

//    @GetMapping("/{studentId}")
//    public ResponseEntity<?> findStudentByStudentNo(@PathVariable("studentId") String studentId) {
//
//        System.out.println("컨트롤러의 studentId = " + studentId);
//
//        StudentRespDTO.StudentDTO student = studentService.findStudentByStudentId(studentId);
//
//        return ResponseEntity.ok(ApiUtils.success(student));
//    }

    // TODO: 리다이렉트 변경 필요
    @PostMapping("/join")
    public String studentJoin(@ModelAttribute StudentJoinDTO studentDTO,
                              @ModelAttribute StudentReqDTO.ParentJoinDTO parentDTO) {

        studentService.studentInsert(studentDTO, parentDTO);

        return "redirect:/main";
    }

    @PostMapping("/status")
    public ResponseEntity<?> statusUpdate(@RequestBody StatusHistoryDTO historyDTO) {
        studentService.statusInsert(historyDTO);
        return ResponseEntity.ok(ApiUtils.success("변경 완료"));
    }

    @GetMapping("/gradeCodes")
    public ResponseEntity<?> getGradeCode() {
        List<GradeCode> gradeCodes = studentService.getGrade();
        return ResponseEntity.ok(ApiUtils.success(gradeCodes));
    }

    @GetMapping("/transfer/{studentId}")
    public ResponseEntity<?> getTransferByStudentId(@PathVariable("studentId") Integer studentId) {

        List<StudentTransferDTO> transferDTO = studentService.findInOutByStudentId(studentId);
        return ResponseEntity.ok(ApiUtils.success(transferDTO));
    }

    @PostMapping("/inout")
    public String studentInOut(@ModelAttribute StudentReqDTO.StudentTransferDTO studentInOutDTO) {
        studentInOutDTO.getStudentNoList();
        studentService.transferStudent(studentInOutDTO);
        studentService.insertTransferHistory(studentInOutDTO);
        return "redirect:/student/transfer";
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
}
