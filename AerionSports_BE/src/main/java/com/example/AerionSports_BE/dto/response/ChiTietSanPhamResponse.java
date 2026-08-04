package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietSanPhamResponse {
    private Integer id;
    private Integer idSanPham;
    private String maSanPham;
    private String tenSanPham;
    private String maCtsp;

    private Integer idMauSac;
    private String tenMauSac;

    private Integer idTrongLuong;
    private String tenTrongLuong;

    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer soLuong;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant ngayCapNhat;
    private String hinhAnh;

    private BigDecimal giaDaGiam;
    private BigDecimal phanTramGiam;
}