package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.payment.PaymentService;
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
    private final PaymentService paymentService;


    @GetMapping("/pay-edu")
    public String getPayEduPage(HttpSession session, Model model) {
//        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
//        if (user == null) {
//            return "redirect:/login";
//        }
//        List<User> users = userService.findByCenterCode(user.getCenterCode());
        List<User> users = userService.findByCenterCode("DAE001");
        List<PaymentRespDTO.AssignStudentsDTO> students = paymentService.findByAssignStudent("2025", "10", "all");

        model.addAttribute("users", users);
        model.addAttribute("students", students);
        return "pay/pay-edu";
    }

    @GetMapping("/pay-list")
    public String getPayListPage(HttpSession session) {
        return "pay/pay-list";
    }

}
