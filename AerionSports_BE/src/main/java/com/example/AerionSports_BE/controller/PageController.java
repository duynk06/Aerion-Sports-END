package com.example.AerionSports_BE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/dang-ky-nhan-vien")
    public String trangDangKyNhanVien() {
        return "dang-ky-nhan-vien"; // trỏ tới templates/dang-ky-nhan-vien.html
    }

    @GetMapping("/dang-ky-khach-hang")
    public String trangDangKyKhachHang() {
        return "dang-ky-khach-hang"; // trỏ tới templates/dang-ky-khach-hang.html
    }

    @GetMapping("/quen-mat-khau")
    public String trangQuenMatKhau() {
        return "quen-mat-khau";
    }
}
