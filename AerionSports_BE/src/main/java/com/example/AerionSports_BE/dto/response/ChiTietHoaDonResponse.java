package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.*;
import jakarta.persistence.*;
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
}
