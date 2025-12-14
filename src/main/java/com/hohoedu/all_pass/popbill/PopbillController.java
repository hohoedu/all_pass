package com.hohoedu.all_pass.popbill;

import com.google.protobuf.Api;
import com.hohoedu.all_pass._core.handler.GlobalExceptionHandler;
import com.hohoedu.all_pass._core.utils.Aes256Util;
import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/popbill")
@RequiredArgsConstructor
public class PopbillController {

    private final PopbillService popbillService;

    @ResponseBody
    @PostMapping("/insert/config")
    public ResponseEntity<?> createPopbillConfig(@RequestBody PopbillReqDTO.PopbillInsertReqDTO dto) {

        try {

            popbillService.createPopbillConfig(dto);
            return ResponseEntity.ok(ApiUtils.success("입력 성공"));

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiUtils.error(e.getMessage(), HttpStatus.BAD_REQUEST));

        } catch (Exception e) {

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiUtils.error("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

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
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/send-join")
    public ResponseEntity<?> sendJoinAlimtalk(@RequestBody Map<String, String> request, HttpSession session) {
        try {
            // 1️⃣ 세션 체크
            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .header(HttpHeaders.LOCATION, "/login")
                        .build();
            }

            // 2️⃣ 전화번호 검증
            String phone = request.get("phone");
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiUtils.error("전화번호를 입력해주세요.", HttpStatus.BAD_REQUEST));
            }


            // 3️⃣ PopbillService에 위임 🔥
            String receiptNum = popbillService.sendJoinAlimtalk(
                    user.getCenterCode(),
                    phone,
                    user.getRegionName(),
                    user.getCenterName()
            );



            log.info("신규회원 알림톡 발송 성공 - centerCode: {}, phone: {}, receiptNum: {}",
                    user.getCenterCode(), phone, receiptNum);

            // 4️⃣ 성공 응답
            return ResponseEntity.ok(ApiUtils.success("알림톡이 발송되었습니다."));

        } catch (Exception e) {
            log.error("알림톡 발송 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiUtils.error("알림톡 발송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
