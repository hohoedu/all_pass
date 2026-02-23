package com.hohoedu.all_pass._core.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.hohoedu.all_pass.consult.ConsultService;
import com.hohoedu.all_pass.consult._dto.ConsultRespDTO;
import com.hohoedu.all_pass.consult.model.InflowRoute;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.GradeCode;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import static com.hohoedu.all_pass._core.vo.Constants.DAYS;

@Controller
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultViewController {

    private final StudentService studentService;
    private final ConsultService consultService;
    private final UserService userService;

    @GetMapping("/main")
    public String getConsultMainPase(Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

//        List<User> teachers = userService.findByCenterCode(user);
        List<User> teachers = userService.findAllUserCode(user);
        List<GradeCode> grades = studentService.findGrade();
        List<InflowRoute> routes = consultService.findInflowRoute();
//        List<ConsultRespDTO.ConsultDTO> consults = consultService.findConsult(user.getCenterCode(), userCode);
        model.addAttribute("user", user);
        model.addAttribute("teachers", teachers);
        model.addAttribute("grades", grades);
        model.addAttribute("routes", routes);
//        model.addAttribute("consults", consults);
        return "consult/consult";
    }

    @GetMapping("/print-consult")
    public String getPrintTimeView(Model model, HttpSession session) {

        List<ConsultRespDTO.ConsultPrintDTO> consults = consultService.findConsultForPrint("PUS002bbun2");

        model.addAttribute("consults", consults);
        model.addAttribute("days", DAYS);

        return "print/print-consult";
    }

    @GetMapping("/test")
    public String getConsultTestPage(Model model) {

        return "consult/consult-test";
    }

    @GetMapping("/level")
    public String getConsultLevelPage(Model model) {

        return "consult/level-test_h";
    }

}
