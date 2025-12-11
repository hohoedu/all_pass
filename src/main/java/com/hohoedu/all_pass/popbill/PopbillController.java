package com.hohoedu.all_pass.popbill;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.popbill._dto.PopbillReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notice")
public class PopbillController {

    private final PopbillService popbillSendService;

    @PostMapping("/send-join")
    public ResponseEntity<?> sendJoin(@RequestBody PopbillReqDTO.JoinNoticeRequest req, HttpSession session) {

        try {

            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(HttpStatus.FOUND) // 302 Redirect
                        .header(HttpHeaders.LOCATION, "/login")
                        .build();
            }
            // 세션에서 센터코드 가져오기 (이미 메뉴 페이지에서 사용 중)
            String centerCode = user.getCenterCode();

            // 템플릿코드: 알림톡 승인된 템플릿 코드 사용
            String templateCode = "022070000338";

            // 수신자명은 모르면 공백으로 처리
            String receiverName = "신규회원";

            String content = "[호호서당]\n회원 등록 안내 메시지입니다.";

            String receipt = popbillSendService.sendAts(
                    centerCode,
                    templateCode,
                    req.getPhone(),
                    "신규회원",
                    content
            );

            return ResponseEntity.ok(ApiUtils.success("hello"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ApiUtils.error(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }
}
