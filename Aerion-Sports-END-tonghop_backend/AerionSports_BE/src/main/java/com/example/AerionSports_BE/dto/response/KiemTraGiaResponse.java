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
public class KiemTraGiaResponse {
    private Integer idChiTiet;        // id của chi_tiet_hoa_don
    private String maCtsp;            // mã chi tiết sản phẩm
    private BigDecimal giaCu;         // giá đang lưu trong hóa đơn
    private BigDecimal giaMoi;        // giá hiện tại của sản phẩm
    private boolean daThayDoi;
    // KiemTraGiaResponse.java — thêm field
    private Boolean trangThai; // true = hoạt động, false = ngừng
}