package com.hohoedu.all_pass._core.view;

import com.google.api.Http;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainViewController {

    private final UserService userService;
    private final ClassService classService;


    @GetMapping({"/", "/home"})
    public String getIndexPage(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:https://highreader.co.kr";
        }
        return "index";
    }

    @GetMapping("/index")
    public String getIndex(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping({"/admin", "/login"})
    public String getLoginPage() {
        return "login";
    }

    // 메인화면 열기
    @GetMapping("/main")
    public String getMainPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<User> users = userService.findByCenterCode(user.getCenterCode());
        List<ClassRespDTO.MainClassSummaryDTO> classSummary = classService.getClassSummary(user.getCenterCode(), "all");


        model.addAttribute("users", users);
        model.addAttribute("classSummary", classSummary);

        return "main";
    }

    // 주소 팝업창 열기
    @GetMapping("/juso")
    public String jusoPopup() {
        return "juso";
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

    @PostMapping("/eclass/redirect")
    public void redirectToEClass(HttpSession session, HttpServletResponse response) throws IOException {

        UserRespDTO.LoginRespDTO user =
                (UserRespDTO.LoginRespDTO) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect("/login");
            return;
        }

        String userCode = user.getUserCode();
        String centerCode = user.getCenterCode();

        String redirectUrl = "http://hohoseodang.com/eclass_center_erp.html"
                + "?userCode=" + URLEncoder.encode(userCode, StandardCharsets.UTF_8)
                + "&centerCode=" + URLEncoder.encode(centerCode, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }

}
