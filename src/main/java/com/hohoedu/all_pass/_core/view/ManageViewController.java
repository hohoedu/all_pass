package com.hohoedu.all_pass._core.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.hohoedu.all_pass._core.config.DateConfig;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.class_instance.model.ClassCode;
import com.hohoedu.all_pass.class_instance.model.UnitCode;
import com.hohoedu.all_pass.manage.ManageService;
import com.hohoedu.all_pass.manage._dto.ManageRespDTO;
import com.hohoedu.all_pass.notice.NoticeService;
import com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/manage")
@RequiredArgsConstructor
public class ManageViewController {

    private final ClassService classService;
    private final NoticeService noticeService;
    private final ManageService manageService;
    private final UserService userService;

    @GetMapping("/order")
    public String getManageOrderPage(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String month,
            HttpSession session,
            Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String userCode = user.getUserCode();
        String centerCode = user.getCenterCode();

        // URL 파라미터가 없으면 다음달로 설정
        if (year == null || month == null) {
            LocalDate now = LocalDate.now();
            LocalDate nextMonth = now.plusMonths(1);
            year = String.valueOf(nextMonth.getYear());
            month = nextMonth.format(DateTimeFormatter.ofPattern("MM"));
        }

        // 파라미터로 받은 년월이 이미 다음달이므로 그대로 사용
        List<ManageRespDTO.BasicOrderListDTO> baseOrderList = manageService.getBasicOrderList(
                userCode,
                centerCode,
                year,
                month);

        List<ManageRespDTO.SavedOrderListDTO> savedOrderList = manageService.getSavedOrderList(
                userCode,
                centerCode,
                year,
                month);

        model.addAttribute("baseOrderList", baseOrderList);
        model.addAttribute("savedOrderList", savedOrderList);
        model.addAttribute("year", year);
        model.addAttribute("month", month);

        return "manage/order";
    }

    @GetMapping("/reorder")
    public String getManageReorderPage(Model model, HttpSession session) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String userCode = user.getUserCode();
        String cneterCode = user.getCenterCode();
        LocalDate now = LocalDate.now(); // 이번 달
        LocalDate prev = now.minusMonths(1); // 지난 달

        String startYear = String.valueOf(prev.getYear());
        String startMonth = String.format("%02d", prev.getMonthValue());

        String endYear = String.valueOf(now.getYear());
        String endMonth = String.format("%02d", now.getMonthValue());

        List<ClassCode> classCodes = classService.findClassCode();
        Map<String, List<ClassRespDTO.ClassUnitDTO>> classUnitMap = classService.findClassUnitsOverPeriod(cneterCode,
                startYear, startMonth, endYear, endMonth);
        ObjectMapper mapper = new ObjectMapper();
        String classUnits = mapper.writeValueAsString(classUnitMap);
        String classCodesJson = mapper.writeValueAsString(classCodes);

        List<ManageRespDTO.ReorderListDTO> reorderList = manageService.getReorderList(userCode, cneterCode, endYear,
                endMonth);

        model.addAttribute("reorderList", reorderList);
        model.addAttribute("classCodes", classCodes);
        model.addAttribute("classCodesJson", classCodesJson);
        model.addAttribute("classUnits", classUnits);

        return "manage/reorder";
    }

    @GetMapping("/sms")
    public String getManageSmsPage(HttpSession session, Model model) throws JsonProcessingException {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        List<NoticeRespDTO.CenterNoticeDTO> noticeList = noticeService.findCenterNoticeByCenterCode(user);
        List<NoticeRespDTO.NoticeStudentDTO> studentList = noticeService.findStudentByUserCode(user);

        model.addAttribute("noticeList", noticeList);
        model.addAttribute("studentList", studentList);

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        if (!noticeList.isEmpty()) {
            model.addAttribute("firstNotice", noticeList.get(0));
        }

        return "manage/sms";
    }

    @GetMapping("/teacher")
    public String getManageTeacherPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<UserRespDTO.UserListRespDTO> users = userService.findAllBycenterCode(user.getCenterCode());
        List<UserRespDTO.MenuListDTO> menus = userService.findMenus();

        model.addAttribute("users", users);
        model.addAttribute("menus", menus);

        log.info("userModel = {}", model);
        log.info("menuModel = {}", menus);
        return "manage/teacher";
    }

    @GetMapping("tuition")
    public String getManageTuitionPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        ManageRespDTO.TuitionRespDTO data = manageService.getTuitionData(user.getCenterCode());

        model.addAttribute("hanClasses", data.getHanClasses());
        model.addAttribute("bookClasses", data.getBookClasses());
        model.addAttribute("hohoClasses", data.getHohoClasses());
        model.addAttribute("hanFeeMap", data.getHanFeeMap());
        model.addAttribute("bookFeeMap", data.getBookFeeMap());
        model.addAttribute("hohoFeeMap", data.getHohoFeeMap());
        model.addAttribute("centerCode", user.getCenterCode());

        return "manage/tuition";
    }

    @GetMapping("notice")
    public String getManageNoticePage() {
        return "manage/notice";
    }
}
