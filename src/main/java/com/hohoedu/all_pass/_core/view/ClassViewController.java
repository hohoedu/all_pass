package com.hohoedu.all_pass._core.view;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.extern.slf4j.Slf4j;
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
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.threeten.bp.LocalDate;

import static com.hohoedu.all_pass._core.vo.Constants.DAYS;

@Slf4j
@Controller
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassViewController {

    private final UserService userService;
    private final ClassService classService;
    private final StudentService studentService;

    // 시간표 등록
    @GetMapping("/timetable")
    public String getClassTimetable(@RequestParam("year") String year, @RequestParam("month") String month, Model model,
            HttpSession session) throws JsonProcessingException {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<ClassCode> classCodes = classService.findClassCode();
        List<GradeCode> grades = classService.findGrade();
        Map<String, List<ClassRespDTO.ClassUnitDTO>> classUnitMap = classService.findClassUnits(user.getCenterCode(),
                year, month);
        ObjectMapper mapper = new ObjectMapper();
        String classUnits = mapper.writeValueAsString(classUnitMap);
        String classCodesJson = mapper.writeValueAsString(classCodes);

        List<StudentWebRespDTO.StudentsListDTO> students = studentService.findStudentByCenterCode(year, month,
                user.getCenterCode(), user.getUserCode());

        List<ClassRespDTO.ComClassStudentDTO> comclassInfos = classService
                .findComClassStudentsByUserCode(user.getUserCode(), year, month);

        String comclassInfosJson = mapper.writeValueAsString(comclassInfos);

        model.addAttribute("comclassInfosJson", comclassInfosJson);
        model.addAttribute("userCode", user.getUserCode());
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("classCodesJson", classCodesJson);
        model.addAttribute("classUnits", classUnits);
        model.addAttribute("grades", grades);
        model.addAttribute("days", DAYS);
        model.addAttribute("students", students);

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
    public String getClassTimeView(@RequestParam("year") String year, @RequestParam("month") String month, Model model,
            HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<User> users = userService.findActiveUser(user);
        model.addAttribute("users", users);
        model.addAttribute("days", DAYS);

        ClassRespDTO.TimeTableViewRespDTO viewData = classService.findTableViewWithStudents(year, month,
                user.getUserCode(), user.getCenterCode());

        Map<String, Map<String, TimeTableDTO>> tableMap = viewData.getTables().stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO::getDayname,
                        Collectors.toMap(TimeTableDTO::getPeriodNo, Function.identity())));
        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));

        Map<String, List<ClassRespDTO.StudentStatRespDTO>> statsMap = viewData.getStats().stream()
                .collect(Collectors.groupingBy(ClassRespDTO.StudentStatRespDTO::getGb));

        model.addAttribute("user", user);
        model.addAttribute("tableMap", tableMap);
        model.addAttribute("statsMap", statsMap);
        model.addAttribute("totalStudentsLong", viewData.getTotalStudentsLong());
        model.addAttribute("totalStudentsDouble", viewData.getTotalStudentsDouble());
        model.addAttribute("selectedUserCode", user.getUserCode());

        return "class/timeview";
    }

    @GetMapping("/print-timeview")
    public String getPrintTimeView(@RequestParam String ym, @RequestParam String userCode, Model model,
            HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        String yy = ym.substring(0, 4);
        String mm = ym.substring(4, 6);
        // 센터 선생님 목록
        List<User> users = userService.findAllUserCode(user);
        // List<User> users = userService.findByCenterCodeDev();

        // 선택된 선생님
        User selectedUser = users.stream()
                .filter(u -> userCode.equals(u.getUserCode()))
                .findFirst()
                .orElse(null);

        // 시간표 조회
        ClassRespDTO.TimeTableViewRespDTO viewData = classService.findTableViewWithStudents(yy, mm, userCode,
                user.getCenterCode());

        Map<String, Map<String, TimeTableDTO>> tableMap = viewData.getTables().stream()
                .collect(Collectors.groupingBy(
                        TimeTableDTO::getDayname,
                        Collectors.toMap(
                                TimeTableDTO::getPeriodNo,
                                Function.identity())));

        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));
        Map<String, List<ClassRespDTO.StudentStatRespDTO>> statsMap = viewData.getStats().stream()
                .collect(Collectors.groupingBy(ClassRespDTO.StudentStatRespDTO::getGb));
        model.addAttribute("yy", yy);
        model.addAttribute("mm", mm);
        model.addAttribute("users", users);
        model.addAttribute("selectedUserCode", userCode);
        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("days", DAYS);
        model.addAttribute("tableMap", tableMap);
        model.addAttribute("statsMap", statsMap);
        model.addAttribute("totalStudentsLong", viewData.getTotalStudentsLong());
        model.addAttribute("totalStudentsDouble", viewData.getTotalStudentsDouble());

        return "print/print-timeview";
    }

    // 수업 일지 페이지
    @GetMapping("/record")
    public String getClassRecordPage(Model model, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        LocalDate today = LocalDate.now();
        String currentDate = today.toString();
        String dayName = DateConfig.currentYearMonth().get("currentDayName");

        // ✅ 1. 주차 계산 먼저
        String yy = String.valueOf(today.getYear());
        String mm = String.format("%02d", today.getMonthValue());
        List<ClassRespDTO.ClassWeekDTO> weeks = classService.getClassWeek(yy, mm, user.getCenterCode());
        String activeWeek = findWeekByDate(
                weeks,
                today,
                date -> classService.getClassWeek(
                        String.valueOf(date.getYear()),
                        String.format("%02d", date.getMonthValue()),
                        user.getCenterCode()));
        if (activeWeek == null)
            activeWeek = "ju_1";

        // ✅ 2. 주차가 전월에서 찾아진 경우 → 전월 기준으로 yy, mm 교체
        boolean isCurrentMonthMatch = weeks.stream()
                .anyMatch(w -> isDateMatch(w.getMon(), today) || isDateMatch(w.getTue(), today) ||
                        isDateMatch(w.getWed(), today) || isDateMatch(w.getThu(), today) ||
                        isDateMatch(w.getFri(), today) || isDateMatch(w.getSat(), today) ||
                        isDateMatch(w.getSun(), today));

        if (!isCurrentMonthMatch) {
            // 전월로 기준 변경
            LocalDate prevMonth = today.minusMonths(1);
            yy = String.valueOf(prevMonth.getYear());
            mm = String.format("%02d", prevMonth.getMonthValue());
            log.info("[페이지로딩] 전월 기준으로 변경: {}-{}", yy, mm);
        }

        List<User> users = userService.findActiveUser(user);
        List<ClassRespDTO.RecordLabelDTO> labels = classService.getTimeTableByUserCode(
                currentDate, dayName, user.getUserCode(), user.getCenterCode());

        if (!labels.isEmpty()) {
            String timeTableKey = labels.get(0).getTimeTableKey();
            String classKey = labels.get(0).getClassKey();
            String unitKey = labels.get(0).getUnitKey();

            ClassRespDTO.RecordBundleDTO bundle = classService.getTimeTableByKey(
                    user.getUserCode(), timeTableKey, currentDate, classKey, unitKey, user.getCenterCode());

            model.addAttribute("students", bundle.getStudents());
            model.addAttribute("content", bundle.getAfterClass() != null
                    ? bundle.getAfterClass()
                    : new ClassRespDTO.AfterClassRespDTO());

            if (bundle.getWeek() != null)
                activeWeek = bundle.getWeek();
        } else {
            model.addAttribute("students", Collections.emptyList());
            model.addAttribute("content", new ClassRespDTO.AfterClassRespDTO());
        }
        List<String> unsentStudentIds = classService.findUnsentAfterNotification(user.getUserCode(), user.getCenterCode());
        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("labels", labels);
        model.addAttribute("activeWeek", activeWeek);

        model.addAttribute("unsentStudentIds", unsentStudentIds);
        return "class/record";
    }

    private String findWeekByDate(List<ClassRespDTO.ClassWeekDTO> weeks, LocalDate targetDate,
            Function<LocalDate, List<ClassRespDTO.ClassWeekDTO>> weeksFetcher) {
        // 현재 월에서 먼저 탐색
        Optional<String> result = findWeekInList(weeks, targetDate);
        if (result.isPresent()) {
            return result.get();
        }

        // 매칭 없거나 비어있으면 → 전월로 재조회
        LocalDate prevMonthDate = targetDate.minusMonths(1);
        List<ClassRespDTO.ClassWeekDTO> prevWeeks = weeksFetcher.apply(prevMonthDate);

        return findWeekInList(prevWeeks, targetDate)
                .orElse(prevWeeks.isEmpty() ? "ju_1" : prevWeeks.get(0).getWeek());
    }

    private Optional<String> findWeekInList(List<ClassRespDTO.ClassWeekDTO> weeks, LocalDate targetDate) {
        return weeks.stream()
                .filter(week -> isDateMatch(week.getMon(), targetDate) ||
                        isDateMatch(week.getTue(), targetDate) ||
                        isDateMatch(week.getWed(), targetDate) ||
                        isDateMatch(week.getThu(), targetDate) ||
                        isDateMatch(week.getFri(), targetDate) ||
                        isDateMatch(week.getSat(), targetDate) ||
                        isDateMatch(week.getSun(), targetDate))
                .map(ClassRespDTO.ClassWeekDTO::getWeek)
                .findFirst();
    }

    // 날짜 비교 헬퍼 메서드
    private boolean isDateMatch(Object dateObj, LocalDate targetDate) {
        if (dateObj == null)
            return false;

        if (dateObj instanceof LocalDate) {
            return targetDate.equals(dateObj);
        }

        if (dateObj instanceof String) {
            String dateStr = (String) dateObj;
            if (dateStr == null || dateStr.trim().isEmpty())
                return false;

            try {
                LocalDate date = LocalDate.parse(dateStr);
                return targetDate.equals(date);
            } catch (Exception e) {
                return false;
            }
        }

        return false;
    }

    // 보강 페이지
    @GetMapping("/remedial")
    public String getClassRemedialPage(Model model, @RequestParam("year") String year,
            @RequestParam("month") String month, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<User> users = userService.findActiveUser(user);

        List<RemedialDTO> remedials = classService.findRemedialByUserNo(year, month, user.getUserCode());
        List<RemedialDTO> rightRemedials = remedials.stream()
                .filter(RemedialDTO::isAction)
                .toList();

        List<RemedialDTO> leftRemedials = remedials.stream()
                .filter(r -> !r.isAction())
                .toList();

        List<ClassRespDTO.AbsentFlagDTO> absentFlags = classService.findAbsentFlags(year, month, user.getUserCode());

        model.addAttribute("rightRemedials", rightRemedials);
        model.addAttribute("leftRemedials", leftRemedials);
        model.addAttribute("absentFlags", absentFlags);
        model.addAttribute("users", users);
        return "class/remedial";
    }

    // 월간평가 (초등)
    @GetMapping("/monthly")
    public String getClassMonthlyPage(HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "login";
        }

        log.info(user.getUserName());
        // 센터별 선생님 목록
        List<User> users = userService.findActiveUser(user);

        // 클래스 리스트 가져오기
        List<TimeTableLabelDTO> labels = classService.getMonthlyClassList(
                user.getUserCode(),
                DateConfig.currentYearMonth().get("currentYear"),
                DateConfig.currentYearMonth().get("currentMonth"),
                DateConfig.currentYearMonth().get("currentDayName"),
                user.getCenterCode());
        // 첫번째 수업에 대한 학생 목록 가져오기
        if (!labels.isEmpty()) {
            List<MonthlyStudentDTO> students = classService.getMonthlyClassDetail(labels.get(0).getTimeTableKey());
            log.info(students.toString());
            model.addAttribute("students", students);
        }

        model.addAttribute("users", users);
        model.addAttribute("labels", labels);
        return "class/monthly";
    }

    @GetMapping("/infant")
    public String getMonthlyInfantPage(HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "login";
        }

        String yy = DateConfig.currentYearMonth().get("currentYear");
        String mm = DateConfig.currentYearMonth().get("currentMonth");

        List<User> users = userService.findActiveUser(user);
        model.addAttribute("users", users);

        ClassReqDTO.InfantClassLabelsDTO classInfantDTO = new ClassReqDTO.InfantClassLabelsDTO();
        classInfantDTO.setUserCode(user.getUserCode());
        classInfantDTO.setYy(yy);
        classInfantDTO.setMm(mm);

        List<TimeTableLabelDTO> labels;

        try {
            labels = classService.findInfantClassLabel(classInfantDTO);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "수업 정보를 불러오는 중 오류가 발생했습니다.");
            return "class/infant";
        }

        model.addAttribute("labels", labels);

        if (labels == null || labels.isEmpty()) {
            model.addAttribute("noData", true);
            return "class/infant";
        }

        TimeTableLabelDTO label = labels.get(0);
        label.setYy(yy);
        String classKey = label.getClassKey();

        Set<String> hanKeys = Set.of("Y", "P", "S");

        Set<String> bookKeys = Set.of("K", "M", "J");

        try {
            if (hanKeys.contains(classKey)) {
                ClassRespDTO.InfantHanDTO infantHanDTO = classService.findInfantHan(label);
                infantHanDTO.setClassLabel(label.getClassSubject());
                model.addAttribute("infantHanDTO", infantHanDTO);
            }

            if (bookKeys.contains(classKey)) {
                ClassRespDTO.InfantBookDTO infantBookDTO = classService.findInfantBook(label);
                infantBookDTO.setClassLabel(label.getClassSubject());
                model.addAttribute("infantBookDTO", infantBookDTO);
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "수업 세부 정보를 불러오는 중 오류가 발생했습니다.");
        }

        return "class/infant";
    }

    @GetMapping("/edu-timeview")
    public String getEduTimeView(HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        List<User> users = userService.findActiveUser(user);
        model.addAttribute("users", users);
        model.addAttribute("currentYy", DateConfig.currentYearMonth().get("currentYear"));
        model.addAttribute("currentMm", DateConfig.currentYearMonth().get("currentMonth"));

        return "class/edu-timeview";
    }

    @GetMapping("/edu-print-timeview")
    public String getEduPrintTimeView(
            @RequestParam String userCode,
            @RequestParam String yy,
            @RequestParam String mm,
            HttpSession session, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null)
            return "redirect:/login";

        List<ClassRespDTO.TimeTableDTO> tables = classService.findEduTableViewWithStudents(
                yy, mm, userCode, user.getCenterCode());

        Map<String, Map<String, ClassRespDTO.TimeTableDTO>> tableMap = tables.stream()
                .collect(Collectors.groupingBy(
                        ClassRespDTO.TimeTableDTO::getDayname,
                        Collectors.toMap(ClassRespDTO.TimeTableDTO::getPeriodNo, Function.identity())));
        DAYS.forEach(d -> tableMap.putIfAbsent(d.get("id"), new HashMap<>()));

        User selectedUser = userService.findActiveUser(user).stream()
                .filter(u -> u.getUserCode().equals(userCode))
                .findFirst()
                .orElse(null);

        model.addAttribute("selectedUser", selectedUser);
        model.addAttribute("tableMap", tableMap);
        model.addAttribute("days", DAYS);
        model.addAttribute("yy", yy);
        model.addAttribute("mm", mm);

        return "print/print-edu-timeview";
    }

}
