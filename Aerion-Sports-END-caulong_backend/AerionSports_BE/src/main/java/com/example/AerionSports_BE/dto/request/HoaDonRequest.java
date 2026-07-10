package com.example.AerionSports_BE.dto.request;

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
public class HoaDonRequest {
    private Integer id;

    private String maHoaDon;

    private String loaiHoaDon;

    private BigDecimal tongTienThanhToan;

    private Integer trangThai;

    private LocalDateTime ngayTao;

    // Khách hàng
    private Integer idKhachHang;
    private String tenKhachHang;
    private String sdtKhachHang;

    // Nhân viên
    private Integer idNhanVien;
    private String tenNhanVien;
}
