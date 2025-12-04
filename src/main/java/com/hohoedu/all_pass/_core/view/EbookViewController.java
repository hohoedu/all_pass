package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass.admin.center.Center;
import com.hohoedu.all_pass.admin.center.CenterService;
import com.hohoedu.all_pass.class_instance.ClassService;
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
public class EbookViewController {

    private final ClassService classService;
    private final CenterService centerService;

    @GetMapping("/person")
    public String getAdminPersonPage(Model model, HttpSession session) {
        List<UnitCode> unitCodes = classService.findUnitCodeForPerson();
        List<Center> center = centerService.findAllCenter();
        model.addAttribute("unitCodes", unitCodes);
        model.addAttribute("center", center);
        return "/admin/ebook/person";
    }
}
