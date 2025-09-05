package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageViewController {

    @GetMapping("/teacher")
    public String getManageTeacherPage() {
        return "manage/manage-teacher";
    }

}
