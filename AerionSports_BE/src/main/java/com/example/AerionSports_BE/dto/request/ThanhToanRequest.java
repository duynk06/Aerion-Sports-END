package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

// ThanhToanRequest.java
@Getter
@Setter
public class ThanhToanRequest {
    private Integer idHoaDon;
    private Integer idHinhThucThanhToan; // 1=Tiền mặt, 2=Chuyển khoản
    private BigDecimal soTienKhachDua;
    private String ghiChu;
}