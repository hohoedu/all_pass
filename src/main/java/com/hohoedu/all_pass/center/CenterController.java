package com.hohoedu.all_pass.center;

import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.threeten.bp.LocalDate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/center")
public class CenterController {

    private final ClassService classService;
    private final PaymentService paymentService;
    private final StudentService studentService;
    private final UserService userService;

    @GetMapping("/main/summary")
    public ResponseEntity<?> getMainSummary(@RequestParam String userCode, HttpSession session) {

        UserRespDTO.LoginRespDTO loginUser = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (loginUser == null) return ResponseEntity.status(401).build();

        String year  = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());

        List<UserRespDTO.UserListRespDTO> users = userService.findAllBycenterCode(loginUser.getCenterCode());

        UserRespDTO.UserListRespDTO selectedUser = users.stream()
                .filter(u -> u.getUserCode().equals(userCode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        String targetUserCode, targetRoleKey, targetType;
        if ("all".equals(userCode)) {
            targetUserCode = loginUser.getUserCode();
            targetRoleKey  = "ADMIN";
            targetType     = loginUser.getType();
        } else {
            targetUserCode = userCode;
            targetRoleKey  = selectedUser.getRoleKey();
            targetType     = selectedUser.getType();
        }

        long total = System.currentTimeMillis();
        long t;

        Map<String, Object> result = new HashMap<>();

        t = System.currentTimeMillis();
        result.put("classSummary", classService.getClassSummary(loginUser.getCenterCode(), targetUserCode));
        log.info("[PERF] classSummary: {}ms", System.currentTimeMillis() - t);

        t = System.currentTimeMillis();
        result.put("remedialSummary", classService.getRemedialSummary(loginUser.getCenterCode()));
        log.info("[PERF] remedialSummary: {}ms", System.currentTimeMillis() - t);

        t = System.currentTimeMillis();
        result.put("paymentSummary", paymentService.getPaymentSummaryByPeriod(loginUser.getCenterCode(), targetUserCode, targetRoleKey, targetType));
        log.info("[PERF] paymentSummary: {}ms", System.currentTimeMillis() - t);

        t = System.currentTimeMillis();
        result.put("allUnpaidSummary", paymentService.getPaymentSummary(loginUser.getCenterCode(), targetUserCode, targetRoleKey, targetType));
        log.info("[PERF] allUnpaidSummary: {}ms", System.currentTimeMillis() - t);

        t = System.currentTimeMillis();
        result.put("absentSummary", classService.getAbsentSummary(loginUser.getCenterCode(), targetUserCode, targetRoleKey, year, month));
        log.info("[PERF] absentSummary: {}ms", System.currentTimeMillis() - t);

        t = System.currentTimeMillis();
        result.put("studentStatus", studentService.getStudentStatus(loginUser.getCenterCode(), targetUserCode, targetRoleKey, year, month));
        log.info("[PERF] studentStatus: {}ms", System.currentTimeMillis() - t);

        log.info("[PERF] ===== TOTAL: {}ms =====", System.currentTimeMillis() - total);

        return ResponseEntity.ok(result);
    }
}
