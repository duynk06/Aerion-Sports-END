package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.HoaDon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonResponse {

    private Integer id;
    private String maHoaDon;
    private Integer loaiHoaDon;
    private BigDecimal tongTienThanhToan;
    private Integer trangThai;
    private LocalDateTime ngayTao;

    private BigDecimal tienGiam;
    private BigDecimal tienVanChuyen;
    private BigDecimal tongTienHang;
    private String ghiChu;

    // --- THÔNG TIN GIAO HÀNG (Lấy từ Hóa Đơn - Snapshot) ---
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiNhan;

    // --- THÔNG TIN TÀI KHOẢN KHÁCH HÀNG (Nếu có) ---
    private Integer idKhachHang;
    private String tenKhachHang;
    private String sdtKhachHang;
    private String email;
    private String diaChiMacDinhKhachHang;

    // --- THÔNG TIN NHÂN VIÊN ---
    private Integer idNhanVien;
    private String tenNv;

    //--THÔNG TIN PHIẾU GIẢM GIÁ
    private String maPhieuGiamGia;
    private String tenPhieuGiamGia;
    private String loaiPhieuGiamGia;      // ← THÊM: "PHAN_TRAM", "VAN_CHUYEN", hoặc mặc định (số tiền cố định)
    private BigDecimal giaTriGiamGoc;

    public HoaDonResponse(HoaDon hoaDon) {
        this.id = hoaDon.getId();
        this.maHoaDon = hoaDon.getMaHoaDon();
        this.loaiHoaDon = hoaDon.getLoaiHoaDon();
        this.tongTienThanhToan = hoaDon.getTongTienThanhToan();
        this.trangThai = hoaDon.getTrangThai();
        this.ngayTao = hoaDon.getNgayTao();
        this.tienGiam = hoaDon.getTienGiam();
        this.tienVanChuyen = hoaDon.getTienVanChuyen();
        this.tongTienHang = hoaDon.getTongTienHang();
        this.ghiChu = hoaDon.getGhiChu();

        // Gán trực tiếp thông tin người nhận từ Hóa Đơn
        this.tenNguoiNhan = hoaDon.getTenNguoiNhan();
        this.sdtNguoiNhan = hoaDon.getSdtNguoiNhan();
        this.diaChiNhan = hoaDon.getDiaChiNhan();

        // Chỉ lấy ID và Email từ bảng Khách Hàng (nếu đơn này do user có tài khoản đặt)
        if (hoaDon.getKhachHang() != null) {
            this.idKhachHang = hoaDon.getKhachHang().getId();
            this.tenKhachHang = hoaDon.getKhachHang().getHoTen();
            this.sdtKhachHang = hoaDon.getKhachHang().getSdt();
            this.email = hoaDon.getKhachHang().getEmail();
            if (hoaDon.getKhachHang().getAddresses() != null && !hoaDon.getKhachHang().getAddresses().isEmpty()) {
                this.diaChiMacDinhKhachHang = hoaDon.getKhachHang().getAddresses().stream()
                        // Dùng Boolean.TRUE.equals() để tránh lỗi NullPointerException nếu macDinh bị null
                        .filter(dc -> Boolean.TRUE.equals(dc.getMacDinh()))
                        // Nối chuỗi tạo thành địa chỉ hoàn chỉnh
                        .map(dc -> dc.getDiaChiChiTiet() + ", " + dc.getPhuongXa() + ", " + dc.getTinhThanh())
                        .findFirst() // Lấy cái đầu tiên tìm được
                        .orElse("Khách hàng chưa thiết lập địa chỉ mặc định"); // Nếu có địa chỉ nhưng không có cái nào mặc định
            } else {
                this.diaChiMacDinhKhachHang = "";
            }
        }

        if (hoaDon.getNhanVien() != null) {
            this.idNhanVien = hoaDon.getNhanVien().getId();
            this.tenNv = hoaDon.getNhanVien().getTenNv();
        }
        if(hoaDon.getPhieuGiamGia() != null) {
            this.tenPhieuGiamGia = hoaDon.getPhieuGiamGia().getTenPhieuGiamGia();
            this.maPhieuGiamGia = hoaDon.getPhieuGiamGia().getMaPhieuGiamGia();
            this.loaiPhieuGiamGia = hoaDon.getPhieuGiamGia().getLoaiPhieuGiamGia();   // ← THÊM
            this.giaTriGiamGoc = hoaDon.getPhieuGiamGia().getGiaTriGiam();           // ← THÊM
        }
    }

    // Trả về class CSS tương ứng
    public String getTrangThaiClass() {
        if (this.trangThai == null) return "";
        return switch (this.trangThai) {
            case 0 -> "status-wait-confirm";
            case 1 -> "status-confirmed";
            case 2 -> "status-wait-delivery";
            case 3 -> "status-delivering";
            case 4 -> "status-delivered";
            case 5 -> "status-completed";
            case 6 -> "status-cancel";
            default -> "";
        };
    }

    public String getTrangThaiName() {
        if (this.trangThai == null) return "Không xác định";
        return switch (this.trangThai) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Chờ giao hàng";
            case 3 -> "Đang giao hàng";
            case 4 -> "Đã giao hàng";
            case 5 -> "Đã hoàn thành";
            case 6 -> "Đã hủy";
            default -> "Không xác định";
        };
    }
}