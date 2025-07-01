package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/counsel")
public class CounselingViewController {
    
    @GetMapping("/main")
    public String getMethodName() {
        return "counsel/consult";
    }
    
}
