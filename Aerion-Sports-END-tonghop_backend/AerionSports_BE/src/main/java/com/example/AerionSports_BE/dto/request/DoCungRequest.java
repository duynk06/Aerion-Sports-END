package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DoCungRequest {
    @NotBlank(message = "Mã độ cứng không được để trống")
    @Size(max = 50, message = "Mã không được quá 50 ký tự")
    private String maDoCung;

    @NotBlank(message = "Tên chỉ số độ cứng không được để trống")
    @Size(max = 100, message = "Tên độ cứng không vượt quá 100 ký tự")
    private String tenDoCung;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
