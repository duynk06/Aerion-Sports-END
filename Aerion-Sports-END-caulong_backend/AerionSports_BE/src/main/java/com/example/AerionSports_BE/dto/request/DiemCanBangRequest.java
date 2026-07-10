package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DiemCanBangRequest {

    @NotBlank(message = "Mã điểm cân bằng không được để trống")
    @Size(max = 50, message = "Mã không được quá 50 ký tự")
    private String maDiemCanBang;

    @NotBlank(message = "Tên điểm cân bằng không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String tenDiemCanBang;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
