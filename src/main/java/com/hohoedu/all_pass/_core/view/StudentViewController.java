package com.hohoedu.all_pass._core.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hohoedu.all_pass.parent.code.RelationCode;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.model.Student;
import com.hohoedu.all_pass.student.service.StudentService;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/student")
public class StudentViewController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/main")
    public String getStudentMain(Model model) {
        List<Student> students = studentService.findAllByMyBatis();
        model.addAttribute("students", students);
        return "student/student-main";
    }

    @GetMapping("/join")
    public String getStudentJoin(Model model) {

        List<GradeCode> gradeCodes = studentService.findGrade();
        List<RelationCode> relationCodes = studentService.findRelation();
        model.addAttribute("gradeCodes", gradeCodes);
        model.addAttribute("relationCodes", relationCodes);
        return "student/join";
    }

    @GetMapping("/student-inout")
    public String getStudentInout() {
        return "student/student-inout";
    }

    @GetMapping("/print-student-inout")
    public String getMethodName() {
        return "print/print-student-inout";
    }
    

    @GetMapping("/student-out")
    public String getStudentOut() {
        return "student/student-out";
    }
}
