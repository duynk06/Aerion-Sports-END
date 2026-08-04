package com.example.AerionSports_BE.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

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
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        httpSecurity
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth

                        // =========================================================
                        // 🔓 NHÓM 1: PUBLIC - KHÔNG CẦN ĐĂNG NHẬP
                        // =========================================================
                        .requestMatchers(
                                "/",
                                "/cua-hang",
                                "/cua-hang/**",

                                "/ws-chat",
                                "/ws-chat/**",
                                "/topic/**",
                                "/app/**",

                                "/api/auth/**",
                                "/auth/**",

                                "/login",
                                "/login/**",
                                "/logout",
                                "/access-denied",

                                "/public/client-auth/**",
                                "/api/public/client-auth/**",

                                "/public/online-orders/**",
                                "/api/public/online-orders/**",

                                "/dang-ky-nhan-vien",
                                "/dang-ky-khach-hang",
                                "/quen-mat-khau",

                                "/ban-hang-online/**",

                                "/error",

                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        .requestMatchers(HttpMethod.GET,
                                "/san-pham/them-moi",
                                "/san-pham/bien-the/them-moi",
                                "/san-pham/bien-the/sua"
                        ).hasAnyRole("QL")

                        // =========================================================
                        // 🔒 NHÓM 2-B: XEM (GET) - QL + NV
                        // =========================================================
                        .requestMatchers(HttpMethod.GET,
                                "/ban-hang", "/ban-hang/**",
                                "/hoa-don", "/hoa-don/**",
                                "/san-pham", "/san-pham/**",
                                "/thuoc-tinh/**",
                                "/khach-hang", "/khach-hang/**",
                                "/lich-lam-viec", "/lich-lam-viec/**",
                                "/dot-giam-gia", "/dot-giam-gia/**",
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
                                "/api/dot-giam-gia/**",
                                "/phieu-giam-gia", "/phieu-giam-gia/**"
                        ).hasAnyRole("QL", "NV")

                        // 🆕 NHÓM 2-D: HÓA ĐƠN (POST) - QL + NV
                        // Nhân viên cần tự chuyển trạng thái hóa đơn (xác nhận, hủy...) qua trang chi tiết
                        .requestMatchers(HttpMethod.POST,
                                "/hoa-don/**"
                        ).hasAnyRole("QL", "NV")

                        // =========================================================
                        // 🆕 NHÓM 2-E: BÁN HÀNG TẠI QUẦY (POST/PUT/DELETE) - QL + NV
                        // ⚠️ QUAN TRỌNG: Đây là màn hình NHÂN VIÊN thao tác hằng ngày
                        // (tạo hóa đơn, thêm/xóa sản phẩm, đổi số lượng, áp phiếu giảm giá,
                        // thanh toán, hủy hóa đơn, quản lý địa chỉ giao hàng...).
                        // Phải đặt các dòng này TRƯỚC nhóm QL-only ở NHÓM 2-C bên dưới,
                        // vì Spring Security áp dụng đúng rule đầu tiên khớp theo thứ tự khai báo.
                        // Thiếu nhóm này sẽ khiến toàn bộ thao tác ghi (tạo đơn, thêm sản phẩm,
                        // thanh toán, hủy đơn...) của tài khoản NV bị chặn -> access-denied -> 404.
                        // =========================================================
                        .requestMatchers(HttpMethod.POST,
                                "/ban-hang/**"
                        ).hasAnyRole("QL", "NV")

                        .requestMatchers(HttpMethod.PUT,
                                "/ban-hang/**"
                        ).hasAnyRole("QL", "NV")

                        .requestMatchers(HttpMethod.DELETE,
                                "/ban-hang/**"
                        ).hasAnyRole("QL", "NV")

                        // =========================================================
                        // 🔒 NHÓM 2-C: THÊM/SỬA/XÓA (POST/PUT/DELETE) - CHỈ QL
                        // ⚠️ "/ban-hang/**" không còn nằm trong nhóm này (xem NHÓM 2-E)
                        // ⚠️ "/hoa-don/**" (POST) không nằm trong nhóm này (xem NHÓM 2-D)
                        // =========================================================
                        .requestMatchers(HttpMethod.POST,
                                "/san-pham/**", "/thuoc-tinh/**",
                                "/khach-hang/**", "/lich-lam-viec/**",
                                "/dot-giam-gia/**", "/api/dot-giam-gia/**",
                                "/api/san-pham/**", "/api/chat-lieu-khung-vot/**", "/api/chat-lieu-than-vot/**",
                                "/api/chu-vi-can-vot/**", "/api/danh-muc/**", "/api/diem-can-bang/**",
                                "/api/do-cung/**", "/api/mau-sac/**", "/api/thuong-hieu/**",
                                "/api/trong-luong/**", "/api/xuat-xu/**", "/phieu-giam-gia/**"
                        ).hasAnyRole("QL")

                        .requestMatchers(HttpMethod.PUT,
                                "/san-pham/**", "/thuoc-tinh/**",
                                "/khach-hang/**", "/lich-lam-viec/**",
                                "/dot-giam-gia/**", "/api/dot-giam-gia/**",
                                "/api/san-pham/**", "/api/chat-lieu-khung-vot/**", "/api/chat-lieu-than-vot/**",
                                "/api/chu-vi-can-vot/**", "/api/danh-muc/**", "/api/diem-can-bang/**",
                                "/api/do-cung/**", "/api/mau-sac/**", "/api/thuong-hieu/**",
                                "/api/trong-luong/**", "/api/xuat-xu/**", "/phieu-giam-gia/**"
                        ).hasAnyRole("QL")

                        .requestMatchers(HttpMethod.DELETE,
                                "/san-pham/**", "/thuoc-tinh/**",
                                "/khach-hang/**", "/lich-lam-viec/**",
                                "/dot-giam-gia/**", "/api/dot-giam-gia/**",
                                "/api/san-pham/**", "/api/chat-lieu-khung-vot/**", "/api/chat-lieu-than-vot/**",
                                "/api/chu-vi-can-vot/**", "/api/danh-muc/**", "/api/diem-can-bang/**",
                                "/api/do-cung/**", "/api/mau-sac/**", "/api/thuong-hieu/**",
                                "/api/trong-luong/**", "/api/xuat-xu/**", "/phieu-giam-gia/**"
                        ).hasAnyRole("QL")

                        // =========================================================
                        // 🔒 NHÓM 3: CHỈ QL
                        // =========================================================
                        .requestMatchers(

                                "/thong-ke",

                                "/nhan-vien",
                                "/nhan-vien/**",

                                "/dot-giam-gia",
                                "/dot-giam-gia/**",

                                "/phieu-giam-gia",
                                "/phieu-giam-gia/**",

                                "/lich-su-thanh-toan/**",
                                "/lich-su-hoa-don/**",

                                "/api/dot-giam-gia/**"

                        ).hasAnyRole("QL")

                        // =========================================================
                        // 🔒 NHÓM 4: CÒN LẠI PHẢI ĐĂNG NHẬP
                        // =========================================================
                        .anyRequest().authenticated()

                );

        httpSecurity.addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return httpSecurity.build();
    }
}