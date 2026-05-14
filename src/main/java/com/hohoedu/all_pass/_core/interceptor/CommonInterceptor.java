package com.hohoedu.all_pass._core.interceptor;

import com.hohoedu.all_pass.student.repository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;


@Component
@RequiredArgsConstructor
public class CommonInterceptor implements HandlerInterceptor {

    private final StudentRepository studentRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("centerCode") != null) {
            String centerCode = (String) session.getAttribute("centerCode");

            int count = studentRepository.countPendingStudents(centerCode);
            session.setAttribute("hasPendingStudent", count > 0);
        }
        return true;
    }
}
