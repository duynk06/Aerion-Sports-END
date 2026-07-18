package com.example.AerionSports_BE.controller;


import com.example.AerionSports_BE.dto.DangKyKhachHangRequest;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/public/client-auth")
@CrossOrigin("*")
public class ClientAuthController {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@Valid @RequestBody DangKyKhachHangRequest request) {

        if (!request.getMatKhau().equals(request.getNhapLaiMatKhau())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Mật khẩu và xác nhận mật khẩu không trùng khớp!"));
        }

        if (khachHangRepository.existsBySdt(request.getSdt())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Số điện thoại đã được đăng ký!"));
        }

        if (khachHangRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email đã được đăng ký!"));
        }

        if (taiKhoanRepository.findByTenDangNhapAndTrangThai(request.getEmail(), 1).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email này đã được dùng làm tên đăng nhập!"));
        }

        // 1. Tạo hồ sơ khách hàng
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang("KH" + System.currentTimeMillis() % 100000);
        kh.setHoTen(request.getHoTen().trim());
        kh.setSdt(request.getSdt().trim());
        kh.setEmail(request.getEmail().trim());
        if (request.getNgaySinh() != null && !request.getNgaySinh().isBlank()) {
            kh.setNgaySinh(LocalDate.parse(request.getNgaySinh()));
        }
        kh.setGioiTinh(request.getGioiTinh());
        kh.setDiemTichLuy(0);
        kh.setTrangThai(1);
        kh.setNgayTao(LocalDateTime.now());
        kh.setNgayCapNhat(LocalDateTime.now());

        KhachHang savedKh = khachHangRepository.save(kh);

        // 2. Tạo tài khoản đăng nhập gắn với hồ sơ vừa tạo
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(request.getEmail().trim());
        tk.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        tk.setLoaiTaiKhoan("KHACH_HANG");
        tk.setIdChuTaiKhoan(savedKh.getId());
        tk.setNgayTao(LocalDateTime.now());
        tk.setNgayCapNhat(LocalDateTime.now());
        tk.setTrangThai(1);

        taiKhoanRepository.save(tk);

        return ResponseEntity.ok(Map.of("message", "Đăng ký tài khoản thành công! Vui lòng đăng nhập."));
    }
}