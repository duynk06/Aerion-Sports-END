package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.DatHangRequest;
import com.example.AerionSports_BE.dto.SanPhamDatHangItem;
import com.example.AerionSports_BE.entity.*;
import com.example.AerionSports_BE.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/khach-hang-tu-quan-ly/don-hang")
@CrossOrigin("*")
public class DonHangController {

    @Autowired private HoaDonRepository hoaDonRepository;
    @Autowired private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private DiaChiKhachHangRepository diaChiRepository;
    @Autowired private KhachHangRepository khachHangRepository;
    @Autowired private TaiKhoanRepository taiKhoanRepository;


    // Trùng logic với KhachHangTuQuanLyController — sau này nên tách ra 1 @Component dùng chung
    private KhachHang layKhachHangDangDangNhap() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhapAndTrangThai(username, 1)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản hợp lệ!"));
        if (!"KHACH_HANG".equals(tk.getLoaiTaiKhoan())) {
            throw new RuntimeException("Tài khoản này không phải khách hàng!");
        }
        return khachHangRepository.findById(tk.getIdChuTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ khách hàng tương ứng!"));
    }

    // ================== ĐẶT HÀNG ==================
    @PostMapping
    @Transactional
    public ResponseEntity<?> datHang(@RequestBody DatHangRequest req) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();

            if (req.getItems() == null || req.getItems().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Giỏ hàng trống!"));
            }

            // 🌟 Xác định thông tin người nhận
            String tenNguoiNhan, sdtNguoiNhan, diaChiNhan;
            if (req.getIdDiaChi() != null) {
                // Bắt buộc kiểm tra địa chỉ này thuộc về đúng khách hàng đang đăng nhập (chống IDOR)
                DiaChiKhachHang dc = diaChiRepository.findByIdAndKhachHangId(req.getIdDiaChi(), kh.getId())
                        .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không hợp lệ!"));
                tenNguoiNhan = dc.getNguoiNhan();
                sdtNguoiNhan = dc.getSdt();
                diaChiNhan = (dc.getDiaChiChiTiet() != null ? dc.getDiaChiChiTiet() + ", " : "")
                        + (dc.getPhuongXa() != null ? dc.getPhuongXa() + ", " : "")
                        + (dc.getTinhThanh() != null ? dc.getTinhThanh() : "");
            } else {
                if (req.getTenNguoiNhan() == null || req.getTenNguoiNhan().isBlank()
                        || req.getSdtNguoiNhan() == null || req.getSdtNguoiNhan().isBlank()
                        || req.getDiaChiNhan() == null || req.getDiaChiNhan().isBlank()) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn địa chỉ hoặc nhập đầy đủ thông tin nhận hàng!"));
                }
                tenNguoiNhan = req.getTenNguoiNhan().trim();
                sdtNguoiNhan = req.getSdtNguoiNhan().trim();
                diaChiNhan = req.getDiaChiNhan().trim();
            }

            // 🌟 Kiểm tra tồn kho + tính tổng tiền TRƯỚC khi tạo bất cứ gì (fail sớm, không tạo đơn rác)
            BigDecimal tongTienHang = BigDecimal.ZERO;
            List<ChiTietSanPham> danhSachCtsp = new java.util.ArrayList<>();

            for (SanPhamDatHangItem item : req.getItems()) {
                if (item.getSoLuong() == null || item.getSoLuong() <= 0) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Số lượng sản phẩm không hợp lệ!"));
                }
                ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(item.getIdChiTietSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại (id=" + item.getIdChiTietSanPham() + ")"));

                if (ctsp.getTrangThai() == null || ctsp.getTrangThai() != 1) {
                    return ResponseEntity.badRequest().body(Map.of("message", "Sản phẩm '" + ctsp.getMaCtsp() + "' hiện đã ngừng bán!"));
                }
                if (ctsp.getSoLuong() == null || ctsp.getSoLuong() < item.getSoLuong()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "message", "Sản phẩm '" + ctsp.getMaCtsp() + "' chỉ còn " + (ctsp.getSoLuong() == null ? 0 : ctsp.getSoLuong()) + " sản phẩm trong kho!"
                    ));
                }

                tongTienHang = tongTienHang.add(ctsp.getGiaBan().multiply(BigDecimal.valueOf(item.getSoLuong())));
                danhSachCtsp.add(ctsp);
            }

            // 🌟 Phí ship cố định tạm thời — có thể thay bằng logic tính theo khu vực sau
            BigDecimal tienVanChuyen = BigDecimal.valueOf(30000);
            BigDecimal tienGiam = BigDecimal.ZERO; // TODO: áp dụng PhieuGiamGia khi có đủ field
            BigDecimal tongTienThanhToan = tongTienHang.subtract(tienGiam).add(tienVanChuyen);

            // 🌟 Tạo hoá đơn
            HoaDon hd = new HoaDon();
            hd.setKhachHang(kh);
            hd.setMaHoaDon("HD" + System.currentTimeMillis());
            hd.setLoaiHoaDon(2); // 2 = đơn online (giả định — chỉnh lại nếu hệ thống bạn dùng số khác)
            hd.setTenNguoiNhan(tenNguoiNhan);
            hd.setSdtNguoiNhan(sdtNguoiNhan);
            hd.setDiaChiNhan(diaChiNhan);
            hd.setGhiChu(req.getGhiChu());
            hd.setTongTienHang(tongTienHang);
            hd.setTienGiam(tienGiam);
            hd.setTienVanChuyen(tienVanChuyen);
            hd.setTongTienThanhToan(tongTienThanhToan);
            hd.setTrangThai(1); // 1 = Chờ xác nhận
            hd.setNgayTao(LocalDateTime.now());
            hd.setNgayCapNhat(LocalDateTime.now());
            hd.setNguoiCapNhat(kh.getHoTen());
            hoaDonRepository.save(hd);

            // 🌟 Tạo chi tiết hoá đơn + trừ tồn kho
            for (int i = 0; i < req.getItems().size(); i++) {
                SanPhamDatHangItem item = req.getItems().get(i);
                ChiTietSanPham ctsp = danhSachCtsp.get(i);

                ChiTietHoaDon ct = new ChiTietHoaDon();
                ct.setHoaDon(hd);
                ct.setChiTietSanPham(ctsp);
                ct.setMaHoaDonChiTiet("HDCT" + System.currentTimeMillis() + "-" + i);
                ct.setSoLuong(item.getSoLuong());
                ct.setDonGia(ctsp.getGiaBan());
                ct.setThanhTien(ctsp.getGiaBan().multiply(BigDecimal.valueOf(item.getSoLuong())));
                ct.setNgayTao(LocalDateTime.now());
                ct.setNgayCapNhat(LocalDateTime.now());
                ct.setTrangThai(1);
                chiTietHoaDonRepository.save(ct);

                // Trừ tồn kho
                ctsp.setSoLuong(ctsp.getSoLuong() - item.getSoLuong());
                chiTietSanPhamRepository.save(ctsp);
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Đặt hàng thành công!",
                    "maHoaDon", hd.getMaHoaDon(),
                    "tongTienThanhToan", hd.getTongTienThanhToan()
            ));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Đặt hàng thất bại: " + e.getMessage()));
        }
    }

    // ================== DANH SÁCH ĐƠN HÀNG CỦA TÔI ==================
    @GetMapping
    public ResponseEntity<?> danhSachDonHang() {
        try {
            KhachHang kh = layKhachHangDangDangNhap();
            List<HoaDon> ds = hoaDonRepository.findByKhachHang_IdOrderByIdDesc(kh.getId());
            return ResponseEntity.ok(ds);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // ================== CHI TIẾT 1 ĐƠN HÀNG (có kiểm tra chủ sở hữu) ==================
    @GetMapping("/{id}")
    public ResponseEntity<?> chiTietDonHang(@PathVariable Integer id) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();
            HoaDon hd = hoaDonRepository.findByIdAndKhachHang_Id(id, kh.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoặc bạn không có quyền xem!"));
            // Dùng query có FETCH sẵn để lấy đủ chi tiết sản phẩm trong 1 lần truy vấn
            HoaDon hdChiTiet = hoaDonRepository.findByIdWithChiTiet(hd.getId());
            return ResponseEntity.ok(hdChiTiet);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // ================== HUỶ ĐƠN (chỉ khi còn ở trạng thái Chờ xác nhận) ==================
    @PutMapping("/{id}/huy")
    public ResponseEntity<?> huyDonHang(@PathVariable Integer id) {
        try {
            KhachHang kh = layKhachHangDangDangNhap();
            HoaDon hd = hoaDonRepository.findByIdAndKhachHang_Id(id, kh.getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng hoặc bạn không có quyền huỷ!"));

            if (hd.getTrangThai() == null || hd.getTrangThai() != 1) {
                return ResponseEntity.badRequest().body(Map.of("message", "Chỉ có thể huỷ đơn khi đang ở trạng thái 'Chờ xác nhận'!"));
            }

            hd.setTrangThai(0); // 0 = Đã huỷ
            hd.setNgayCapNhat(LocalDateTime.now());
            hoaDonRepository.save(hd);

            // Hoàn lại tồn kho
            for (ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                ChiTietSanPham ctsp = ct.getChiTietSanPham();
                ctsp.setSoLuong(ctsp.getSoLuong() + ct.getSoLuong());
                chiTietSanPhamRepository.save(ctsp);
            }

            return ResponseEntity.ok(Map.of("message", "Huỷ đơn hàng thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Huỷ đơn thất bại: " + e.getMessage()));
        }
    }
}
