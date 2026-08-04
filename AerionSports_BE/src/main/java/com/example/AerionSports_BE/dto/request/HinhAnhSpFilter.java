package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HinhAnhSpFilter {
    private Integer idSanPhamChiTiet; // Tìm tất cả ảnh của 1 biến thể cụ thể
    private Integer trangThai;
}
