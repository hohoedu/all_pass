package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment._dto.PaymentRespDTO;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageViewController {

    private final UserService userService;
    private final ClassService classService;
    private final PaymentService paymentService;

    @GetMapping("/order")
    public String getManageOrderPage() {
        return "manage/order";
    }

    @GetMapping("/reorder")
    public String getManageReorderPage() {
        return "manage/reorder";
    }

    @GetMapping("sms")
    public String getManageSmsPage() {
        return "manage/sms";
    }

    @GetMapping("/teacher")
    public String getManageTeacherPage() {
        return "manage/teacher";
    }

    @GetMapping("tuition")
    public String getManageTuitionPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<ClassCode> classCodes = classService.findClassCode();

        List<ClassCode> hanClasses = classCodes.stream()
                .filter(c -> "1".equals(c.getClassType()))
                .toList();

        List<ClassCode> bookClasses = classCodes.stream()
                .filter(c -> "2".equals(c.getClassType()))
                .toList();

        List<PaymentRespDTO.ClassFeeMapDTO> feeMaps = paymentService.findClassFeeMapByCenterCode(user.getCenterCode());

        Map<String, String> bookFeeMap = feeMaps.stream()
                .filter(f -> "2".equals(f.getClassType()))
                .collect(Collectors.toMap(PaymentRespDTO.ClassFeeMapDTO::getClassKey, PaymentRespDTO.ClassFeeMapDTO::getFee));

        Map<String, String> hanFeeMap = feeMaps.stream()
                .filter(f -> "1".equals(f.getClassType()))
                .collect(Collectors.toMap(PaymentRespDTO.ClassFeeMapDTO::getClassKey, PaymentRespDTO.ClassFeeMapDTO::getFee));

        model.addAttribute("hanFeeMap", hanFeeMap);
        model.addAttribute("bookFeeMap", bookFeeMap);
        model.addAttribute("hanClasses", hanClasses);
        model.addAttribute("bookClasses", bookClasses);
        model.addAttribute("centerCode", user.getCenterCode());

        return "manage/tuition";
    }

    @GetMapping("notice")
    public String getManageNoticePage() {
        return "manage/notice";
    }
}
