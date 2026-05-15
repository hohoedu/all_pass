package com.hohoedu.all_pass.center;

import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.student.StudentService;
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
        if (loginUser == null)
            return ResponseEntity.status(401).build();

        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());

        List<UserRespDTO.UserListRespDTO> users = userService.findAllBycenterCode(loginUser.getCenterCode());

        String targetUserCode, targetRoleKey, targetType;
        if ("all".equals(userCode)) {
            targetUserCode = loginUser.getUserCode();
            targetRoleKey = "ADMIN";
            targetType = loginUser.getType();
        } else {
            UserRespDTO.UserListRespDTO selectedUser = users.stream()
                    .filter(u -> u.getUserCode().equals(userCode))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
            targetUserCode = selectedUser.getUserCode();
            targetRoleKey = "TEACHER";
            targetType = selectedUser.getType();
        }

        Map<String, Object> result = new HashMap<>();

        result.put("classSummary", classService.getClassSummary(loginUser.getCenterCode(), targetUserCode, targetRoleKey));

        result.put("remedialSummary", classService.getRemedialSummary(loginUser.getCenterCode()));

        result.put("paymentSummary", paymentService.getPaymentSummaryByPeriod(loginUser.getCenterCode(), targetUserCode,
                targetRoleKey, targetType));

        result.put("allUnpaidSummary",
                paymentService.getPaymentSummary(loginUser.getCenterCode(), targetUserCode, targetRoleKey, targetType));

        result.put("absentSummary",
                classService.getAbsentSummary(loginUser.getCenterCode(), targetUserCode, targetRoleKey, year, month));

        result.put("studentStatus",
                studentService.getStudentStatus(loginUser.getCenterCode(), targetUserCode, targetRoleKey, year, month));

        return ResponseEntity.ok(result);
    }
}
