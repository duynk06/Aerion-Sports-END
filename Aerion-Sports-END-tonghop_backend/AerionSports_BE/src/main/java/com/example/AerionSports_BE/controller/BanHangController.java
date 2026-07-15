package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.SanPhamPosDTO;
import com.example.AerionSports_BE.dto.request.DiaChiRequest;
import com.example.AerionSports_BE.dto.request.ThanhToanRequest;
import com.example.AerionSports_BE.dto.request.ThemSanPhamRequest;
import com.example.AerionSports_BE.dto.response.BanHangResponse;
import com.example.AerionSports_BE.dto.response.KhachHangPosResponse;
import com.example.AerionSports_BE.dto.response.SanPhamPosResponse;
import com.example.AerionSports_BE.service.BanHangService;
import com.example.AerionSports_BE.service.KhachHangPosService;
import com.example.AerionSports_BE.service.SanPhamPosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller // Đổi từ @RestController thành @Controller để hỗ trợ Thymeleaf
@RequestMapping("/ban-hang")
@RequiredArgsConstructor
public class BanHangController {

    private final BanHangService banHangService;
    private final SanPhamPosService sanPhamPosService;
    private final KhachHangPosService khachHangPosService;

    // =========================================================================
    // 1. ENDPOINT TRẢ VỀ GIAO DIỆN THYMELEAF (Chạy khi vào URL /ban-hang)
    // =========================================================================
    @GetMapping
    public String hienThiTrangBanHang(Model model) {
        // Trả về file giao diện: src/main/resources/templates/ban-hang.html
        // (Nếu file nằm trong thư mục con thì return "admin/ban-hang")
        return "ban-hang/index";
    }

    // =========================================================================
    // 2. CÁC ENDPOINT XỬ LÝ DỮ LIỆU BẰNG AJAX (Không tải lại trang)
    // Spring Boot sẽ tự hiểu ResponseEntity là trả về JSON
    // =========================================================================

    @PostMapping("/tao-hoa-don")
    @ResponseBody // Đảm bảo trả về dữ liệu, không tìm kiếm file HTML
    public ResponseEntity<BanHangResponse> taoHoaDon() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        return ResponseEntity.ok(banHangService.taoHoaDonCho(username));
    }

    @GetMapping("/hoa-don-cho")
    @ResponseBody
    public ResponseEntity<List<BanHangResponse>> getHoaDonCho() {
        return ResponseEntity.ok(banHangService.getHoaDonCho());
    }

    @GetMapping("/san-pham")
    @ResponseBody
    public ResponseEntity<Page<SanPhamPosResponse>> locSanPham(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer idMauSac,
            @RequestParam(required = false) Integer idTrongLuong,
            @RequestParam(required = false) BigDecimal giaMin,
            @RequestParam(required = false) BigDecimal giaMax,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(sanPhamPosService.locSanPham(
                keyword, idMauSac, idTrongLuong, giaMin, giaMax, trangThai, page, size));
    }

    @GetMapping("/khoang-gia")
    @ResponseBody
    public ResponseEntity<?> getKhoangGia() {
        return ResponseEntity.ok(sanPhamPosService.getKhoangGia());
    }

    @GetMapping("/khach-hang")
    @ResponseBody
    public ResponseEntity<Page<KhachHangPosResponse>> timKhachHangPos(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        return ResponseEntity.ok(khachHangPosService.locKhachHangPos(keyword, page, size));
    }

    @PutMapping("/{id}/khach-hang")
    @ResponseBody
    public ResponseEntity<BanHangResponse> capNhatKhachHang(
            @PathVariable("id") Integer id,
            @RequestParam(required = false) Integer idKhachHang
    ) {
        return ResponseEntity.ok(banHangService.capNhatKhachHangVaoHoaDon(id, idKhachHang));
    }

    @PostMapping("/them-san-pham")
    @ResponseBody
    public ResponseEntity<?> themSanPham(@RequestBody ThemSanPhamRequest request) {
        try {
            return ResponseEntity.ok(banHangService.themSanPhamVaoHoaDon(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/chi-tiet/{id}/so-luong")
    @ResponseBody
    public ResponseEntity<?> capNhatSoLuong(
            @PathVariable Integer id,
            @RequestParam Integer soLuong
    ) {
        try {
            return ResponseEntity.ok(banHangService.capNhatSoLuong(id, soLuong));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/chi-tiet/{id}")
    @ResponseBody
    public ResponseEntity<?> xoaChiTiet(@PathVariable Integer id) {
        try {
            banHangService.xoaChiTietHoaDon(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/thanh-toan")
    @ResponseBody
    public ResponseEntity<?> thanhToan(@RequestBody ThanhToanRequest request) {
        try {
            return ResponseEntity.ok(banHangService.thanhToan(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/hoa-don/{id}")
    @ResponseBody
    public ResponseEntity<?> huyHoaDon(@PathVariable Integer id) {
        try {
            banHangService.huyHoaDon(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/loai-hoa-don")
    @ResponseBody
    public ResponseEntity<?> capNhatLoaiHoaDon(
            @PathVariable Integer id,
            @RequestParam Integer loaiHoaDon
    ) {
        try {
            return ResponseEntity.ok(banHangService.capNhatLoaiHoaDon(id, loaiHoaDon));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/phi-van-chuyen")
    @ResponseBody
    public ResponseEntity<?> capNhatPhiVanChuyen(
            @PathVariable Integer id,
            @RequestParam BigDecimal phiVanChuyen
    ) {
        try {
            return ResponseEntity.ok(banHangService.capNhatPhiVanChuyen(id, phiVanChuyen));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/khach-hang/{id}/dia-chi")
    @ResponseBody
    public ResponseEntity<?> getDiaChiKhachHang(@PathVariable Integer id) {
        return ResponseEntity.ok(banHangService.getDiaChiKhachHang(id));
    }

    @GetMapping("/{id}/phieu-giam-gia-tot-nhat")
    @ResponseBody
    public ResponseEntity<?> getPhieuTotNhat(@PathVariable Integer id) {
        return ResponseEntity.ok(banHangService.timPhieuGiamGiaTotNhat(id));
    }

    @PutMapping("/{id}/ap-dung-phieu")
    @ResponseBody
    public ResponseEntity<?> apDungPhieu(
            @PathVariable Integer id,
            @RequestParam Integer idPhieu
    ) {
        try {
            return ResponseEntity.ok(banHangService.apDungPhieuGiamGia(id, idPhieu));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/hoa-don/{id}/kiem-tra-gia")
    @ResponseBody
    public ResponseEntity<?> kiemTraGia(@PathVariable Integer id) {
        return ResponseEntity.ok(banHangService.kiemTraGiaThayDoi(id));
    }

    @GetMapping("/san-pham/tim-theo-ma")
    @ResponseBody
    public ResponseEntity<?> timSanPhamTheoMa(@RequestParam String maCtsp) {
        try {
            return ResponseEntity.ok(banHangService.timSanPhamTheoMa(maCtsp));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PostMapping("/khach-hang/{id}/dia-chi")
    @ResponseBody
    public ResponseEntity<?> themDiaChi(@PathVariable("id") Integer idKhachHang,
                                        @RequestBody DiaChiRequest request) {
        try {
            return ResponseEntity.ok(banHangService.themDiaChiKhachHang(idKhachHang, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/dia-chi/{id}")
    @ResponseBody
    public ResponseEntity<?> suaDiaChi(@PathVariable("id") Integer idDiaChi,
                                       @RequestBody DiaChiRequest request) {
        try {
            return ResponseEntity.ok(banHangService.capNhatDiaChiKhachHang(idDiaChi, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/dia-chi-giao-hang")
    @ResponseBody
    public ResponseEntity<?> capNhatDiaChiGiaoHang(@PathVariable("id") Integer idHoaDon,
                                                   @RequestParam Integer idDiaChi) {
        try {
            return ResponseEntity.ok(banHangService.capNhatDiaChiGiaoHang(idHoaDon, idDiaChi));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}