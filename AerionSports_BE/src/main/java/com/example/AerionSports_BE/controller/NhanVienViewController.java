package com.example.AerionSports_BE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Controller này CHỈ để trả về trang HTML (Thymeleaf view),
// tương tự KhachHangViewController — tách riêng khỏi NhanVienController
// (đang là @RestController trả JSON cho API /nhan-vien/...).
@Controller
public class NhanVienViewController {

    @GetMapping("/nhan-vien")
    public String danhSachNhanVien() {
        // Trả về templates/nhan-vien.html
        return "nhan-vien/nhan-vien";
    }

    @GetMapping("/nhan-vien/them")
    public String themNhanVien() {
        // Trả về templates/nhan-vien-them.html
        return "nhan-vien/nhan-vien-them";
    }

    @GetMapping("/nhan-vien/sua/{id}")
    public String suaNhanVien(@PathVariable Integer id) {
        // Trả về templates/nhan-vien-sua.html
        // id được lấy lại phía client bằng JS (window.location.pathname)
        return "nhan-vien/nhan-vien-sua";
    }
}