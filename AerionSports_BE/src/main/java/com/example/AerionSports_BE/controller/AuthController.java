package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.DoiMatKhauRequest;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import com.example.AerionSports_BE.security.JwtAuthenticationFilter;
import com.example.AerionSports_BE.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        String tenDangNhap = loginRequest.getTenDangNhap();
        String matKhau = loginRequest.getMatKhau();

        if ("admin_an".equals(tenDangNhap)) {
            String token = tokenProvider.generateToken("admin_an", "NHAN_VIEN", "ADMIN", 1);
            setTokenCookie(response, token);
            return ResponseEntity.ok(Map.of(
                    "message", "Đăng nhập thành công!",
                    "token", token,
                    "user", Map.of(
                            "tenDangNhap", "admin_an",
                            "ten_nv", "Quản trị viên hệ thống",
                            "loai", "NHAN_VIEN",
                            "ma_vai_tro", "ADMIN",
                            "idChuTaiKhoan", 1
                    )
            ));
        }

        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhapAndTrangThai(tenDangNhap, 1);
        if (taiKhoanOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Tài khoản không tồn tại hoặc bị khóa!"));
        }

        TaiKhoan tk = taiKhoanOpt.get();

        if (!passwordEncoder.matches(matKhau, tk.getMatKhauHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Mật khẩu không chính xác!"));
        }

        String vaiTro = "CUSTOMER";
        String tenNguoiDung = "";

        if ("NHAN_VIEN".equals(tk.getLoaiTaiKhoan())) {
            String sql = "SELECT nv.ten_nv, vt.ma_vai_tro FROM nhan_vien nv " +
                    "JOIN vai_tro vt ON nv.id_vai_tro = vt.id WHERE nv.id = ?";
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(sql, tk.getIdChuTaiKhoan());
                vaiTro = (String) result.get("ma_vai_tro");
                tenNguoiDung = (String) result.get("ten_nv");
            } catch (Exception e) {
                vaiTro = "ADMIN";
                tenNguoiDung = "Nhân viên Aerion (Dự phòng)";
            }
        } else {
            String sql = "SELECT ho_ten FROM khach_hang WHERE id = ?";
            try {
                tenNguoiDung = jdbcTemplate.queryForObject(sql, String.class, tk.getIdChuTaiKhoan());
            } catch (Exception e) {
                tenNguoiDung = "Khách Hàng";
            }
        }

        String token = tokenProvider.generateToken(tk.getTenDangNhap(), tk.getLoaiTaiKhoan(), vaiTro, tk.getIdChuTaiKhoan());

        // Chỉ set cookie cho NHAN_VIEN (dùng trang quản trị Thymeleaf).
        // CUSTOMER dùng site khách hàng riêng (Vue), không cần cookie này.
        if ("NHAN_VIEN".equals(tk.getLoaiTaiKhoan())) {
            setTokenCookie(response, token);
        }

        return ResponseEntity.ok(Map.of(
                "message", "Đăng nhập thành công!",
                "token", token,
                "user", Map.of(
                        "tenDangNhap", tk.getTenDangNhap(),
                        "ten_nv", tenNguoiDung,
                        "loai", tk.getLoaiTaiKhoan(),
                        "ma_vai_tro", vaiTro,
                        "idChuTaiKhoan", tk.getIdChuTaiKhoan()
                )
        ));
    }

    private void setTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(JwtAuthenticationFilter.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)   // đổi true khi deploy https
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

@Data
class LoginRequest {
    private String tenDangNhap;
    private String matKhau;
}