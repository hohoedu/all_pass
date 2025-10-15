package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageViewController {


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
    public String getManageTuitionPage() {
        return "manage/tuition";
    }

    @GetMapping("notice")
    public String getManageNoticePage() {
        return "manage/notice";
    }
}
