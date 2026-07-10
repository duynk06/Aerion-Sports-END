package com.example.AerionSports_BE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietHoaDonRequest {

    private Integer id;

    private String maSanPham;

    private String tenSanPham;

    private String mauSac;

    private String trongLuong;

    private Integer soLuong;

    private BigDecimal donGia;

    private BigDecimal thanhTien;
}
