package com.hohoedu.all_pass._core.view;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hohoedu.all_pass.parent.code.RelationCode;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.model.Student;
import com.hohoedu.all_pass.student.service.StudentService;
import com.hohoedu.all_pass.user.model.User;


@Controller
@RequestMapping("/student")
public class StudentViewController {

    @Autowired
    private StudentService studentService;

    @GetMapping("/main")
    public String getStudentMainPage(Model model) {
        List<Student> students = studentService.findAllByMyBatis();
        List<User> teachers = studentService.findTeacher();
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);

        return "student/student-main";
    }

    @GetMapping("/join")
    public String getStudentJoinPage(Model model) {

        List<GradeCode> gradeCodes = studentService.findGrade();
        List<RelationCode> relationCodes = studentService.findRelation();
        model.addAttribute("gradeCodes", gradeCodes);
        model.addAttribute("relationCodes", relationCodes);

        return "student/join";
    }

    @GetMapping("/student-inout")
    public String getStudentInoutPage(Model model) {
        List<StudentInOutDTO> students = studentService.findAllInOut();
        List<User> teachers = studentService.findTeacher();
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        return "student/student-inout";
    }

    @GetMapping("/print-student-inout")
    public String getStudentInoutPrintPage() {
        return "print/print-student-inout";
    }

    @GetMapping("/student-out")
    public String getStudentOutPage(Model model) {
        List<User> teachers = studentService.findTeacher();
        model.addAttribute("teachers", teachers);
        return "student/student-out";
    }
}
