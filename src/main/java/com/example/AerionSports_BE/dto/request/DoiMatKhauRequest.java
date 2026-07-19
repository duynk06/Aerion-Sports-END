package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DoiMatKhauRequest {
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String matKhauCu;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    private String matKhauMoi;

    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String xacNhanMatKhau;
}
