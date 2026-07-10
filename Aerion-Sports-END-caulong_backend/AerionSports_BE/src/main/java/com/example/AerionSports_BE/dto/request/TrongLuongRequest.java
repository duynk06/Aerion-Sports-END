package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrongLuongRequest {
    @NotBlank(message = "Mã trọng lượng không được để trống")
    @Size(max = 50, message = "Mã không được quá 50 ký tự")
    private String maTrongLuong;

    @NotBlank(message = "Tên trọng lượng (Ví dụ: 3U, 4U) không được để trống")
    @Size(max = 50, message = "Tên không được quá 50 ký tự")
    private String tenTrongLuong;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
