package com.hohoedu.all_pass._core.firebase;

import com.google.protobuf.Api;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.class_instance.ClassService;
import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;
    private final ClassService classService;

    @PostMapping("/attendance")
    public ResponseEntity<?> attendance(@RequestBody FcmDTO.SingleFcmDTO fcmDTO) {
        if (fcmDTO.getToken() == null || fcmDTO.getToken().isEmpty()) {
            throw new IllegalArgumentException("토큰이 없습니다.");
        }
        fcmService.sendMessage(fcmDTO.getToken(), fcmDTO.getTitle(), fcmDTO.getBody());
        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }

    @PostMapping("/before")
    public ResponseEntity<?> beforeClass(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        System.out.println(fcmDTO.getTokens().stream().toList());
        if (fcmDTO.getTokens() == null || fcmDTO.getTokens().isEmpty()) {
            throw new IllegalArgumentException("토큰이 없습니다.");
        }

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }
        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }

    @PostMapping("/after")
    public ResponseEntity<?> afterClass(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }

        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }

    @PostMapping("/monthly")
    public ResponseEntity<?> monthly(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }

        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }

    @PostMapping("/infant")
    public ResponseEntity<?> infant(@RequestBody FcmDTO.InfantFcmDTO fcmDTO, HttpSession session) {

        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiUtils.error("UNAUTHORIZED", HttpStatus.UNAUTHORIZED));
        }


        String userCode = user.getUserCode();
        String centerCode = user.getCenterCode();

        List<String> successStudentIds = new ArrayList<>();

        for (FcmDTO.InfantFcmDTO.StudentTokenDTO s : fcmDTO.getStudents()) {
            boolean ok = fcmService.sendMessage(
                    s.getToken(),
                    fcmDTO.getTitle(),
                    fcmDTO.getBody()
            );

            if (ok) {
                successStudentIds.add(s.getStudentId());
            }
        }

        classService.saveInfantSendHistory(
                fcmDTO.getClassType(),
                fcmDTO.getTimeTableKey(),
                userCode,
                centerCode,
                successStudentIds
        );

        return ResponseEntity.ok(ApiUtils.success("success"));
    }

    @PostMapping("/clinic")
    public ResponseEntity<?> clinic(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }

        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }

    @PostMapping("/guide")
    public ResponseEntity<?> guide(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }

        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }

    @PostMapping("/notice")
    public ResponseEntity<?> notice(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }

        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }
}