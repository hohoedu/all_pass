package com.hohoedu.all_pass.popbill;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.consult.ConsultService;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.student.model.PendingStudent;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import com.popbill.api.PopbillException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/popbill")
@RequiredArgsConstructor
public class PopbillController {

    private final PopbillService popbillService;
    private final StudentService studentService;
    private final ConsultService consultService;

    @ResponseBody
    @PostMapping("/insert/config")
    public ResponseEntity<?> createPopbillConfig(@RequestBody PopbillReqDTO.PopbillInsertReqDTO dto) {

        try {

            popbillService.createPopbillConfig(dto);

            return ResponseEntity.ok(ApiUtils.success("입력 성공"));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiUtils.error("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    // 팝빌 URL 접근
    @GetMapping("/access-url")
    public ResponseEntity<?> getAccessURL(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {
            String url = popbillService.getPopbillAccessURL(user.getCenterCode());

            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, url)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-join")
    public ResponseEntity<?> sendJoinAlimtalk(@RequestBody PopbillReqDTO.SendJoinReqDTO request, HttpSession session) {
        try {
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/login")
                        .build();
            }

            PopbillReqDTO.InviteTrackingReqDTO inviteDTO = popbillService.sendJoinAlimtalk(
                    user.getCenterCode(),
                    request.getPhone(),
                    user.getRegionName(),
                    user.getCenterName(),
                    user.getUserCode());

            PendingStudent pendingStudent = PendingStudent.builder()
                    .name(request.getName())
                    .phone(request.getPhone())
                    .sendKey(inviteDTO.getSendKey())
                    .inviteCode(inviteDTO.getInviteCode())
                    .gradeKey(request.getGradeKey())
                    .userCode(user.getUserCode())
                    .centerCode(user.getCenterCode())
                    .subHoho(Boolean.TRUE.equals(request.getSubHoho()))
                    .subHan(Boolean.TRUE.equals(request.getSubHan()))
                    .subBook(Boolean.TRUE.equals(request.getSubBook()))
                    .status("LINK_SENT")
                    .isDeleted(false)
                    .build();

            studentService.createPendingStudent(pendingStudent);

            consultService.updateSendKey(request.getConsultId(), inviteDTO.getSendKey());

            return ResponseEntity.ok(ApiUtils.success("알림톡이 발송되었습니다."));

        } catch (Exception e) {
            log.error("알림톡 발송 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiUtils.error("알림톡 발송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @PostMapping("/remind/send")
    public ResponseEntity<?> sendRemind(@RequestBody PopbillReqDTO.RemindReqDTO request,
            HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {
            int successCount = popbillService.sendRemindTalk(user.getCenterCode(), request);

            return ResponseEntity.ok(ApiUtils.success(
                    String.format("알림톡 발송이 완료되었습니다. (%d건)", successCount)));

        } catch (Exception e) {
            log.error("알림톡 발송 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiUtils.error("알림톡 발송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @GetMapping("/point")
    public ResponseEntity<?> getPopbillPoint(@RequestParam(value = "param") String param) {

        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @GetMapping("/charge-url")
    public ResponseEntity<?> getChargeUrl(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }

        try {
            String url = popbillService.getChargeUrl(user.getCenterCode());
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("url", url);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, url)
                    .build();
        } catch (PopbillException e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

}
