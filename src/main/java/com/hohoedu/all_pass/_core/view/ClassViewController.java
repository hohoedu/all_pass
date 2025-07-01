package com.hohoedu.all_pass._core.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("class")
public class ClassViewController {

    @GetMapping("/timetable")
    public String getClassTimetable() {
        return "class/class-timetable";
    }

    @GetMapping("/timeview")
    public String getClassTimeView() {
        return "class/timeview";
    }

}
