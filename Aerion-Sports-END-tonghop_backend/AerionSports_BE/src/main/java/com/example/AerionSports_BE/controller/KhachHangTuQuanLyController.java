package com.example.AerionSports_BE.controller;


import com.example.AerionSports_BE.dto.CapNhatHoSoRequest;
import com.example.AerionSports_BE.dto.DiaChiRequest;
import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.DiaChiKhachHangRepository;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/khach-hang-tu-quan-ly")
@CrossOrigin("*")
public class KhachHangTuQuanLyController {

    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private DiaChiKhachHangRepository diaChiRepository;
    @Autowired private TaiKhoanRepository taiKhoanRepository;

    // 🌟 Hàm dùng chung: xác định khách hàng đang đăng nhập là ai, dựa vào token
    private KhachHang layKhachHangDangDangNhap() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("DEBUG: Username từ token: " + username); // Kiểm tra xem token có username không

        TaiKhoan tk = taiKhoanRepository.findByTenDangNhapAndTrangThai(username, 1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với username: " + username));

        System.out.println("DEBUG: ID chu tai khoan: " + tk.getIdChuTaiKhoan()); // Kiểm tra ID này

        return khachHangRepository.findById(tk.getIdChuTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với ID: " + tk.getIdChuTaiKhoan()));
    }

    // ================== HỒ SƠ CÁ NHÂN ==================
    @GetMapping("/ho-so")
    public ResponseEntity<?> xemHoSo() {
        try {
            KhachHang kh = layKhachHangDangDangNhap();

            // Sử dụng Map thủ công để tránh lỗi null của Map.of()
            Map<String, Object> response = new HashMap<>();
            response.put("id", kh.getId());
            response.put("maKhachHang", kh.getMaKhachHang());
            response.put("hoTen", kh.getHoTen());
            response.put("sdt", kh.getSdt());
            response.put("email", kh.getEmail() != null ? kh.getEmail() : "");
            response.put("ngaySinh", kh.getNgaySinh() != null ? kh.getNgaySinh().toString() : "");
            response.put("gioiTinh", kh.getGioiTinh() != null ? kh.getGioiTinh() : 0); // Xử lý null
            response.put("avatar", kh.getAvatar() != null ? kh.getAvatar() : "");
            response.put("diemTichLuy", kh.getDiemTichLuy() != null ? kh.getDiemTichLuy() : 0); // Xử lý null

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Lỗi: " + e.getMessage()));
        }
    }

    @PutMapping("/ho-so")
    public ResponseEntity<?> capNhatHoSo(@RequestBody CapNhatHoSoRequest req) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();

            if (req.getHoTen() == null || req.getHoTen().trim().length() < 3) {
                return ResponseEntity.badRequest().body(Map.of("message", "Họ tên không hợp lệ!"));
            }

            // Kiểm tra trùng email với khách hàng khác (không tính chính mình)
            if (req.getEmail() != null && !req.getEmail().isBlank()
                    && !req.getEmail().trim().equalsIgnoreCase(kh.getEmail())
                    && khachHangRepository.existsByEmailAndIdNot(req.getEmail().trim(), kh.getId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email đã được sử dụng bởi tài khoản khác!"));
            }

            kh.setHoTen(req.getHoTen().trim());
            kh.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
            kh.setGioiTinh(req.getGioiTinh());
            if (req.getNgaySinh() != null && !req.getNgaySinh().isBlank()) {
                kh.setNgaySinh(LocalDate.parse(req.getNgaySinh()));
            }
            kh.setNgayCapNhat(LocalDateTime.now());

            khachHangRepository.save(kh);
            return ResponseEntity.ok(Map.of("message", "Cập nhật hồ sơ thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    // ================== SỔ ĐỊA CHỈ ==================

    @GetMapping("/dia-chi")
    public ResponseEntity<?> danhSachDiaChi() {
        try {
            KhachHang kh = layKhachHangDangDangNhap();
            List<DiaChiKhachHang> ds = diaChiRepository.findByKhachHangId(kh.getId());   // 🌟 SỬA
            return ResponseEntity.ok(ds);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/dia-chi")
    public ResponseEntity<?> themDiaChi(@RequestBody DiaChiRequest req) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();

            if (req.getNguoiNhan() == null || req.getNguoiNhan().isBlank()
                    || req.getSdt() == null || req.getSdt().isBlank()
                    || req.getTinhThanh() == null || req.getTinhThanh().isBlank()
                    || req.getDiaChiChiTiet() == null || req.getDiaChiChiTiet().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng điền đầy đủ thông tin địa chỉ!"));
            }

            List<DiaChiKhachHang> dsHienTai = diaChiRepository.findByKhachHangId(kh.getId());   // 🌟 SỬA
            boolean laDauTien = dsHienTai.isEmpty();

            boolean macDinh = Boolean.TRUE.equals(req.getMacDinh()) || laDauTien;
            if (macDinh) {
                diaChiRepository.boMacDinhTheoKhachHang(kh.getId());   // 🌟 dùng luôn method có sẵn, gọn hơn vòng lặp cũ
            }

            DiaChiKhachHang moi = DiaChiKhachHang.builder()
                    .khachHang(kh)
                    .nguoiNhan(req.getNguoiNhan().trim())
                    .sdt(req.getSdt().trim())
                    .tinhThanh(req.getTinhThanh())
                    .phuongXa(req.getPhuongXa())
                    .diaChiChiTiet(req.getDiaChiChiTiet().trim())
                    .macDinh(macDinh)
                    .ngayTao(LocalDateTime.now())
                    .ngayCapNhat(LocalDateTime.now())
                    .build();

            diaChiRepository.save(moi);
            return ResponseEntity.ok(Map.of("message", "Thêm địa chỉ thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Thêm địa chỉ thất bại: " + e.getMessage()));
        }
    }

    @PutMapping("/dia-chi/{id}")
    public ResponseEntity<?> suaDiaChi(@PathVariable Integer id, @RequestBody DiaChiRequest req) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();

            DiaChiKhachHang addr = diaChiRepository.findByIdAndKhachHangId(id, kh.getId())   // 🌟 SỬA
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ hoặc bạn không có quyền sửa!"));

            if (Boolean.TRUE.equals(req.getMacDinh()) && !addr.getMacDinh()) {
                diaChiRepository.boMacDinhTheoKhachHang(kh.getId());
            }

            addr.setNguoiNhan(req.getNguoiNhan().trim());
            addr.setSdt(req.getSdt().trim());
            addr.setTinhThanh(req.getTinhThanh());
            addr.setPhuongXa(req.getPhuongXa());
            addr.setDiaChiChiTiet(req.getDiaChiChiTiet().trim());
            if (req.getMacDinh() != null) addr.setMacDinh(req.getMacDinh());
            addr.setNgayCapNhat(LocalDateTime.now());

            diaChiRepository.save(addr);
            return ResponseEntity.ok(Map.of("message", "Cập nhật địa chỉ thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    @DeleteMapping("/dia-chi/{id}")
    public ResponseEntity<?> xoaDiaChi(@PathVariable Integer id) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();

            DiaChiKhachHang addr = diaChiRepository.findByIdAndKhachHangId(id, kh.getId())   // 🌟 SỬA
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ hoặc bạn không có quyền xoá!"));

            boolean laMacDinh = addr.getMacDinh();
            diaChiRepository.delete(addr);

            if (laMacDinh) {
                List<DiaChiKhachHang> conLai = diaChiRepository.findByKhachHangId(kh.getId());   // 🌟 SỬA
                if (!conLai.isEmpty()) {
                    DiaChiKhachHang moiMacDinh = conLai.get(0);
                    moiMacDinh.setMacDinh(true);
                    diaChiRepository.save(moiMacDinh);
                }
            }

            return ResponseEntity.ok(Map.of("message", "Xoá địa chỉ thành công!"));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Xoá thất bại: " + e.getMessage()));
        }
    }
}