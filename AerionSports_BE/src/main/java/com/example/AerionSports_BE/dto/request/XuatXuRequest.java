package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class XuatXuRequest {
    @NotBlank(message = "Mã xuất xứ không được để trống")
    @Size(max = 50, message = "Mã không quá 50 ký tự")
    private String maXuatXu;

    @NotBlank(message = "Tên quốc gia xuất xứ không được để trống")
    private String tenXuatXu;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
