package com.hohoedu.all_pass.student.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.student._dto.StudentReqDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentTransferDTO;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.model.Student;
import com.hohoedu.all_pass.student.service.StudentService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    final private StudentService studentService;

    @ResponseBody
    @GetMapping("/jpa")
    public List<Student> allStudentsByJpa() {

        return studentService.findAllByJpa();
    }

    @ResponseBody
    @GetMapping("/mybatis")
    public List<Student> allStudentsByMyBatis() {

        return studentService.findAllByMyBatis();
    }

    @ResponseBody
    @GetMapping("/students")
    public ResponseEntity<?> findStudents() {

        List<Student> students = studentService.findAllByMyBatis();
        return ResponseEntity.ok(ApiUtils.success(students));
    }

    @ResponseBody
    @GetMapping("/{studentId}")
    public ResponseEntity<?> findStudentByStudentNo(@PathVariable("studentId") Integer studentId) {

        System.out.println("컨트롤러의 studentId = " + studentId);

        StudentRespDTO.StudentDTO student = studentService.findStudentByStudentId(studentId);

        return ResponseEntity.ok(ApiUtils.success(student));
    }

    @PostMapping("/join")
    public String studentJoin(@ModelAttribute StudentReqDTO.StudentJoinDTO studentDTO,
            @ModelAttribute StudentReqDTO.ParentJoinDTO parentDTO) {

        studentService.studentInsert(studentDTO, parentDTO);

        return "redirect:/main";
    }

    @ResponseBody
    @PostMapping("/status")
    public void statusUpdate(@ModelAttribute StudentReqDTO.StatusHistoryDTO historyDTO) {
        studentService.statusInsert(historyDTO);

    }

    @GetMapping("/gradeCodes")
    public ResponseEntity<?> getGradeCode() {
        List<GradeCode> gradeCodes = studentService.getGrade();
        return ResponseEntity.ok(ApiUtils.success(gradeCodes));
    }

    @GetMapping("/inout/{studentId}")
    public ResponseEntity<?> findInOutByStudentId(@PathVariable("studentId") Integer studentId) {

        System.out.println("===========================");
        System.out.println("컨트롤러 진입!");
        System.out.println("student = " + studentId);
        System.out.println("===========================");
        List<StudentTransferDTO> transferDTO = studentService.findInOutByStudentId(studentId);
        return ResponseEntity.ok(ApiUtils.success(transferDTO));
    }

    @PostMapping("/inout")
    public String studentInOut(@ModelAttribute StudentReqDTO.StudentTransferDTO studentInOutDTO) {
        System.out.println("출력!");

        String inoutHan = studentInOutDTO.getInoutHan();
        String inoutRead = studentInOutDTO.getInoutRead();
        String teacher = studentInOutDTO.getTeacherNo();
        List<String> student = studentInOutDTO.getStudentNo();
        String moveAt = studentInOutDTO.getMoveAt();
        String transferReason = studentInOutDTO.getTransferReason();

        System.out.println("======================");
        System.out.println("inoutHan = " + inoutHan);
        System.out.println("inoutRead = " + inoutRead);
        System.out.println("teacherNo = " + teacher);
        System.out.println("studentNo = " + student);
        System.out.println("moveAt = " + moveAt);
        System.out.println("transferReason = " + transferReason);
        System.out.println("======================");
        studentService.transferStudent(studentInOutDTO);
        return "redirect:/student/student-inout";
    }

}
