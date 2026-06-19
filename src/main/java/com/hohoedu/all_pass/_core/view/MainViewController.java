package com.hohoedu.all_pass._core.view;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.center.CenterService;
import com.hohoedu.all_pass.center._dto.CenterRespDTO.PointDTO;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassRespDTO;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.payment._dto.web.PaymentRespDTO;
import com.hohoedu.all_pass.payment.model.CardCode;
import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryLogisticsRepository;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student._dto.web.StudentWebRespDTO;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.PostMapping;
import org.threeten.bp.LocalDate;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MainViewController {

    private final ClassService classService;
    private final PaymentService paymentService;
    private final StudentService studentService;
    private final CenterService centerService;
    private final UserService userService;
    private final SecondaryLogisticsRepository testRepository;

    @GetMapping({"/", "/home"})
    public String getIndexPage(HttpSession session, Model model) {
        LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:https://highreader.co.kr";
        }
        String centerCode = user.getCenterCode();

        PointDTO point = new PointDTO();

        int popbillPoint = centerService.findPopbillPoint(centerCode);
        int paymintPoint = centerService.findPaymintPoint(centerCode);
        String paymintChargeUrl = centerService.findPaymintUrl(centerCode);

        point.setPopbillPoint(popbillPoint);
        point.setPaymintPoint(paymintPoint);
        point.setChargeUrl(paymintChargeUrl);

        session.setAttribute("point", point);

        return "index";
    }

    @GetMapping("/index")
    public String getIndex(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }
        return "index";
    }

    @GetMapping({"/admin", "/login"})
    public String getLoginPage() {
        return "login";
    }

    // 메인화면 열기
    @GetMapping("/main")
    public String getMainPage(HttpSession session, Model model) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());
        List<User> users = userService.findActiveUser(user);
        boolean isMeInList = users.stream()
                .anyMatch(u -> u.getUserCode().equals(user.getUserCode()));

        // 초기 조회 대상 결정
        String targetUserCode, targetRoleKey;
        if ("ADMIN".equals(user.getRoleKey()) && !isMeInList) {
            // ADMIN인데 목록에 없음 → 전체 조회
            targetUserCode = user.getUserCode();
            targetRoleKey = "ADMIN";
        } else {
            // 일반 유저이거나, ADMIN인데 목록에 본인 있음 → 본인만
            targetUserCode = user.getUserCode();
            targetRoleKey = "TEACHER";
        }
        // 수업정보
        List<ClassRespDTO.MainClassSummaryDTO> classSummary = classService.getClassSummary(user.getCenterCode(),
                targetUserCode, targetRoleKey);

        // 보강 정보
        List<ClassRespDTO.MainRemedialSummaryDTO> remedialSummary = classService
                .getRemedialSummary(user.getCenterCode());

        // 이번 달 미납 정보
        List<PaymentRespDTO.MainPaymentSummaryDTO> paymentSummary = paymentService
                .getPaymentSummaryByPeriod(user.getCenterCode(), targetUserCode, targetRoleKey, user.getType());

        // 전체 미납 정보
        List<PaymentRespDTO.MainPaymentSummaryDTO> allUnpaidSummary = paymentService
                .getPaymentSummary(user.getCenterCode(), targetUserCode, targetRoleKey, user.getType());

        // 결석 정보
        ClassRespDTO.MainAbsentSummaryDTO absentSummary = classService.getAbsentSummary(user.getCenterCode(),
                targetUserCode, targetRoleKey, year, month);

        // 학생 현황
        StudentWebRespDTO.MainStudentStatusDTO studentStatus = studentService.getStudentStatus(user.getCenterCode(),
                targetUserCode, targetRoleKey, year, month);

        model.addAttribute("user", user);
        model.addAttribute("users", users);
        model.addAttribute("isMeInList", isMeInList);
        model.addAttribute("classSummary", classSummary);
        model.addAttribute("remedialSummary", remedialSummary);
        model.addAttribute("paymentSummary", paymentSummary);
        model.addAttribute("allUnpaidSummary", allUnpaidSummary);
        model.addAttribute("absentSummary", absentSummary);
        model.addAttribute("studentStatus", studentStatus);
        model.addAttribute("currentYear", year);
        model.addAttribute("currentMonth", month);

        return "main";
    }

    // 주소 팝업창 열기
    @GetMapping("/juso")
    public String jusoPopup() {
        return "juso";
    }

    // 주소값 리턴
    @PostMapping("/juso")
    public String preventPostError(HttpServletRequest request, RedirectAttributes redirectAttributes) {

        redirectAttributes.addAttribute("roadFullAddr", request.getParameter("roadFullAddr"));
        redirectAttributes.addAttribute("roadAddrPart1", request.getParameter("roadAddrPart1"));
        redirectAttributes.addAttribute("roadAddrPart2", request.getParameter("roadAddrPart2"));
        redirectAttributes.addAttribute("addrDetail", request.getParameter("addrDetail"));

        return "redirect:/jusoCallBack";
    }

    // Callback
    @GetMapping("/jusoCallBack")
    public String jusoCallback(
            @RequestParam(name = "roadFullAddr", required = false) String roadFullAddr,
            @RequestParam(name = "roadAddrPart1", required = false) String roadAddrPart1,
            @RequestParam(name = "roadAddrPart2", required = false) String roadAddrPart2,
            @RequestParam(name = "addrDetail", required = false) String addrDetail,
            Model model) {

        String roadAddrPart = roadAddrPart1 + " " + roadAddrPart2;

        model.addAttribute("roadFullAddr", roadFullAddr);
        model.addAttribute("roadAddrPart", roadAddrPart);
        model.addAttribute("addrDetail", addrDetail);

        return "juso-callback";
    }

    @GetMapping("/school")
    public String getSchool() {
        return "school";
    }

    // @PostMapping("/eclass/redirect")
    // public void redirectToEClass(HttpSession session, HttpServletResponse
    // response) throws IOException {
    //
    // UserRespDTO.LoginRespDTO user =
    // (UserRespDTO.LoginRespDTO) session.getAttribute("user");
    //
    // if (user == null) {
    // response.sendRedirect("/login");
    // return;
    // }
    //
    // String userCode = user.getUserCode();
    // String centerCode = user.getCenterCode();
    //
    // String redirectUrl = "http://hohoseodang.com/eclass_center_erp.html"
    // + "?userCode=" + URLEncoder.encode(userCode, StandardCharsets.UTF_8)
    // + "&centerCode=" + URLEncoder.encode(centerCode, StandardCharsets.UTF_8);
    //
    // response.sendRedirect(redirectUrl);
    // }
    //
    // //https://hohoschool.com/erpbook/booksend.html
    //
    // @PostMapping("/clinic/redirect")
    // public void redirectToClinic(HttpSession session, HttpServletResponse
    // response) throws IOException {
    //
    // UserRespDTO.LoginRespDTO user =
    // (UserRespDTO.LoginRespDTO) session.getAttribute("user");
    //
    // if (user == null) {
    // response.sendRedirect("/login");
    // return;
    // }
    //
    // String userCode = user.getUserCode();
    // String centerCode = user.getCenterCode();
    //
    // String redirectUrl = "http://hohoseodang.com/eclass_center_erp.html"
    // + "?userCode=" + URLEncoder.encode(userCode, StandardCharsets.UTF_8)
    // + "&centerCode=" + URLEncoder.encode(centerCode, StandardCharsets.UTF_8);
    //
    // response.sendRedirect(redirectUrl);
    // }

    @GetMapping("/appid")
    public String getAppId(HttpSession session) {

        return "app_id_temp";
    }

    @GetMapping("/pay")
    public String getPaySearchPage(HttpSession session, HttpServletRequest request, Model model) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login?redirectUrl=" + request.getRequestURI();
        }
        List<CardCode> cardList = paymentService.findCardCode();
        model.addAttribute("cardList", cardList);

        return "pay/pay-search";
    }

    @GetMapping("/test/socket")
    @ResponseBody
    public String testSocket() {
        try (java.net.Socket socket = new java.net.Socket("192.168.0.28", 47158)) {
            return "소켓 연결 성공!";
        } catch (Exception e) {
            return "소켓 에러: " + e.getMessage();
        }
    }

    @GetMapping("/api/proxy/kindergarten")
    @ResponseBody
    public String getKindergarten(@RequestParam String sidoCode, @RequestParam String sggCode) {
        String url = "https://e-childschoolinfo.moe.go.kr/api/notice/basicInfo2.do?" +
                "key=687efc54181d4faaa1f37b15d5399b52&sidoCode=" + sidoCode + "&sggCode=" + sggCode +
                "&pageCnt=100&currentPage=1";

        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

}
