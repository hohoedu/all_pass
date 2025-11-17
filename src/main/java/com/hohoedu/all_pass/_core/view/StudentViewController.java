package com.hohoedu.all_pass._core.view;

import java.time.YearMonth;
import java.util.List;

import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.family.model.RelationCode;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.MainStudentDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentInOutDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO.StudentSnapshotRespDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentViewController {

    private final StudentService studentService;
    private final ClassService classService;

    // 학생 등록
    @GetMapping("/join")
    public String getStudentJoinPage(Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        List<GradeCode> gradeCodes = studentService.findGrade();
        List<RelationCode> relationCodes = studentService.findRelation();

        model.addAttribute("centerCode", user.getCenterCode());
        model.addAttribute("gradeCodes", gradeCodes);
        model.addAttribute("relationCodes", relationCodes);

        return "student/join";
    }

    // 학생 정보 메인
    @GetMapping("/main")
    public String getStudentMainPage(HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<MainStudentDTO> students = studentService.getStudentsByUserCode("all", user.getCenterCode());
        List<User> teachers = studentService.findTeacher(user.getCenterCode());
        List<TimeTableLabelDTO> labels = classService.getAllClassLabel("all");

        model.addAttribute("labels", labels);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);

        return "student/student-main";
    }

    // 전입 전출
    @GetMapping("/transfer")
    public String getStudentInoutPage(Model model, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<StudentInOutDTO> students = studentService.findAllInOut(user.getCenterCode());
        List<User> teachers = studentService.findTeacher(user.getCenterCode());
        List<TimeTableLabelDTO> labels = classService.getAllClassLabel("all");

        model.addAttribute("labels", labels);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);
        return "student/student-inout";
    }

    // 전입 전출 현황 출력화면
    @GetMapping("/print-transfer")
    public String getStudentTransferPrintPage() {
        return "print/print-student-transfer";
    }

    // 학생 통계 화면
    @GetMapping("/overview")
    public String getStudentOverview(HttpSession session, Model model) {
        var user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        final String centerCode = user.getCenterCode();

        // 최근 12개월(포함) 범위
        YearMonth nowYm = YearMonth.now();
        YearMonth startYm = nowYm.minusMonths(11);
        String currentYm = nowYm.toString().replace("-", "");   // yyyyMM
        String yearAgoYm = startYm.toString().replace("-", ""); // yyyyMM

        // 월별 스냅샷 업서트
        for (YearMonth ym = startYm; !ym.isAfter(nowYm); ym = ym.plusMonths(1)) {
            studentService.upsertSnapshot(centerCode, ym.toString().replace("-", ""));
        }

        // 범위 조회
        List<StudentSnapshotRespDTO> snapshot = studentService.getSnapshotRange(centerCode, yearAgoYm, currentYm);

        model.addAttribute("snapshot", snapshot);
        model.addAttribute("centerCode", centerCode);
        model.addAttribute("currentYm", currentYm);
        model.addAttribute("startYm", yearAgoYm);

        return "/student/student-overview";
    }

    // 학생 통계 출력 화면
    @GetMapping("/print-overview")
    public String getStudentOverviewPrintPage() {
        return "print/print-student-overview";
    }

}
