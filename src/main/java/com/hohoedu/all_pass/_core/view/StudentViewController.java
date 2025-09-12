package com.hohoedu.all_pass._core.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.family.model.RelationCode;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.StudentRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentViewController {

    private final StudentService studentService;
    private final ClassService classService;

    @GetMapping("/join")
    public String getStudentJoinPage(Model model) {

        List<GradeCode> gradeCodes = studentService.findGrade();
        List<RelationCode> relationCodes = studentService.findRelation();

        model.addAttribute("gradeCodes", gradeCodes);
        model.addAttribute("relationCodes", relationCodes);

        return "student/join";
    }

    @GetMapping("/main")
    public String getStudentMainPage(HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<MainStudentDTO> students = studentService.getStudentsByUserCode("all", user.getCenterCode());
        List<User> teachers = studentService.findTeacher(user.getCenterCode());
        List<TimeTableLabelDTO> labels = classService.getClassLabel("all");

        model.addAttribute("labels", labels);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);

        return "student/student-main";
    }

    @GetMapping("/transfer")
    public String getStudentInoutPage(Model model) {
        List<StudentInOutDTO> students = studentService.findAllInOut();
        List<User> teachers = studentService.findTeacher("DAE001");
        List<TimeTableLabelDTO> labels = classService.getClassLabel("all");

        model.addAttribute("labels", labels);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        return "student/student-inout";
    }

    @GetMapping("/print-transfer")
    public String getStudentTransferPrintPage() {
        return "print/print-student-transfer";
    }

    @GetMapping("/print-overview")
    public String getStudentOverviewPrintPage() {
        return "print/print-student-overview";
    }

    @GetMapping("/overview")
    public String getStudentOutPage(HttpSession session, Model model) {
        String currentYm = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String yearAgoYm = LocalDate.now().minusYears(1).plusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        studentService.saveSnapshot(currentYm);
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login"; // 로그인 페이지로 이동
        }
        List<StudentSnapshotRespDTO> snapshot = studentService.getSnapshot(yearAgoYm, currentYm, 1);
        List<User> teachers = studentService.findTeacher("1");

        model.addAttribute("snapshot", snapshot);
        model.addAttribute("teachers", teachers);
        return "student/student-overview";
    }

}
