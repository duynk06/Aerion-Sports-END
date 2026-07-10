package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DanhMucRequest {
    @NotBlank(message = "Mã danh mục không được để trống")
    @Size(max = 50, message = "Mã không được quá 50 ký tự")
    private String maDanhMuc;

    @NotBlank(message = "Tên danh mục không được để trống")
    private String tenDanhMuc;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
