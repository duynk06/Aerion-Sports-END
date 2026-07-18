package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.NhanVienRepository;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/thong-tin-ca-nhan")
public class ThongTinCaNhanController {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @GetMapping
    public String hienThi(Model model) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhapAndTrangThai(currentUsername, 1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hợp lệ!"));

        NhanVien nhanVien = nhanVienRepository.findById(taiKhoan.getIdChuTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin nhân viên!"));

        model.addAttribute("nhanVien", nhanVien);
        return "thong-tin-ca-nhan/index";
    }
}