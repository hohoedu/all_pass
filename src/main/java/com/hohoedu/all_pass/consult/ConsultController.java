package com.hohoedu.all_pass.consult;

import com.hohoedu.all_pass.consult._dto.ConsultReqDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/consult")
@RequiredArgsConstructor
public class ConsultController {

    private final ConsultService consultService;

    @PostMapping("/save")
    public String getMethodName(@RequestBody ConsultReqDTO.ConsultRegisterReqDTO reqDTO) {
        System.out.println("gradeNo = " + reqDTO.getGradeNo());
        System.out.println("inflowRouteNo = " + reqDTO.getInflowRouteNo());
        System.out.println(reqDTO.getStudentName());
        consultService.registerConsult(reqDTO);
        return "redirect:/consult/main";
    }

}
