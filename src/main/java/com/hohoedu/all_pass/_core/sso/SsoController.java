package com.hohoedu.all_pass._core.sso;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.hohoedu.all_pass.user.User;
import com.hohoedu.all_pass.user.UserService;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * 올패스 ↔ 호호책방 직원 계정 SSO 브릿지. 두 앱이 서로 다른 도메인이라 세션을
 * 직접 공유할 수 없어, 원타임 서명 토큰(SsoTokenUtil)으로 상대 서버가 대신
 * 로그인 상태를 만들어준다. 세션 세팅 방식은 UserController#loginUser와 동일하게 맞춘다.
 */
@Controller
@RequestMapping("/sso")
@RequiredArgsConstructor
public class SsoController {

    private final SsoTokenUtil ssoTokenUtil;
    private final UserService userService;
    private final HttpSession session;

    @Value("${book-clinic.base-url}")
    private String bookClinicBaseUrl;

    /** 현재 로그인된 직원 그대로 호호책방으로 이동 */
    @GetMapping("/to-book-clinic")
    public String toBookClinic(@RequestParam(value = "redirectUrl", required = false) String redirectUrl) {
        LoginRespDTO current = (LoginRespDTO) session.getAttribute("user");
        if (current == null) {
            return "redirect:/login";
        }

        String token;
        try {
            token = ssoTokenUtil.issue(current.getUserId(), redirectUrl);
        } catch (RuntimeException e) {
            return "redirect:/";
        }
        String url = UriComponentsBuilder.fromUriString(bookClinicBaseUrl + "/sso/callback")
                .queryParam("token", token)
                .toUriString();
        return "redirect:" + url;
    }

    /** 호호책방이 발급한 토큰을 검증하고 올패스 세션(및 Spring Security 인증)을 새로 만든다 */
    @GetMapping("/callback")
    public String callback(@RequestParam("token") String token) {
        SsoTokenUtil.SsoPrincipal principal;
        try {
            principal = ssoTokenUtil.verify(token);
        } catch (RuntimeException e) {
            // IllegalArgumentException(서명/만료/재사용) 외에 IllegalStateException(키 설정 오류)도
            // 500으로 새지 않도록 여기서 함께 막는다 — 어느 쪽이든 사용자에게는 재로그인 유도가 맞다.
            return "redirect:/login";
        }

        LoginRespDTO dto;
        try {
            dto = userService.loginRespDTOFor(principal.userId());
        } catch (RuntimeException e) {
            return "redirect:/login";
        }

        List<User> userList = userService.findActiveUser(dto);
        session.setAttribute("user", dto);
        session.setAttribute("readableMenus", dto.getReadableMenus());
        session.setAttribute("userList", userList);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                dto.getUserId(), null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        String destination = (principal.redirectUrl() != null && !principal.redirectUrl().isBlank())
                ? principal.redirectUrl() : "/";
        return "redirect:" + destination;
    }
}
