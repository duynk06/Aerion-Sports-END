package com.example.AerionSports_BE.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Controller này CHỈ để trả về trang HTML (Thymeleaf view),
// khác với KhachHangController (đang là @RestController trả JSON cho API /public/khach-hang/...).
// Không được gộp chung 2 controller vì @RestController và @Controller xử lý return value khác nhau
// (RestController trả JSON trực tiếp, Controller trả tên view để Thymeleaf render HTML).
@Controller
public class KhachHangViewController {

    @GetMapping("/khach-hang")
    public String danhSachKhachHang() {
        // Trả về templates/khach-hang.html
        return "khach-hang/khach-hang";
    }

    @GetMapping("/khach-hang/them")
    public String themKhachHang() {
        // Trả về templates/khach-hang-them.html
        return "khach-hang/khach-hang-them";
    }

    @GetMapping("/khach-hang/sua/{id}")
    public String suaKhachHang(@PathVariable Integer id) {
        // Trả về templates/khach-hang-sua.html
        // id được lấy lại phía client bằng JS (window.location.pathname), nên ở đây
        // không cần add vào Model, nhưng vẫn khai báo @PathVariable để Spring match đúng route.
        return "khach-hang/khach-hang-sua";
    }
}