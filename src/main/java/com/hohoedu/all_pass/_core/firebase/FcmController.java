package com.hohoedu.all_pass._core.firebase;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/send")
    public ResponseEntity<?> send(@RequestBody FcmDTO fcmDTO) {
        if (fcmDTO.getTokens() == null || fcmDTO.getTokens().isEmpty()) {
            throw new IllegalArgumentException("토큰이 없습니다.");
        }

        for (String token : fcmDTO.getTokens()) {
            fcmService.sendMessage(token, fcmDTO.getTitle(), fcmDTO.getBody());
        }
        return ResponseEntity.ok(ApiUtils.success("발송 성공"));
    }
}