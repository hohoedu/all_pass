package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass.admin.AdminService;
import com.hohoedu.all_pass.admin._dto.AdminRespDTO;
import com.hohoedu.all_pass.admin.model.SubjectCode;
import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.center.CenterService;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor()
public class AdminViewController {

    private final ClassService classService;
    private final CenterService centerService;
    private final AdminService adminService;

    @GetMapping("/order/order-list")
    public String getAdminOrderList(Model model, HttpSession session) {
        List<Center> center = centerService.findAllCenter();
        adminService.findAdminOrderList();

        model.addAttribute("center", center);

        return "/admin/order/order-list";
    }

    @GetMapping("/order/order-list2")
    public String getAdminOrderList2(Model model, HttpSession session) {
        List<Center> center = centerService.findAllCenter();
        adminService.findAdminOrderList();

        model.addAttribute("center", center);

        return "/admin/order/2logistics_2order";
    }

    @GetMapping("/ebook/person")
    public String getAdminPersonPage(Model model, HttpSession session) {
        List<UnitCode> unitCodes = classService.findUnitCodeForPerson();
        List<Center> center = centerService.findAllCenter();
        model.addAttribute("unitCodes", unitCodes);
        model.addAttribute("center", center);
        return "/admin/ebook/person";
    }

    @GetMapping("/app/record")
    public String getAdminRecordPage(Model model, HttpSession session) {
        return "/admin/app/record";
    }

    @GetMapping("/app/infant")
    public String getAdminInfantPage(Model model, HttpSession session) {
        return "/admin/app/infant";
    }

    @GetMapping("/app/monthly")
    public String getAdminMonthlyPage(Model model, HttpSession session) {
        return "/admin/app/monthly";
    }


    @GetMapping("/app/book")
    public String book(Model model, HttpSession session) {
        List<ClassCode> classCodes = classService.findClassCodeExcludeMid();
        List<SubjectCode> subjects = adminService.findSubjects();
        List<AdminRespDTO.BookSuggestViewDTO> list = adminService.findBookSuggest();

        // week 기준 Map (1~4)
        Map<Integer, AdminRespDTO.BookSuggestViewDTO> bookMap =
                list.stream()
                        .collect(Collectors.toMap(
                                b -> Integer.parseInt(b.getWeek()),
                                b -> b,
                                (a, b) -> a
                        ));
        model.addAttribute("subjects", subjects);
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("bookMap", bookMap);
        return "admin/app/book-suggest";
    }
}
