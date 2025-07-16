package com.hohoedu.all_pass._core.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance.code.ClassCode;
import com.hohoedu.all_pass.class_instance.code.UnitCode;
import com.hohoedu.all_pass.class_instance.service.ClassService;
import com.hohoedu.all_pass.student.code.GradeCode;
import com.hohoedu.all_pass.student.model.Student;
import com.hohoedu.all_pass.student.service.StudentService;
import com.hohoedu.all_pass.user.model.User;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassViewController {

        private final ClassService classService;
        private final StudentService studentService;

        // 시간표 등록
        @GetMapping("/timetable")
        public String getClassTimetable(
                        @RequestParam("year") String year,
                        @RequestParam("month") String month,

                        Model model) {
                System.out.println("year = " + year);
                System.out.println("month = " + month);

                List<ClassCode> classCodes = classService.findClassCode();
                List<GradeCode> grades = classService.findGrade();
                List<UnitCode> unitCodes = classService.findUnitCode();
                List<Student> students = studentService.findAllByMyBatis();
                model.addAttribute("classCodes", classCodes);
                model.addAttribute("unitCodes", unitCodes);
                model.addAttribute("grades", grades);
                model.addAttribute("days", List.of(
                                Map.of("id", "mon", "label", "월"),
                                Map.of("id", "tue", "label", "화"),
                                Map.of("id", "wed", "label", "수"),
                                Map.of("id", "thu", "label", "목"),
                                Map.of("id", "fri", "label", "금"),
                                Map.of("id", "sat", "label", "토")));
                model.addAttribute("students", students);

                List<TimeTableDTO> tables = classService.findTimeTableWithStudents(year, month);

                Map<String, Map<String, TimeTableDTO>> tableMap = tables.stream()
                                .collect(Collectors.groupingBy(
                                                TimeTableDTO::getDayname,
                                                Collectors.toMap(
                                                                TimeTableDTO::getPeriodNo,
                                                                Function.identity())));

                model.addAttribute("tableMap", tableMap);

                return "class/timetable";
        }

        // 시간표 조회
        @GetMapping("/timeview")
        public String getClassTimeView(
                        @RequestParam("year") String year,
                        @RequestParam("month") String month,
                        @RequestParam("user") Integer userNo,
                        Model model) {
                List<User> users = classService.findUsers();
                model.addAttribute("users", users);
                List<Map<String, String>> days = List.of(
                                Map.of("id", "mon", "label", "월"),
                                Map.of("id", "tue", "label", "화"),
                                Map.of("id", "wed", "label", "수"),
                                Map.of("id", "thu", "label", "목"),
                                Map.of("id", "fri", "label", "금"),
                                Map.of("id", "sat", "label", "토"));
                model.addAttribute("days", days);

                List<TimeTableDTO> tables = classService.findTableViewWithStudents(year, month, userNo);

                Map<String, Map<String, TimeTableDTO>> tableMap = tables.stream()
                                .collect(Collectors.groupingBy(
                                                TimeTableDTO::getDayname,
                                                Collectors.toMap(
                                                                TimeTableDTO::getPeriodNo,
                                                                Function.identity())));
                days.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));
                model.addAttribute("tableMap", tableMap);
                return "class/timeview";
        }

        @GetMapping("/print-timeview")
        public String getPrintTimeView() {
                return "print/print-timeview";
        }

}
