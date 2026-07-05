package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.HoaDon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BanHangResponse {

    private Integer id;

    private String maHoaDon;

    // khách hàng
    private Integer idKhachHang;
    private String tenKhachHang;
    private String sdt;
    // BanHangResponse.java
    private String diaChiKhachHang;
    private String tinhThanhKhachHang;

    // nhân viên
    private Integer idNhanVien;
    private String tenNhanVien;

    // tiền
    private BigDecimal tongTienHang;
    private BigDecimal tienGiam;
    private BigDecimal tongTienThanhToan;
    private Integer loaiHoaDon;
    private BigDecimal tienVanChuyen;
    private String maPhieuGiamGia;
    private String tenPhieuGiamGia;
    private String ghiChu;

    private Integer trangThai;

    private List<ChiTietHoaDonResponse> sanPham;
// BanHangResponse.java

// Thêm import cần thiết

    public BanHangResponse(HoaDon hoaDon) {

        this.id = hoaDon.getId();
        this.maHoaDon = hoaDon.getMaHoaDon();
        this.tongTienHang = hoaDon.getTongTienHang();
        this.tienGiam = hoaDon.getTienGiam();
        this.tongTienThanhToan = hoaDon.getTongTienThanhToan();
        this.tienVanChuyen = hoaDon.getTienVanChuyen();
        this.loaiHoaDon = hoaDon.getLoaiHoaDon();
        this.ghiChu = hoaDon.getGhiChu();
        this.trangThai = hoaDon.getTrangThai();

        // BanHangResponse.java — constructor, sửa phần khách hàng
        if (hoaDon.getKhachHang() != null) {
            this.idKhachHang = hoaDon.getKhachHang().getId();
            this.tenKhachHang = hoaDon.getKhachHang().getHoTen();
            this.sdt = hoaDon.getKhachHang().getSdt();
        }

        if (hoaDon.getNhanVien() != null) {
            this.idNhanVien = hoaDon.getNhanVien().getId();
            this.tenNhanVien = hoaDon.getNhanVien().getTenNv();
        }

        if (hoaDon.getPhieuGiamGia() != null) {
            this.maPhieuGiamGia = hoaDon.getPhieuGiamGia().getMaPhieuGiamGia();
            this.tenPhieuGiamGia = hoaDon.getPhieuGiamGia().getTenPhieuGiamGia();
        }

        if (hoaDon.getChiTietHoaDons() != null && !hoaDon.getChiTietHoaDons().isEmpty()) {
            this.sanPham = hoaDon.getChiTietHoaDons().stream()
                    .map(cthd -> {
                        ChiTietSanPham ctsp = cthd.getChiTietSanPham();
                        return new ChiTietHoaDonResponse(
                                cthd.getId(),
                                ctsp.getIdSanPham().getMaSanPham(),   // ✅ Sửa từ getMaCtsp() → getMaSanPham()
                                ctsp.getIdSanPham().getTenSanPham(),
                                ctsp.getIdMauSac() != null ? ctsp.getIdMauSac().getTenMauSac() : "",
                                ctsp.getIdTrongLuong() != null ? ctsp.getIdTrongLuong().getTenTrongLuong() : "",
                                cthd.getSoLuong(),
                                cthd.getDonGia(),
                                cthd.getThanhTien()
                        );
                    })
                    .collect(Collectors.toList());
        } else {
            this.sanPham = new ArrayList<>();
        }
    }
}