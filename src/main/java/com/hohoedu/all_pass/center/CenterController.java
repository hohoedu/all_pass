package com.hohoedu.all_pass.center;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.class_instance._dto.web.ClassReqDTO;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.secondary._dto.SecondaryDTO;
import com.hohoedu.all_pass.secondary.repository.SecondaryUserRepository;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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
    private final SecondaryUserRepository secondaryUserRepository;

    @GetMapping("/teachers")
    public ResponseEntity<?> getTeachersByCenterCode(@RequestParam(value = "centerCode") String centerCode) {
        if ("ULS001".equals(centerCode)) {
            return ResponseEntity.ok(secondaryUserRepository.findActiveTeachers());
        }
        List<User> teachers = userService.findActiveUserByCenterCode(centerCode);
        return ResponseEntity.ok(teachers);
    }

    @PostMapping("/week/get")
    public ResponseEntity<?> getWeek(@RequestBody ClassReqDTO.GetWeekDTO reqDTO) {
        return ResponseEntity.ok(ApiUtils.success(
                classService.getClassWeek(reqDTO.getYear(), reqDTO.getMonth(), reqDTO.getCenterCode())));
    }

    @PostMapping("/week/save")
    public ResponseEntity<?> saveWeek(@RequestBody ClassReqDTO.WeekReqDTO reqDTO) {
        try {
            classService.saveClassWeek(reqDTO, reqDTO.getCenterCode());
            return ResponseEntity.ok(ApiUtils.success("success"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));
        }
    }

    @GetMapping("/timetable")
    public ResponseEntity<?> getTimetable(
            @RequestParam(value = "centerCode") String centerCode,
            @RequestParam(value = "userCode") String userCode,
            @RequestParam(value = "year") String year,
            @RequestParam(value = "month") String month) {
        if ("ULS001".equals(centerCode)) {
            return ResponseEntity.ok(convertSecondaryTimetable(
                    secondaryUserRepository.findTimetable(userCode, year, month)));
        }
        return ResponseEntity.ok(classService.findTimeTableWithStudents(userCode, year, month));
    }

    private List<SecondaryDTO.TimetableDTO> convertSecondaryTimetable(
            List<SecondaryDTO.TimetableRawDTO> raws) {
        Map<Integer, String> dayMap = Map.of(2, "mon", 3, "tue", 4, "wed", 5, "thu", 6, "fri", 7, "sat");
        List<SecondaryDTO.TimetableDTO> result = new java.util.ArrayList<>();
        for (SecondaryDTO.TimetableRawDTO raw : raws) {
            SecondaryDTO.TimetableDTO dto = new SecondaryDTO.TimetableDTO();
            dto.setPeriodNo(raw.getTimelevel());
            dto.setDayname(dayMap.getOrDefault(raw.getDaynumber(), "mon"));
            dto.setStartTime(raw.getStime());
            dto.setEndTime(raw.getEtime());
            dto.setClassName(raw.getClassName());
            dto.setUnitName(raw.getUnitName());
            dto.setClassType("10".equals(raw.getGb()) ? "1" : "2");
            result.add(dto);
        }
        return result;
    }

    @GetMapping("/main/summary")
    public ResponseEntity<?> getMainSummary(@RequestParam(value = "userCode") String userCode, HttpSession session) {

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
