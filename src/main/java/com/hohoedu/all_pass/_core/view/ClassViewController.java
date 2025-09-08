package com.hohoedu.all_pass._core.view;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO;
import lombok.Data;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.InitRecordDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RecordStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.ClassRespDTO.TimeTableLabelDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.student.Student;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;

import static com.hohoedu.all_pass._core.vo.Constants.DAYS;

@Controller
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassViewController {

    private final UserService userService;
    private final ClassService classService;
    private final StudentService studentService;
    private final DateConfig dateConfig;

    // 시간표 등록
    @GetMapping("/timetable")
    public String getClassTimetable(
            @RequestParam("year") String year,
            @RequestParam("month") String month,
            Model model) {

        List<ClassCode> classCodes = classService.findClassCode();
        List<GradeCode> grades = classService.findGrade();
        List<UnitCode> unitCodes = classService.findUnitCode();
        List<Student> students = studentService.findStudentByCenterCode("DAE001");
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("unitCodes", unitCodes);
        model.addAttribute("grades", grades);
        model.addAttribute("days", DAYS);
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
            Model model) {
        List<User> users = userService.findByCenterNo("DAE001");
        model.addAttribute("users", users);
        model.addAttribute("days", DAYS);

        List<TimeTableDTO> tables = classService.findTableViewWithStudents(year, month, "DAE001cos");

        Map<String, Map<String, TimeTableDTO>> tableMap = tables.stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO::getDayname,
                        Collectors.toMap(
                                TimeTableDTO::getPeriodNo,
                                Function.identity())));
        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));
        model.addAttribute("tableMap", tableMap);
        return "class/timeview";
    }

    @GetMapping("/print-timeview")
    public String getPrintTimeView() {
        return "print/print-timeview";
    }

    // 수업 일지 페이지
    @GetMapping("/record")
    public String getClassRecordPage(Model model) {

        // user Session에서 centerCode 받아오기
        List<User> users = userService.findByCenterNo("DAE001");

        String today = dateConfig.currentYearMonth().get("today");
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");
        String dayName = dateConfig.currentYearMonth().get("currentDayName");

        List<InitRecordDTO> labels = classService.getTimeTableByDate(yy, mm, dayName, "DAE001cos");
        if (!labels.isEmpty()) {
            String classCode = labels.get(0).getTimeTableKey();
            List<RecordStudentDTO> students = classService.getTimeTableByClassCode(classCode, today);
            model.addAttribute("students", students);
        }
        model.addAttribute("users", users);
        model.addAttribute("labels", labels);

        return "class/record";
    }

    // 보강 페이지
    @GetMapping("/remedial")
    public String getClassRemedialPage(Model model,
                                       @RequestParam("year") String year,
                                       @RequestParam("month") String month) {

        List<RemedialDTO> remedials = classService.findRemedialByUserNo(year, month);
        List<RemedialDTO> rightRemedials = remedials.stream()
                .filter(RemedialDTO::isAction)
                .toList();

        List<RemedialDTO> leftRemedials = remedials.stream()
                .filter(r -> !r.isAction())
                .toList();

        model.addAttribute("rightRemedials", rightRemedials);
        model.addAttribute("leftRemedials", leftRemedials);
        return "class/remedial";
    }

    // 월간평가 (초등)
    @GetMapping("/monthly")
    public String getClassMonthlyPage(HttpSession session, Model model) {
        // UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
        // session.getAttribute("user");

        // if (user == null) {
        // return "login";
        // }

        // 센터별 선생님 목록
        List<User> users = userService.findByCenterNo("DAE001");

        // 클래스 리스트 가져오기
        List<TimeTableLabelDTO> labels = classService.getLabelsByYM(
                dateConfig.currentYearMonth().get("currentYear"),
                dateConfig.currentYearMonth().get("currentMonth"));

        // 첫번째 수업에 대한 학생 목록 가져오기
        if (!labels.isEmpty()) {
            List<MonthlyStudentDTO> students = classService
                    .getMonthlyClassDetail(labels.get(0).getTimeTableKey());
            model.addAttribute("students", students);
        }
        model.addAttribute("users", users);
        model.addAttribute("labels", labels);

        return "class/monthly";
    }

    @GetMapping("/infant")
    public String getMonthlyInfantPage() {
        return "class/infant";
    }

}
