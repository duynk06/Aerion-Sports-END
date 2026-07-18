package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MauSacRequest {
    @NotBlank(message = "Mã màu sắc không được để trống")
    @Size(max = 50, message = "Mã màu không được quá 50 ký tự")
    private String maMauSac;

    @NotBlank(message = "Tên màu sắc không được để trống")
    private String tenMauSac;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
