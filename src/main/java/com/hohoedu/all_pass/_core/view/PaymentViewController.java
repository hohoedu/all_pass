package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.CardCode;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentViewController {

    private final UserService userService;
    private final PaymentService paymentService;

    @GetMapping("/pay-edu")
    public String getPayEduPage(@RequestParam("year") String year, @RequestParam("month") String month, HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
//        String userCode = user.getRoleKey().equals("ADMIN") ? "all" : user.getUserCode();

        List<User> users = userService.findByCenterCode(user);
//        List<PaymentRespDTO.AssignStudentsDTO> students = paymentService.findByAssignStudent(year, month, "all", user.getCenterCode(), "EDU_FEE");

        model.addAttribute("user", user);
        model.addAttribute("users", users);
//        model.addAttribute("students", students);
        return "pay/pay-edu";
    }

    @GetMapping("/pay-list")
    public String getPayListPage(@RequestParam("year") String year, @RequestParam("month") String month, HttpSession session, Model model) {
        LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }


        List<PaymentRespDTO.MonthlyPaymentDTO> payments = paymentService.findMonthlyPayments(user.getUserCode(), user.getCenterCode(), year, month);
        List<CardCode> cardCode = paymentService.findCardCode();
        model.addAttribute("cardCode", cardCode);
        model.addAttribute("payments", payments);
        return "pay/pay-list";
    }

}
