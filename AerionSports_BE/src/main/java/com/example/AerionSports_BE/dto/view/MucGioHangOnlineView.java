package com.example.AerionSports_BE.dto.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MucGioHangOnlineView {
    // Dong du lieu da san sang de render tren trang gio hang.
    private Integer productId;
    private Integer variantId;
    private String tenSanPham;
    private String thuongHieu;
    private String mauSac;
    private String trongLuong;
    private String hinhAnh;
    private BigDecimal donGia;
    private BigDecimal donGiaGoc;
    private int soLuong;
    private int tonKho;
    private BigDecimal thanhTien;
}
