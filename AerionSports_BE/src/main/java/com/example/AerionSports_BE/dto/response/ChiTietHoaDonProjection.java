package com.example.AerionSports_BE.dto.response;

import java.math.BigDecimal;

public interface ChiTietHoaDonProjection {
    Integer getId();
    String getMaSanPham();
    String getTenSanPham();
    String getMauSac();
    String getTrongLuong();
    Integer getSoLuong();
    BigDecimal getDonGia();
    BigDecimal getThanhTien();
    String getAnh();
    BigDecimal getGiaGoc();
    String getTenDotGiamGia();
}