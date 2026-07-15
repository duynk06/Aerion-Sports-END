package com.example.AerionSports_BE.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class SanPhamPosDTO {
    private Integer id;          // id của chi_tiet_san_pham
    private String ma;           // maCtsp
    private String ten;          // tên sản phẩm cha
    private String mauSac;       // tên màu sắc
    private String trongLuong;   // tên trọng lượng
    private BigDecimal gia;      // giaBan
    private Integer soLuongTon;  // số lượng tồn
    private String anh;          // đường dẫn ảnh chính
    private BigDecimal giaGoc;
}
