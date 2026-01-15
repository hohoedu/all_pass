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
import com.hohoedu.all_pass.notice.CenterNotice;
import com.hohoedu.all_pass.notice.NoticeService;
import com.hohoedu.all_pass.notice._dto.web.NoticeRespDTO;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.student.model.GradeCode;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.threeten.bp.LocalDate;

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
    private final DateConfig dateConfig;

    @GetMapping("/order")
    public String getManageOrderPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String userCode = user.getUserCode();
        String cneterCode = user.getCenterCode();
        String year = dateConfig.currentYearMonth().get("currentYear");
        String month = dateConfig.currentYearMonth().get("currentMonth");

        List<ManageRespDTO.BasicOrderListDTO> baseOrderList = manageService.getBasicOrderList(userCode, cneterCode, year, month);
        List<ManageRespDTO.SavedOrderListDTO> savedOrderList = manageService.getSavedOrderList(userCode, cneterCode, year, month);

        model.addAttribute("baseOrderList", baseOrderList);
        model.addAttribute("savedOrderList", savedOrderList);
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
        LocalDate now = LocalDate.now();            // 이번 달
        LocalDate prev = now.minusMonths(1);        // 지난 달

        String startYear = String.valueOf(prev.getYear());
        String startMonth = String.format("%02d", prev.getMonthValue());

        String endYear = String.valueOf(now.getYear());
        String endMonth = String.format("%02d", now.getMonthValue());

        List<ClassCode> classCodes = classService.findClassCode();
        Map<String, List<ClassRespDTO.ClassUnitDTO>> classUnitMap = classService.findClassUnitsOverPeriod(cneterCode, startYear, startMonth, endYear, endMonth);
        ObjectMapper mapper = new ObjectMapper();
        String classUnits = mapper.writeValueAsString(classUnitMap);
        String classCodesJson = mapper.writeValueAsString(classCodes);

        List<ManageRespDTO.ReorderListDTO> reorderList = manageService.getReorderList(userCode, cneterCode, endYear, endMonth);

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
    public String getManageTeacherPage(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "manage/teacher";
    }

    @GetMapping("tuition")
    public String getManageTuitionPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        List<ClassCode> classCodes = classService.findClassCode();
        List<UnitCode> hanLevelCode = classService.findHanLevelCode();
        List<ClassCode> hanClasses = classCodes.stream()
                .filter(c -> "1".equals(c.getClassType()))
                .toList();

        List<ClassCode> bookClasses = classCodes.stream()
                .filter(c -> "2".equals(c.getClassType()))
                .toList();

        List<PaymentRespDTO.ClassFeeMapDTO> feeMaps = manageService.findClassFeeMapByCenterCode(user.getCenterCode());

        Map<String, String> bookFeeMap = feeMaps.stream()
                .filter(f -> "2".equals(f.getClassType()))
                .collect(Collectors.toMap(PaymentRespDTO.ClassFeeMapDTO::getClassKey, PaymentRespDTO.ClassFeeMapDTO::getFee));

        Map<String, String> hanFeeMap = feeMaps.stream()
                .filter(f -> "1".equals(f.getClassType()))
                .collect(Collectors.toMap(PaymentRespDTO.ClassFeeMapDTO::getClassKey, PaymentRespDTO.ClassFeeMapDTO::getFee));

        Map<String, String> levelFeeMap = feeMaps.stream()
                .filter(f -> "HL".equals(f.getClassKey()))
                .collect(Collectors.toMap(PaymentRespDTO.ClassFeeMapDTO::getUnitKey, PaymentRespDTO.ClassFeeMapDTO::getFee));

        model.addAttribute("hanFeeMap", hanFeeMap);
        model.addAttribute("bookFeeMap", bookFeeMap);
        model.addAttribute("levelFeeMap", levelFeeMap);
        model.addAttribute("hanClasses", hanClasses);
        model.addAttribute("bookClasses", bookClasses);
        model.addAttribute("hanLevelCode", hanLevelCode);
        model.addAttribute("centerCode", user.getCenterCode());

        return "manage/tuition";
    }

    @GetMapping("notice")
    public String getManageNoticePage() {
        return "manage/notice";
    }
}
