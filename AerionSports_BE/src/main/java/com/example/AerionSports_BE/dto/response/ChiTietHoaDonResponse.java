package com.example.AerionSports_BE.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ChiTietHoaDonResponse {

    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String mauSac;
    private String trongLuong;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private String anh;
    private BigDecimal giaGoc;
    private String tenDotGiamGia;

    // ✅ Constructor đầy đủ 11 tham số — dùng cho chỗ mới (map từ ChiTietHoaDonProjection)
    public ChiTietHoaDonResponse(Integer id, String maSanPham, String tenSanPham, String mauSac,
                                 String trongLuong, Integer soLuong, BigDecimal donGia,
                                 BigDecimal thanhTien, String anh, BigDecimal giaGoc, String tenDotGiamGia) {
        this.id = id;
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.mauSac = mauSac;
        this.trongLuong = trongLuong;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.anh = anh;
        this.giaGoc = giaGoc;
        this.tenDotGiamGia = tenDotGiamGia;
    }

    // ✅ Constructor cũ 10 tham số — giữ lại để BanHangOnlineChiTietHoaDonRepository (JPQL "new ChiTietHoaDonResponse(...)")
    // không bị vỡ; tenDotGiamGia mặc định null cho các chỗ chưa cần hiển thị đợt giảm giá
    public ChiTietHoaDonResponse(Integer id, String maSanPham, String tenSanPham, String mauSac,
                                 String trongLuong, Integer soLuong, BigDecimal donGia,
                                 BigDecimal thanhTien, String anh, BigDecimal giaGoc) {
        this(id, maSanPham, tenSanPham, mauSac, trongLuong, soLuong, donGia, thanhTien, anh, giaGoc, null);
    }
}