package com.hohoedu.all_pass._core.firebase;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/push")
@RequiredArgsConstructor
public class FcmController {

    private final FcmService fcmService;

    @PostMapping("/send")
    public String send(@RequestBody FcmDTO fcmDTO) {
        fcmService.sendMessage(
                fcmDTO.getToken(),
                fcmDTO.getTitle(),
                fcmDTO.getBody()
        );
        return "Push Sent!";
    }
}