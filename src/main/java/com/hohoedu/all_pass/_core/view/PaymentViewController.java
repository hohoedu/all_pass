package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentViewController {

    private final UserService userService;

    @GetMapping("/pay-edu")
    public String getPayEduPage(HttpSession session, Model model) {
//        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
//        if (user == null) {
//            return "redirect:/login";
//        }
//        List<User> users = userService.findByCenterCode(user.getCenterCode());
        List<User> users = userService.findByCenterCode("DAE001");
        model.addAttribute("users", users);
        return "pay/pay-edu";
    }

    @GetMapping("/pay-list")
    public String getPayListPage(HttpSession session) {
        return "pay/pay-list";
    }

}
