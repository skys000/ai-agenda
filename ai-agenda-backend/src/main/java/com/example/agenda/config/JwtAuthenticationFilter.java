package com.example.agenda.config;

import com.example.agenda.util.BaseContext;
import com.example.agenda.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean; // 引入这个

import java.io.IOException;
import java.util.Collections;

// 1. 改为继承 GenericFilterBean
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;

    // 2. 实现 doFilter (注意参数变化)
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        final String authHeader = request.getHeader("Authorization");

        // 如果是 OPTIONS 直接过
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 如果没有 Token，直接过（交给后面的 Security 拦截）
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // ============================================================
            // 关键修正：每次进入都检查 Context。如果异步线程 Context 是空的，就重新解析填进去
            // ============================================================
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                final String jwt = authHeader.substring(7);
                String username = jwtUtil.getUsernameFromToken(jwt);
                Long userId = jwtUtil.getUserIdFromToken(jwt);

                if (username != null) {
                    if (userId == null) userId = 1L; // 你的兜底逻辑
                    BaseContext.setCurrentId(userId);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, Collections.emptyList()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // System.out.println(">>> [Filter] 身份已恢复/设置: " + username);
                }
            }
        } catch (Exception e) {
            // System.out.println(">>> [Filter] Token 解析异常: " + e.getMessage());
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            BaseContext.removeCurrentId();
        }
    }
}