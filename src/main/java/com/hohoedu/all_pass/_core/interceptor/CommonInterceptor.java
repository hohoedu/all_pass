package com.hohoedu.all_pass._core.interceptor;

import com.hohoedu.all_pass.student.repository.StudentRepository;
import com.hohoedu.all_pass.user._dto.UserRespDTO.LoginRespDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommonInterceptor implements HandlerInterceptor {

    private final StudentRepository studentRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {

            if (session.getAttribute("hasPendingStudent") == null) {
                LoginRespDTO user = (LoginRespDTO) session.getAttribute("user");

                int count = studentRepository.countPendingStudents(user.getCenterCode());
                session.setAttribute("hasPendingStudent", count > 0);

            }
        }
        return true;
    }
}
