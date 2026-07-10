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

                .authorizeHttpRequests(auth -> auth

                        // 🔓 PHÂN HỆ CÔNG KHAI TỰ DO
                        .requestMatchers("/api/auth/**", "/auth/**").permitAll()
                        .requestMatchers("/public/client-auth/**", "/api/public/client-auth/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        .requestMatchers("/ban-hang", "/ban-hang/**").permitAll()
                        .requestMatchers("/hoa-don", "/hoa-don/**").permitAll()
                        .requestMatchers("/chi-tiet-hoa-don/**").permitAll()
                        .requestMatchers("/lich-su-hoa-don/**").permitAll()
                        .requestMatchers("/lich-su-thanh-toan/**").permitAll()
                        .requestMatchers("/phieu-giam-gia/**").permitAll()
                        .requestMatchers("/public/khach-hang/**").permitAll()
                        .requestMatchers("/public/online-orders/**").permitAll()
                        .requestMatchers("/api/public/online-orders/**").permitAll()

                        .requestMatchers("/api/san-pham/search").permitAll()
                        .requestMatchers("/api/chi-tiet-san-pham/**", "/api/hinh-anh-sp/**").permitAll()
                        .requestMatchers("/api/chat-lieu-khung-vot/all", "/api/chat-lieu-khung-vot/search").permitAll()
                        .requestMatchers("/api/chat-lieu-than-vot/all", "/api/chat-lieu-than-vot/search").permitAll()
                        .requestMatchers("/api/chu-vi-can-vot/active", "/api/chu-vi-can-vot/search", "/api/chu-vi-can-vot/*").permitAll()
                        .requestMatchers("/api/danh-muc/all", "/api/danh-muc/search").permitAll()
                        .requestMatchers("/api/diem-can-bang/all", "/api/diem-can-bang/search").permitAll()
                        .requestMatchers("/api/do-cung/all", "/api/do-cung/search").permitAll()
                        .requestMatchers("/api/mau-sac/all", "/api/mau-sac/search").permitAll()
                        .requestMatchers("/api/thuong-hieu/all", "/api/thuong-hieu/search", "/api/thuong-hieu/detail/*").permitAll()
                        .requestMatchers("/api/trong-luong/all", "/api/trong-luong/search").permitAll()
                        .requestMatchers("/api/xuat-xu/all", "/api/xuat-xu/search").permitAll()
                        .requestMatchers("/api/auth/doi-mat-khau").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/api/realtime/**").permitAll()

                        // 🔓 TẠM MỞ TOÀN BỘ GIAO DIỆN QUẢN TRỊ THYMELEAF
                        .requestMatchers(
                                "/trang-chu",
                                "/thong-ke",
                                "/san-pham", "/san-pham/**",
                                "/thuoc-tinh/**",
                                "/dot-giam-gia", "/dot-giam-gia/**",
                                "/nhan-vien", "/nhan-vien/**",
                                "/giao-ca", "/giao-ca/**",
                                "/lich-lam-viec", "/lich-lam-viec/**",
                                "/khach-hang", "/khach-hang/**"
                        ).permitAll()

                        // 🔓 TẠM MỞ LUÔN CÁC API TƯƠNG ỨNG ĐỂ NÚT BẤM TRONG TRANG HOẠT ĐỘNG ĐƯỢC
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
                                "/api/dot-giam-gia/**"


                        ).permitAll()

                        .anyRequest().authenticated()
                );

        httpSecurity.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }
}