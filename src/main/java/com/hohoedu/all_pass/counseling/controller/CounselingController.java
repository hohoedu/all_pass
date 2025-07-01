package com.hohoedu.all_pass.counseling.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/consult")
public class CounselingController {

    @GetMapping("/main")
    public String getMethodName() {
        return "consult";
    }

}
