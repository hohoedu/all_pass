package com.hohoedu.all_pass._core.firebase;

import com.google.protobuf.Api;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

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
    public ResponseEntity<?> infant(@RequestBody FcmDTO.MultiFcmDTO fcmDTO) {

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }

        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
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