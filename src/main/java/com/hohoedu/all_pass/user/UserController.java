package com.hohoedu.all_pass.user;

import com.hohoedu.all_pass._core.utils.ApiUtils;
import com.hohoedu.all_pass.payment.PaymentService;
import com.hohoedu.all_pass.student.StudentService;
import com.hohoedu.all_pass.user._dto.UserRespDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import com.hohoedu.all_pass.user._dto.UserReqDTO;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;


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
    public String loginUser(@ModelAttribute UserReqDTO.UserLoginDTO loginDTO,
            @RequestParam(required = false) String redirectUrl, RedirectAttributes redirectAttributes) {

        try {
            LoginRespDTO dto = userService.login(loginDTO);
            List<User> userList = userService.findActiveUser(dto);
            session.setAttribute("user", dto);
            session.setAttribute("readableMenus", dto.getReadableMenus());
            session.setAttribute("userList", userList);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    dto.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);

            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

            if (redirectUrl != null && !redirectUrl.isEmpty()) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/login";

        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerTeacher(@RequestBody UserReqDTO.UserRegisterDTO dto, HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, "/login")
                    .build();
        }
        userService.registerTeacher(dto, user);
        return ResponseEntity.ok(ApiUtils.success(null));
    }

    @GetMapping("/logout")
    public String logout() {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(HttpSession session) {
        UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
        List<User> users = userService.findByCenterCode(user);

        return ResponseEntity.ok(ApiUtils.success(users));

    }

    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody UserReqDTO.PasswordChangeRequest request,
            HttpSession session) {
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
        return ResponseEntity.ok(ApiUtils.success("TODAY_TRANSFER_APPLIED"));
    }
}
