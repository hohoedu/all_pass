package com.hohoedu.all_pass.user;

import com.hohoedu.all_pass._core.utils.ApiUtils;
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

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
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
    public String loginUser(@ModelAttribute UserReqDTO.UserLoginDTO loginDTO, RedirectAttributes redirectAttributes) {

        try {
            LoginRespDTO dto = userService.login(loginDTO);
            session.setAttribute("user", dto);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    dto.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(authentication);

            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

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
        List<User> users = userService.findByCenterCode(user.getCenterCode());

        return ResponseEntity.ok(ApiUtils.success(users));

    }
}
