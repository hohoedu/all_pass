package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MainViewController {

    @GetMapping({"/", "/home"})
    public String getIndexPage(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:https://highreader.co.kr";
        }
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

    // 주소 팝업창 열기
    @GetMapping("/juso")
    public String jusoPopup() {
        return "juso";
    }

    @PostMapping("/schedule/select")
    public String getMethodName() {
        return "test";
    }

    // 주소값 리턴
    @PostMapping("/juso")
    public String preventPostError(HttpServletRequest request, RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("roadFullAddr", request.getParameter("roadFullAddr"));
        redirectAttributes.addAttribute("roadAddrPart1", request.getParameter("roadAddrPart1"));
        redirectAttributes.addAttribute("roadAddrPart2", request.getParameter("roadAddrPart2"));
        redirectAttributes.addAttribute("addrDetail", request.getParameter("addrDetail"));

        return "redirect:/jusoCallBack";
    }

    // Callback
    @GetMapping("/jusoCallBack")
    public String jusoCallback(
            @RequestParam(name = "roadFullAddr", required = false) String roadFullAddr,
            @RequestParam(name = "roadAddrPart1", required = false) String roadAddrPart1,
            @RequestParam(name = "roadAddrPart2", required = false) String roadAddrPart2,
            @RequestParam(name = "addrDetail", required = false) String addrDetail,
            Model model) {

        String roadAddrPart = roadAddrPart1 + " " + roadAddrPart2;

        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart", roadAddrPart);
        model.addAttribute("addrDetail", addrDetail);

        return "juso-callback";
    }

    @GetMapping("/school")
    public String getSchool() {
        return "school";
    }

}
