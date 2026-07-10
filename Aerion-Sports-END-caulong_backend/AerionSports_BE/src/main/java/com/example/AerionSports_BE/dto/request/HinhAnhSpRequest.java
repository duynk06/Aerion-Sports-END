package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HinhAnhSpRequest {
    @NotNull(message = "Id chi tiết sản phẩm không được trống")
    private Integer idSanPhamChiTiet;

    private Boolean laAnhChinh = false;

    @NotBlank(message = "Đường dẫn ảnh không được trống")
    private String duongDanAnh;

    @NotNull(message = "Trạng thái không được trống")
    private Integer trangThai;
}
