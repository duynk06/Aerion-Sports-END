package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ThuongHieuRequest {
    @NotBlank(message = "Mã thương hiệu không được để trống")
    @Size(max = 50, message = "Mã thương hiệu không được quá 50 ký tự")
    private String maThuongHieu;

    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 255, message = "Tên thương hiệu không được quá 255 ký tự")
    private String tenThuongHieu;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
