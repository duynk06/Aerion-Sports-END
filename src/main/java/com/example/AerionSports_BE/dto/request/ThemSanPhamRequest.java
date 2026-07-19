package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ThemSanPhamRequest {
    private Integer idHoaDon;
    private Integer idSanPhamChiTiet; // ID của biến thể sản phẩm (có màu, size cụ thể)
    private Integer soLuong;
    private BigDecimal donGia;
}