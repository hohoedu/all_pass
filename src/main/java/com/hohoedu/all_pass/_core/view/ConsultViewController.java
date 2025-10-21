package com.hohoedu.all_pass._core.view;

import java.util.List;

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

@Controller
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultViewController {

    private final StudentService studentService;
    private final ConsultService consultService;

    @GetMapping("/main")
    public String getConsultMainPase(Model model) {
        List<GradeCode> grades = studentService.findGrade();
        List<InflowRoute> routes = consultService.findInflowRoute();
        List<ConsultRespDTO.ConsultDTO> consults = consultService.findConsult();
        model.addAttribute("grades", grades);
        model.addAttribute("routes", routes);
        model.addAttribute("consults", consults);
        return "consult/consult";
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
