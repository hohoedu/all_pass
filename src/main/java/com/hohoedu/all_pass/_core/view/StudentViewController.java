package com.hohoedu.all_pass._core.view;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.hohoedu.all_pass.consult.ConsultService;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebReqDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;

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
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;
import org.springframework.web.bind.annotation.RequestParam;
import org.threeten.bp.LocalDate;

@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentViewController {

    private final StudentService studentService;
    private final ClassService classService;
    private final UserService userService;
    private final ConsultService consultService;

    // 학생 등록
    @GetMapping("/web/join")
    public String getStudentJoinPage(Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        List<GradeCode> gradeCodes = studentService.findGrade();
        List<RelationCode> relationCodes = studentService.findRelation();

        model.addAttribute("centerCode", user.getCenterCode());
        model.addAttribute("gradeCodes", gradeCodes);
        model.addAttribute("relationCodes", relationCodes);

        return "student/join";
    }

    @GetMapping("/mobile/join")
    public String getStudentJoinPageByParent(@RequestParam(value = "centerCode") String centerCode,
            @RequestParam(required = false) String invite, Model model, HttpSession session) {

        List<GradeCode> gradeCodes = studentService.findGrade();
        List<RelationCode> relationCodes = studentService.findRelation();

        StudentWebRespDTO.PendingStudentRespDTO pendingStudent = studentService.findStudentByInviteCode(invite);

        model.addAttribute("centerCode", centerCode);
        model.addAttribute("inviteCode", invite);
        model.addAttribute("pendingStudent", pendingStudent);
        model.addAttribute("gradeCodes", gradeCodes);
        model.addAttribute("relationCodes", relationCodes);

        return "student/join";
    }

    // 학생 정보 메인
    @GetMapping("/main")
    public String getStudentMainPage(HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<MainStudentDTO> students = studentService.getStudentsByUserCode(user.getUserCode(), user.getCenterCode());
        List<User> teachers = userService.findActiveUser(user);
        List<TimeTableLabelDTO> labels = classService.getAllClassLabel(user.getUserCode());
        model.addAttribute("user", user);
        model.addAttribute("labels", labels);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);

        return "student/student-main";
    }

    // 신규 입회 등록 관리
    @GetMapping("/new")
    public String getStudentNewPage(Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<StudentWebRespDTO.PendingStudentRespDTO> students = studentService
                .findPendingStudent(user.getCenterCode());

        model.addAttribute("students", students);

        return "student/student-new";
    }

    // 전입 전출
    @GetMapping("/transfer")
    public String getStudentInoutPage(Model model, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<StudentInOutDTO> students = studentService.findAllInOut(user.getCenterCode(), user.getUserCode());
        // List<StudentInOutDTO> students =
        // studentService.findStudentByUserCode(userCode);
        List<User> teachers = userService.findActiveUser(user);
        // List<TimeTableLabelDTO> labels = classService.getAllClassLabel(userCode);
        // model.addAttribute("labels", labels);
        model.addAttribute("students", students);
        model.addAttribute("teachers", teachers);

        return "student/student-inout";
    }

    // 입탈퇴 현황
    @GetMapping("/withdraw")
    public String getStudentWithdrawPage(Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        YearMonth ym = YearMonth.now();

        List<User> teachers = userService.findActiveUser(user);

        boolean isMeInList = teachers.stream()
                .anyMatch(t -> t.getUserCode().equals(user.getUserCode()));
        model.addAttribute("user", user);
        model.addAttribute("teachers", teachers);
        model.addAttribute("isMeInList", isMeInList);
        model.addAttribute("selectedMonthValue", ym.toString());

        return "student/student_withdrawal";
    }

    @GetMapping("/print-withdraw")
    public String getStudentWithdrawPrintPage(
            @RequestParam String ym,
            @RequestParam String userCode,
            @RequestParam String tab,
            Model model, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        StudentWebReqDTO.WithdrawReqDTO req = new StudentWebReqDTO.WithdrawReqDTO();
        req.setYm(ym);
        req.setUserCode(userCode);

        if ("all".equals(tab)) {
            model.addAttribute("joinGrouped", studentService.findJoinList(req).stream()
                    .collect(Collectors.groupingBy(s -> s.getTeacherName() != null ? s.getTeacherName() : "미지정")));
            model.addAttribute("withdrawGrouped", studentService.findWithdrawList(req).stream()
                    .collect(Collectors.groupingBy(s -> s.getTeacherName() != null ? s.getTeacherName() : "미지정")));
            model.addAttribute("transferInGrouped", studentService.findTransferInList(req).stream()
                    .collect(Collectors
                            .groupingBy(s -> s.getTransferInTeacher() != null ? s.getTransferInTeacher() : "미지정")));
            model.addAttribute("transferOutGrouped", studentService.findTransferOutList(req).stream()
                    .collect(Collectors
                            .groupingBy(s -> s.getTransferOutTeacher() != null ? s.getTransferOutTeacher() : "미지정")));
            model.addAttribute("graduateGrouped", studentService.findGraduateList(req).stream()
                    .collect(Collectors.groupingBy(s -> s.getTeacherName() != null ? s.getTeacherName() : "미지정")));

            // 상담 - 해당 월 첫날 ~ 마지막날
            LocalDate firstDay = LocalDate.parse(ym + "-01");
            String startDate = firstDay.toString();
            String endDate = firstDay.withDayOfMonth(firstDay.lengthOfMonth()).toString();

            Map<String, List<ConsultRespDTO.ConsultPrintDTO>> consultGrouped = consultService
                    .findConsultForPrint(userCode, startDate, endDate)
                    .stream()
                    .collect(Collectors.groupingBy(s -> s.getUsername() != null ? s.getUsername() : "미지정"));

            model.addAttribute("consultGrouped", consultGrouped);
            model.addAttribute("tab", "all");
            model.addAttribute("tabName", "전체");
            model.addAttribute("ym", ym);
            return "print/print-student-withdrawal";
        }

        String tabName = switch (tab) {
            case "t1" -> "입회";
            case "t2" -> "탈퇴";
            case "t3" -> "전입";
            case "t4" -> "전출";
            case "t5" -> "졸업";
            default -> "";
        };

        Map<String, ?> groupedByTeacher = switch (tab) {
            case "t1" -> studentService.findJoinList(req).stream()
                    .collect(Collectors.groupingBy(s -> s.getTeacherName() != null ? s.getTeacherName() : "미지정"));
            case "t2" -> studentService.findWithdrawList(req).stream()
                    .collect(Collectors.groupingBy(s -> s.getTeacherName() != null ? s.getTeacherName() : "미지정"));
            case "t3" -> studentService.findTransferInList(req).stream()
                    .collect(Collectors
                            .groupingBy(s -> s.getTransferInTeacher() != null ? s.getTransferInTeacher() : "미지정"));
            case "t4" -> studentService.findTransferOutList(req).stream()
                    .collect(Collectors
                            .groupingBy(s -> s.getTransferOutTeacher() != null ? s.getTransferOutTeacher() : "미지정"));
            case "t5" -> studentService.findGraduateList(req).stream()
                    .collect(Collectors.groupingBy(s -> s.getTeacherName() != null ? s.getTeacherName() : "미지정"));
            default -> new HashMap<>();
        };

        model.addAttribute("groupedByTeacher", groupedByTeacher);
        model.addAttribute("tab", tab);
        model.addAttribute("tabName", tabName);
        model.addAttribute("ym", ym);

        return "print/print-student-withdrawal";
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
        if (user == null)
            return "redirect:/login";

        List<User> users = userService.findByCenterCode(user);
        model.addAttribute("users", users);

        return "/student/student-overview";
    }

    // 학생 통계 출력 화면
    @GetMapping("/print-overview")
    public String getStudentOverviewPrintPage() {
        return "print/print-student-overview";
    }

    @GetMapping("/privacy-print")
    public String consentPage(
            @RequestParam(required = false) String yy,
            @RequestParam(required = false) String mm,
            Model model, HttpSession session) {

        LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        LocalDate now = LocalDate.now();
        if (yy == null || yy.isBlank())
            yy = String.valueOf(now.getYear());
        if (mm == null || mm.isBlank())
            mm = String.format("%02d", now.getMonthValue());

        List<StudentWebRespDTO.PrivacyStudentListDTO> students = studentService.getStudentList(user.getCenterCode(), yy,
                mm);

        model.addAttribute("students", students);
        model.addAttribute("yy", yy);
        model.addAttribute("mm", mm);
        model.addAttribute("centerCode", user.getCenterCode());
        return "print/print-privacy";
    }

}
