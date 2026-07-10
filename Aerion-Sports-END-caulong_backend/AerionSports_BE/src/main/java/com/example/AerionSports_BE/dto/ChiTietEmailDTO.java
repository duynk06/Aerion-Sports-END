package com.example.AerionSports_BE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

// ChiTietEmailDTO.java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietEmailDTO {
    private String tenSanPham;
    private String mauSac;
    private String trongLuong;
    private Integer soLuong;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
}