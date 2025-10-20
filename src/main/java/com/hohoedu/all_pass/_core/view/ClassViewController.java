package com.hohoedu.all_pass._core.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.MonthlyStudentDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.RemedialDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO.TimeTableLabelDTO;
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
    public String getClassTimetable(@RequestParam("year") String year, @RequestParam("month") String month, Model model, HttpSession session) throws JsonProcessingException {

        // 세션 확인
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        System.out.println("==================findClassCode===================");
        List<ClassCode> classCodes = classService.findClassCode();
        System.out.println("==================findGrade=======================");
        List<GradeCode> grades = classService.findGrade();
        System.out.println("==================findClassUnits===================");
        Map<String, List<UnitCode>> classUnitMap = classService.findClassUnits();
        ObjectMapper mapper = new ObjectMapper();
        String classUnits = mapper.writeValueAsString(classUnitMap);
        System.out.println("==================findStudenetByCenterCode===================");
        List<Student> students = studentService.findStudentByCenterCode(year, month, user.getCenterCode());

        model.addAttribute("userCode", user.getUserCode());
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("classUnits", classUnits);
        model.addAttribute("grades", grades);
        model.addAttribute("days", DAYS);
        model.addAttribute("students", students);
        System.out.println("==================findTimeTavleWithStudents===================");
        List<TimeTableDTO> tables = classService.findTimeTableWithStudents(user.getUserCode(), year, month);

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
    public String getClassTimeView(@RequestParam("year") String year, @RequestParam("month") String month, Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<User> users = userService.findByCenterCode(user.getCenterCode());
        model.addAttribute("users", users);
        model.addAttribute("days", DAYS);

        List<TimeTableDTO> tables = classService.findTableViewWithStudents(year, month, user.getUserCode());

        Map<String, Map<String, TimeTableDTO>> tableMap = tables.stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO::getDayname,
                        Collectors.toMap(TimeTableDTO::getPeriodNo, Function.identity())
                ));
        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));
        model.addAttribute("tableMap", tableMap);

        // ───────────────────────────────
        // 🔹 현재 조회된 시간표 안의 학생들만 기준
        Map<String, TimeTableDTO.StudentDTO> uniqueStudents = tables.stream()
                .filter(t -> t.getStudents() != null)
                .flatMap(t -> t.getStudents().stream())
                .filter(s -> s.getStudentId() != null)
                .collect(Collectors.toMap(
                        TimeTableDTO.StudentDTO::getStudentId,
                        Function.identity(),
                        // 동일 학생이 여러 수업에 있을 경우 week 값이 더 작은 쪽으로 유지
                        (s1, s2) -> {
                            try {
                                int w1 = s1.getWeek() != null && s1.getWeek().matches("\\d+") ? Integer.parseInt(s1.getWeek()) : 99;
                                int w2 = s2.getWeek() != null && s2.getWeek().matches("\\d+") ? Integer.parseInt(s2.getWeek()) : 99;
                                return w1 <= w2 ? s1 : s2;
                            } catch (Exception e) {
                                return s1;
                            }
                        }
                ));

// 고유 학생 목록
        List<TimeTableDTO.StudentDTO> displayedStudents = new ArrayList<>(uniqueStudents.values());

// 총원 계산
        long total = displayedStudents.size();

// 1–3주 학생 필터링
        List<TimeTableDTO.StudentDTO> oneToThreeWeekStudents = displayedStudents.stream()
                .filter(s -> s.getWeek() != null && s.getWeek().matches("\\d+"))
                .filter(s -> {
                    int week = Integer.parseInt(s.getWeek());
                    return week >= 1 && week <= 3;
                })
                .collect(Collectors.toList());

        long oneToThreeWeeks = oneToThreeWeekStudents.size();

        String oneToThreeWeekNames = oneToThreeWeekStudents.stream()
                .map(s -> s.getStudentName() + "(" + s.getWeek() + "주)")
                .collect(Collectors.joining(", "));

        Map<String, Object> memberStats = new HashMap<>();
        memberStats.put("oneToThreeWeeks", oneToThreeWeeks);
        memberStats.put("oneToThreeWeekNames", oneToThreeWeekNames);
        memberStats.put("total", total);
        model.addAttribute("memberStats", memberStats);

        return "class/timeview";
    }

    @GetMapping("/print-timeview")
    public String getPrintTimeView(Model model, HttpSession session) {

//        List<User> users = userService.findByCenterNo(user.getCenterCode());
        List<User> users = userService.findByCenterCode("DAE001");

        model.addAttribute("users", users);
        model.addAttribute("days", DAYS);

        List<TimeTableDTO> tables = classService.findTableViewWithStudents("2025", "09", "DAE001cos");

        Map<String, Map<String, TimeTableDTO>> tableMap = tables.stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO::getDayname,
                        Collectors.toMap(
                                TimeTableDTO::getPeriodNo,
                                Function.identity())));
        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));
        model.addAttribute("tableMap", tableMap);
        return "print/print-timeview";
    }

    // 수업 일지 페이지
    @GetMapping("/record")
    public String getClassRecordPage(Model model, HttpSession session) {
        String today = dateConfig.currentYearMonth().get("today");
        String yy = dateConfig.currentYearMonth().get("currentYear");
        String mm = dateConfig.currentYearMonth().get("currentMonth");
        String dayName = dateConfig.currentYearMonth().get("currentDayName");

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<User> users = userService.findByCenterCode(user.getCenterCode());

        List<ClassRespDTO.RecordLabelDTO> labels = classService.getTimeTableByUserCode(yy, mm, dayName, user.getUserCode(), user.getCenterCode());

        if (!labels.isEmpty()) {
            String timeTableKey = labels.get(0).getTimeTableKey();
            String classKey = labels.get(0).getClassKey();
            String unitKey = labels.get(0).getUnitKey();
            ClassRespDTO.RecordBundleDTO bundle = classService.getTimeTableByKey(timeTableKey, "ju_1", classKey, unitKey);

            model.addAttribute("students", bundle.getStudents());

            if (bundle.getAfterClass() != null) {
                model.addAttribute("content", bundle.getAfterClass());
            } else {

                model.addAttribute("content", new ClassRespDTO.AfterClassRespDTO());
            }
        }

        model.addAttribute("users", users);
        model.addAttribute("labels", labels);

        return "class/record";
    }

    // 보강 페이지
    @GetMapping("/remedial")
    public String getClassRemedialPage(Model model, @RequestParam("year") String year, @RequestParam("month") String month) {

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

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO)
                session.getAttribute("user");

        if (user == null) {
            return "login";
        }

        // 센터별 선생님 목록
        List<User> users = userService.findByCenterCode(user.getCenterCode());

        // 클래스 리스트 가져오기
        List<TimeTableLabelDTO> labels = classService.getMonthlyClassList(
                "all",
                dateConfig.currentYearMonth().get("currentYear"),
                dateConfig.currentYearMonth().get("currentMonth"),
                dateConfig.currentYearMonth().get("currentDayName"));

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
