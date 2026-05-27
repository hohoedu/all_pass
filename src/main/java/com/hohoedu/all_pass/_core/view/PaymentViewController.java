package com.hohoedu.all_pass._core.view;

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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/pay")
@RequiredArgsConstructor
public class PaymentViewController {

    private final UserService userService;
    private final PaymentService paymentService;

    @GetMapping("/pay-edu")
    public String getPayEduPage(@RequestParam("year") String year, @RequestParam("month") String month,
                                HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        // String userCode = user.getRoleKey().equals("ADMIN") ? "all" :
        // user.getUserCode();

        List<User> users = userService.findActiveUser(user);
        // List<PaymentRespDTO.AssignStudentsDTO> students =
        // paymentService.findByAssignStudent(year, month, "all", user.getCenterCode(),
        // "EDU_FEE");

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        // model.addAttribute("students", students);
        return "pay/pay-edu";
    }

    @GetMapping("/pay-list")
    public String getPayListPage(@RequestParam("year") String year, @RequestParam("month") String month,
                                 HttpSession session, Model model) {
        LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<PaymentRespDTO.MonthlyPaymentDTO> payments = paymentService.findMonthlyPayments(user.getUserCode(),
                user.getCenterCode(), year, month);
        List<CardCode> cardCode = paymentService.findCardCode();
        model.addAttribute("cardCode", cardCode);
        model.addAttribute("payments", payments);
        return "pay/pay-list";
    }

    @GetMapping("/print-cashbill")
    public String getCashbillPrint(@RequestParam String ym,
                                   @RequestParam(required = false) String ids,
                                   Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        String yy = ym.substring(0, 4);
        String mm = ym.substring(4, 6);
        log.info("ym: {}, ids: {}", ym, ids);

        List<PaymentRespDTO.CashbillPrintDTO> printDTO = paymentService.findCashbillPrint(yy, mm, user);

        // ids 파라미터가 있으면 선택된 항목만 필터링
        if (ids != null && !ids.isBlank()) {
            List<String> selectedIds = Arrays.asList(ids.split(","));
            printDTO = printDTO.stream()
                    .filter(dto -> selectedIds.contains(String.valueOf(dto.getBillId())))
                    .collect(Collectors.toList());
        }

        model.addAttribute("yy", yy);
        model.addAttribute("mm", mm);
        model.addAttribute("printDTO", printDTO);

        return "print/print-cashbill";
    }

    @GetMapping("/pay-receipt")
    public String getReceiptPage(@RequestParam(required = false) String yy, @RequestParam(required = false) String mm,
                                 Model model, HttpSession session) {

        LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");
        if (user == null) return "redirect:/login";

        String centerCode = user.getCenterCode();

        LocalDate now = LocalDate.now();
        if (yy == null || yy.isEmpty()) yy = String.valueOf(now.getYear());
        if (mm == null || mm.isEmpty()) mm = String.format("%02d", now.getMonthValue());

        List<PaymentRespDTO.PaidStudentListDTO> students =
                paymentService.getPaidStudentList(centerCode, yy, mm, null);

        model.addAttribute("students", students);
        model.addAttribute("yy", yy);
        model.addAttribute("mm", mm);
        return "pay/pay-receipt";
    }

}
