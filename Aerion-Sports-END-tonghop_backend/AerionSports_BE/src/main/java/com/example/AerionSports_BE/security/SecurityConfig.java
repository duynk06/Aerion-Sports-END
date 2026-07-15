package com.example.AerionSports_BE.security;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomAuthEntryPoint customAuthEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // 🟢 ĐÃ SỬA: Chuyển policy thành IF_REQUIRED để Spring Security cho phép tạo Session
                // phục vụ lưu vết phiên làm việc tĩnh cho Thymeleaf Monolith.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )


                .authorizeHttpRequests(auth -> auth


                        // 🔓 TẠM MỞ TOÀN BỘ GIAO DIỆN QUẢN TRỊ THYMELEAF
                                // Trang quản trị Thymeleaf: cần đăng nhập với vai trò ADMIN hoặc NHAN_VIEN
                                .requestMatchers(
                                        "/trang-chu",
                                        "/thong-ke",
                                        "/san-pham", "/san-pham/**",
                                        "/thuoc-tinh/**",
                                        "/giao-ca", "/giao-ca/**",
                                        "/lich-lam-viec", "/lich-lam-viec/**",
                                        "/khach-hang", "/khach-hang/**"
                                ).hasAnyRole("ADMIN", "QL", "NV")

// Các trang chỉ ADMIN mới được vào
                                .requestMatchers(
                                        "/nhan-vien", "/nhan-vien/**",
                                        "/dot-giam-gia", "/dot-giam-gia/**",
                                        "/hoa-don", "/hoa-don/**",
                                        "/phieu-giam-gia/**",
                                        "/ban-hang", "/ban-hang/**",
                                        "/lich-su-thanh-toan/**",
                                        "/lich-su-hoa-don/**",
                                        "/public/online-orders/**",
                                        "/api/public/online-orders/**"

                                ).hasRole("ADMIN")

// API tương ứng
                                .requestMatchers(
                                        "/api/san-pham/**",
                                        "/api/thong-ke/**",
                                        "/api/chat-lieu-khung-vot/**",
                                        "/api/chat-lieu-than-vot/**",
                                        "/api/chu-vi-can-vot/**",
                                        "/api/danh-muc/**",
                                        "/api/diem-can-bang/**",
                                        "/api/do-cung/**",
                                        "/api/mau-sac/**",
                                        "/api/thuong-hieu/**",
                                        "/api/trong-luong/**",
                                        "/api/xuat-xu/**",
                                        "/api/giao-ca/**", "/api/lich-lam-viec/**",
                                        "/uploads/**",
                                        "/api/realtime/**"
                                ).hasAnyRole("ADMIN", "QL", "NV")

                                .requestMatchers("/api/dot-giam-gia/**").hasRole("ADMIN")

                                .requestMatchers("/nhan-vien/**").hasRole("ADMIN")

                                .anyRequest().authenticated()
                );

        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}