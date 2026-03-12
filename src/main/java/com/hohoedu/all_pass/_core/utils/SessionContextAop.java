package com.hohoedu.all_pass._core.utils;

import com.hohoedu.all_pass.user._dto.UserRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


@Slf4j
@Aspect
@Component
public class SessionContextAop {

    @Autowired
    private DataSource dataSource;

    @Before("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void setSessionContext() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attr == null) return;

            HttpSession session = attr.getRequest().getSession(false);
            if (session == null) return;

            UserRespDTO.LoginRespDTO user = (UserRespDTO.LoginRespDTO) session.getAttribute("user");
            if (user == null) return;

            String username = user.getUserName();
            log.info("SESSION_CONTEXT userCode 세팅: {}", username);
            if (username == null) return;

            // 트랜잭션과 동일한 커넥션 사용 ← 핵심
            Connection conn = DataSourceUtils.getConnection(dataSource);
            try (PreparedStatement ps = conn.prepareStatement(
                    "EXEC sp_set_session_context @key = N'user_code', @value = ?")) {
                ps.setString(1, username);
                ps.execute();
            }

        } catch (Exception e) {
            log.warn("SESSION_CONTEXT 세팅 실패: {}", e.getMessage());
        }
    }
}
