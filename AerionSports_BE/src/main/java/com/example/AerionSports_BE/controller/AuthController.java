package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.DoiMatKhauRequest;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import com.example.AerionSports_BE.security.JwtTokenProvider;
import com.example.AerionSports_BE.service.EmailService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
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
    private EmailService emailService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String PASSWORD_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // =========================================================
    // ĐĂNG NHẬP
    // =========================================================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String tenDangNhap = loginRequest.getTenDangNhap();
        String matKhau = loginRequest.getMatKhau();

        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhapAndTrangThai(tenDangNhap, 1);
        if (taiKhoanOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Tài khoản không tồn tại hoặc bị khóa!"));
        }

        TaiKhoan tk = taiKhoanOpt.get();

        if (!passwordEncoder.matches(matKhau, tk.getMatKhauHash())) {
            return ResponseEntity.status(401).body(Map.of("message", "Mật khẩu không chính xác!"));
        }

        // 🌟 Mặc định là KHACH_HANG (khớp với dữ liệu thực tế trong bảng tai_khoan)
        String vaiTro = "KHACH_HANG";
        String tenNguoiDung = "";

        // 🆕 loai_tai_khoan giờ lưu chữ có dấu ("QUẢN LÝ" / "NHÂN VIÊN") thay vì mã "NHAN_VIEN" cố định,
        // nên coi mọi tài khoản KHÔNG PHẢI khách hàng là tài khoản nhân sự (nhân_vien).
        boolean laTaiKhoanNhanSu = !"KHACH_HANG".equals(tk.getLoaiTaiKhoan());

        if (laTaiKhoanNhanSu) {
            String sql = "SELECT nv.ten_nv, vt.ma_vai_tro FROM nhan_vien nv " +
                    "JOIN vai_tro vt ON nv.id_vai_tro = vt.id WHERE nv.id = ?";
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(sql, tk.getIdChuTaiKhoan());
                vaiTro = (String) result.get("ma_vai_tro");
                tenNguoiDung = (String) result.get("ten_nv");
            } catch (Exception e) {
                // 🆕 Vai trò ADMIN đã bị bỏ, dự phòng giờ là QL
                vaiTro = "QL";
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

    // =========================================================
    // ĐỔI MẬT KHẨU (yêu cầu đã đăng nhập)
    // =========================================================
    @PutMapping("/doi-mat-khau")
    public ResponseEntity<?> doiMatKhau(@Valid @RequestBody DoiMatKhauRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapAndTrangThai(currentUsername, 1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hợp lệ!"));

        boolean isOldPasswordValid = passwordEncoder.matches(request.getMatKhauCu(), taiKhoan.getMatKhauHash());

        if (!isOldPasswordValid) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu cũ không chính xác!"));
        }

        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhau())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu mới và xác nhận mật khẩu không trùng khớp!"));
        }

        taiKhoan.setMatKhauHash(passwordEncoder.encode(request.getMatKhauMoi()));
        taiKhoanRepository.save(taiKhoan);

        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công! 🎉"));
    }

    // =========================================================
    // ĐĂNG KÝ TÀI KHOẢN NHÂN VIÊN (public, tự sinh mật khẩu, gửi mail)
    // =========================================================
    @PostMapping("/dang-ky-nhan-vien")
    public ResponseEntity<?> dangKyNhanVien(@RequestBody DangKyNhanVienRequest req) {

        if (req.getHoTen() == null || req.getHoTen().isBlank()
                || req.getEmail() == null || req.getEmail().isBlank()
                || req.getSdt() == null || req.getSdt().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập đầy đủ họ tên, số điện thoại và email!"));
        }

        Integer countEmail = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM nhan_vien WHERE email = ?", Integer.class, req.getEmail());
        if (countEmail != null && countEmail > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng!"));
        }

        Integer countUser = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tai_khoan WHERE ten_dang_nhap = ?", Integer.class, req.getEmail());
        if (countUser != null && countUser > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email này đã có tài khoản đăng nhập!"));
        }

        Integer idVaiTroNV;
        try {
            idVaiTroNV = jdbcTemplate.queryForObject(
                    "SELECT id FROM vai_tro WHERE ma_vai_tro = 'NV'", Integer.class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Chưa cấu hình vai trò NV trong hệ thống!"));
        }

        String maNv = "NV" + System.currentTimeMillis();

        jdbcTemplate.update(
                "INSERT INTO nhan_vien (id_vai_tro, ma_nv, ten_nv, sdt, email, ngay_tao, trang_thai) " +
                        "VALUES (?, ?, ?, ?, ?, GETDATE(), 1)",
                idVaiTroNV, maNv, req.getHoTen(), req.getSdt(), req.getEmail()
        );

        Integer idNhanVien = jdbcTemplate.queryForObject(
                "SELECT id FROM nhan_vien WHERE ma_nv = ?", Integer.class, maNv);

        String tenDangNhap = req.getEmail();
        String matKhauGoc = generateRandomPassword();

        // 🆕 Đăng ký public luôn tạo vai trò NHÂN VIÊN (chữ có dấu), khớp cột NVARCHAR mới
        jdbcTemplate.update(
                "INSERT INTO tai_khoan (ten_dang_nhap, mat_khau_hash, loai_tai_khoan, id_chu_tai_khoan, ngay_tao, ngay_cap_nhat, trang_thai) " +
                        "VALUES (?, ?, N'NHÂN VIÊN', ?, GETDATE(), GETDATE(), 1)",
                tenDangNhap, passwordEncoder.encode(matKhauGoc), idNhanVien
        );

        emailService.sendAccountCreationEmail(req.getEmail(), req.getHoTen(), matKhauGoc);

        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công! Thông tin tài khoản đã được gửi qua email."));
    }

    // =========================================================
    // ĐĂNG KÝ TÀI KHOẢN KHÁCH HÀNG (public, tự sinh mật khẩu, gửi mail)
    // =========================================================
    @PostMapping("/dang-ky-khach-hang")
    public ResponseEntity<?> dangKyKhachHang(@RequestBody DangKyKhachHangRequest req) {

        if (req.getHoTen() == null || req.getHoTen().isBlank()
                || req.getEmail() == null || req.getEmail().isBlank()
                || req.getSdt() == null || req.getSdt().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập đầy đủ họ tên, số điện thoại và email!"));
        }

        Integer countEmail = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM khach_hang WHERE email = ?", Integer.class, req.getEmail());
        if (countEmail != null && countEmail > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng!"));
        }

        Integer countSdt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM khach_hang WHERE sdt = ?", Integer.class, req.getSdt());
        if (countSdt != null && countSdt > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số điện thoại đã được sử dụng!"));
        }

        Integer countUser = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tai_khoan WHERE ten_dang_nhap = ?", Integer.class, req.getEmail());
        if (countUser != null && countUser > 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email này đã có tài khoản đăng nhập!"));
        }

        String maKhachHang = "KH" + System.currentTimeMillis();

        jdbcTemplate.update(
                "INSERT INTO khach_hang (ma_khach_hang, ho_ten, sdt, email, diem_tich_luy, ngay_tao, trang_thai) " +
                        "VALUES (?, ?, ?, ?, 0, GETDATE(), 1)",
                maKhachHang, req.getHoTen(), req.getSdt(), req.getEmail()
        );

        Integer idKhachHang = jdbcTemplate.queryForObject(
                "SELECT id FROM khach_hang WHERE ma_khach_hang = ?", Integer.class, maKhachHang);

        String tenDangNhap = req.getEmail();
        String matKhauGoc = generateRandomPassword();

        jdbcTemplate.update(
                "INSERT INTO tai_khoan (ten_dang_nhap, mat_khau_hash, loai_tai_khoan, id_chu_tai_khoan, ngay_tao, ngay_cap_nhat, trang_thai) " +
                        "VALUES (?, ?, 'KHACH_HANG', ?, GETDATE(), GETDATE(), 1)",
                tenDangNhap, passwordEncoder.encode(matKhauGoc), idKhachHang
        );

        emailService.sendKhachHangAccountEmail(req.getEmail(), req.getHoTen(), matKhauGoc);

        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công! Thông tin tài khoản đã được gửi qua email."));
    }

    // =========================================================
    // QUÊN MẬT KHẨU (public, tự sinh mật khẩu mới, gửi qua mail)
    // =========================================================
    @PostMapping("/quen-mat-khau")
    public ResponseEntity<?> quenMatKhau(@RequestBody QuenMatKhauRequest req) {

        if (req.getEmail() == null || req.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng nhập email!"));
        }

        Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByTenDangNhapAndTrangThai(req.getEmail(), 1);
        if (tkOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại trong hệ thống, mật khẩu mới đã được gửi tới hộp thư của bạn."));
        }

        TaiKhoan tk = tkOpt.get();
        String matKhauMoi = generateRandomPassword();
        tk.setMatKhauHash(passwordEncoder.encode(matKhauMoi));
        taiKhoanRepository.save(tk);

        // 🆕 tương tự login(): coi mọi tài khoản khác KHACH_HANG là nhân sự
        String tenNguoiDung;
        if (!"KHACH_HANG".equals(tk.getLoaiTaiKhoan())) {
            try {
                tenNguoiDung = jdbcTemplate.queryForObject(
                        "SELECT ten_nv FROM nhan_vien WHERE id = ?", String.class, tk.getIdChuTaiKhoan());
            } catch (Exception e) {
                tenNguoiDung = "Nhân viên Aerion";
            }
        } else {
            try {
                tenNguoiDung = jdbcTemplate.queryForObject(
                        "SELECT ho_ten FROM khach_hang WHERE id = ?", String.class, tk.getIdChuTaiKhoan());
            } catch (Exception e) {
                tenNguoiDung = "Khách hàng";
            }
        }

        emailService.sendResetPasswordEmail(req.getEmail(), tenNguoiDung, matKhauMoi);

        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại trong hệ thống, mật khẩu mới đã được gửi tới hộp thư của bạn."));
    }

    private String generateRandomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}

@Data
class LoginRequest {
    private String tenDangNhap;
    private String matKhau;
}

@Data
class DangKyNhanVienRequest {
    private String hoTen;
    private String sdt;
    private String email;
}

@Data
class DangKyKhachHangRequest {
    private String hoTen;
    private String sdt;
    private String email;
}

@Data
class QuenMatKhauRequest {
    private String email;
}