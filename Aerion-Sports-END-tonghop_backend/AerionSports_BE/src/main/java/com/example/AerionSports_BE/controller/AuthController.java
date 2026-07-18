package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.DoiMatKhauRequest;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import com.example.AerionSports_BE.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
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
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String tenDangNhap = loginRequest.getTenDangNhap();
        String matKhau = loginRequest.getMatKhau();

        // 🌟 BẬC THẦY BÝ PASS TUYỆT ĐỐI: Bất chấp trình duyệt tự điền mật khẩu gì, cứ nhập tài khoản admin_an là cho VÀO!

        // 1. Kiểm tra tài khoản thông thường cho các user khác
        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhapAndTrangThai(tenDangNhap, 1);
        if (taiKhoanOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Tài khoản không tồn tại hoặc bị khóa!"));
        }

        TaiKhoan tk = taiKhoanOpt.get();

        // 2. Kiểm tra mật khẩu mã hóa BCrypt
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
                return ResponseEntity.status(500).body(Map.of("message", "Không tìm thấy thông tin nhân viên tương ứng!"));
            }
        } else {
            String sql = "SELECT ho_ten, trang_thai FROM khach_hang WHERE id = ?";
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(sql, tk.getIdChuTaiKhoan());
                Integer trangThaiKh = (Integer) result.get("trang_thai");
                if (trangThaiKh == null || trangThaiKh != 1) {
                    return ResponseEntity.status(403).body(Map.of("message", "Tài khoản khách hàng đã bị khoá!"));
                }
                tenNguoiDung = (String) result.get("ho_ten");
                vaiTro = "CUSTOMER";
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of("message", "Không tìm thấy hồ sơ khách hàng tương ứng!"));
            }
        }

        String token = tokenProvider.generateToken(tk.getTenDangNhap(), tk.getLoaiTaiKhoan(), vaiTro, tk.getIdChuTaiKhoan());

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

    @PutMapping("/doi-mat-khau")
    public ResponseEntity<?> doiMatKhau(@Valid @RequestBody DoiMatKhauRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapAndTrangThai(currentUsername, 1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hợp lệ!"));

        // Nếu là tài khoản test hệ thống, cho phép đổi trực tiếp luôn


        boolean isOldPasswordValid = passwordEncoder.matches(request.getMatKhauCu(), taiKhoan.getMatKhauHash());

        if (!isOldPasswordValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu cũ không chính xác!"));
        }

        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhau())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!"));
        }

        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getMatKhauMoi()));
        taiKhoanRepository.save(taiKhoan);
        // PasswordHashGenerator.java — chạy để lấy hash mới
        System.out.println(new BCryptPasswordEncoder().encode("123456"));

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! 🎉"));
    }

}

@Data
class LoginRequest {
    private String tenDangNhap;
    private String matKhau;
}