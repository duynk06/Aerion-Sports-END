package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThongKeCardResponse {
    private BigDecimal doanhThu;
    private Integer soSanPhamDaBan;
    private Integer tongDonHang;
    private Integer donHoanThanh;
    private Integer donHuy;
    private Integer donDangXuLy;
}
