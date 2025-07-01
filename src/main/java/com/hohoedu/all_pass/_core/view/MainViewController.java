package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainViewController {

    @GetMapping({ "/", "/home" })
    public String getIndexPage() {
        return "index";
    }

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    } 

    @GetMapping("/main")
    public String getMainPage() {
        return "main";
    }

}
