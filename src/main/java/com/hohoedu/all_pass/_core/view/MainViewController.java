package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;

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

    @GetMapping("/juso")
    public String jusoPopup(
            @RequestParam(name = "inputYn", required = false) String inputYn,
            @RequestParam(name = "roadFullAddr", required = false) String roadFullAddr,
            @RequestParam(name = "roadAddrPart1", required = false) String roadAddrPart1,
            @RequestParam(name = "addrDetail", required = false) String addrDetail,
            Model model) {
        System.out.println("GET으로 주소 돌아옴");
        model.addAttribute("inputYn", inputYn);
        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart1", roadAddrPart1);
        model.addAttribute("addrDetail", addrDetail);
        return "juso"; // juso.html
    }

    @PostMapping("/juso")
    public String preventPostError() {
        return "redirect:/juso";
    }

}
