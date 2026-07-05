package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.response.HoaDonResponse;
import com.example.AerionSports_BE.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping
    public String hienThiDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer loaiHoaDonFilter,
            @RequestParam(required = false) Integer trangThaiFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        if (tuNgay == null) {
            tuNgay = LocalDate.now();
        }
        if (denNgay == null) {
            denNgay = LocalDate.now();
        }
        Page<HoaDonResponse> pageData = hoaDonService.filterHoaDon(keyword, loaiHoaDonFilter, trangThaiFilter, tuNgay, denNgay, page, size);
        model.addAttribute("listHoaDon", pageData.getContent());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalElements", pageData.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiHoaDonFilter", loaiHoaDonFilter);
        model.addAttribute("trangThaiFilter", trangThaiFilter);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        return "hoa-don/index";
    }
    @GetMapping("/chi-tiet/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        HoaDonResponse hoaDon = hoaDonService.detail(id);

        // Bổ sung 3 danh sách còn thiếu (đổi tên method cho khớp service thật của bạn)
        var chiTietSanPham = hoaDonService.getChiTietHoaDon(id);     // List<...>
        var lichSuThanhToan = hoaDonService.getLichSuThanhToan(id);  // List<...>
        var lichSuHoaDon    = hoaDonService.getLichSuHoaDon(id);     // List<...>

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("chiTietSanPham", chiTietSanPham);
        model.addAttribute("lichSuThanhToan", lichSuThanhToan);
        model.addAttribute("lichSuHoaDon", lichSuHoaDon);

        return "hoa-don/detail";
    }
    @PostMapping("/{id}/chuyen-trang-thai")
    public String chuyenTrangThai(
            @PathVariable Integer id,
            @RequestParam Integer trangThaiMoi,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        try {
            hoaDonService.chuyenTrangThai(id, trangThaiMoi, ghiChu, "admin@example.com");
            redirectAttributes.addFlashAttribute("successMessage", "Chuyển trạng thái thành công!");
            return "redirect:/hoa-don/chi-tiet/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hoa-don/chi-tiet/" + id;
        }
    }
}