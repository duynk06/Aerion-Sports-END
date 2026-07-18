package com.example.AerionSports_BE.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException)
            throws IOException {

        String uri = request.getRequestURI();

        // Request tới /api/** (gọi từ JS/Vue) -> trả JSON 401, để JS tự xử lý
        if (uri.startsWith("/api/")) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"message\":\"Chưa đăng nhập hoặc phiên đã hết hạn\"}");
        } else {
            // Request điều hướng trang Thymeleaf -> redirect về trang login
            response.sendRedirect("/login");
        }
    }
}