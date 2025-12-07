package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass.admin.AdminService;
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

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor()
public class AdminViewController {

    private final ClassService classService;
    private final CenterService centerService;
    private final AdminService adminService;

    @GetMapping("/ebook/person")
    public String getAdminPersonPage(Model model, HttpSession session) {
        List<UnitCode> unitCodes = classService.findUnitCodeForPerson();
        List<Center> center = centerService.findAllCenter();
        model.addAttribute("unitCodes", unitCodes);
        model.addAttribute("center", center);
        return "/admin/ebook/person";
    }

    @GetMapping("/app/book")
    public String book(Model model, HttpSession session) {
        List<ClassCode> classCodes = classService.findClassCodeExcludeMid();
        List<SubjectCode> subjects = adminService.findSubjects();
        model.addAttribute("subjects", subjects);
        model.addAttribute("classCodes", classCodes);
        return "admin/app/book-suggest";
    }
}
