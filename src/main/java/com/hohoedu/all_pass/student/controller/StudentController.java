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
}
