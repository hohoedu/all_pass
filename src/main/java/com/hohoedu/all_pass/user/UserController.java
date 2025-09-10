package com.hohoedu.all_pass.user;

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

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final HttpSession session;

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
}
