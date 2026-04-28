package com.hohoedu.all_pass.user;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.hohoedu.all_pass.center.Center;
import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;
import com.hohoedu.all_pass.user.model.UserRoleCode;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PaymentService paymentService;
    private final StudentService studentService;
    private final HttpSession session;

    @GetMapping("/ping")
    @ResponseBody
    public ResponseEntity<?> ping(HttpSession session) {
        Object user = session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute UserReqDTO.UserLoginDTO loginDTO, @RequestParam(required = false) String redirectUrl, RedirectAttributes redirectAttributes) {

        try {
            LoginRespDTO dto = userService.login(loginDTO);
            session.setAttribute("user", dto);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    dto.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);

            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            log.info(redirectUrl);
//            paymentService.destroyExpiredBills();
            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";

        }
    }

    @ResponseBody
    @PostMapping("/join")
    public void joinUser() {
        User user = User.builder()
                .userCode("love")
                .passwordHash("4321")
                .userName("러브")
                .role(UserRoleCode.builder().roleKey("ULS001love").build())
                .center(Center.builder().centerCode("ULS001").build())
                .build();

        userService.insert(user);

    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate(); // 세션 완전 제거
        return "redirect:/login";
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        List<User> users = userService.findByCenterCode(user);

        return ResponseEntity.ok(ApiUtils.success(users));

    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody UserReqDTO.PasswordChangeRequest request, HttpSession session) {
        try {
            LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");

            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "로그인이 필요합니다."));
            }
            request.setUserCode(user.getUserCode());
            userService.changePassword(request);

            return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 변경되었습니다."));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "비밀번호 변경에 실패했습니다."));
        }
    }

    @PostMapping("/apply-today")
    public ResponseEntity<?> applyTodayTransfers(@RequestHeader("X-CRON-TOKEN") String token) {
        token.equals("hohoedu");
        log.info("token = {}", token);
        log.info(LocalDate.now().toString());
        studentService.applyTodayTransfers(LocalDate.now());
        paymentService.paymentBillStatusChange();
        return ResponseEntity.ok(ApiUtils.success("TODAY_TRANSFER_APPLIED")
        );
    }
}
