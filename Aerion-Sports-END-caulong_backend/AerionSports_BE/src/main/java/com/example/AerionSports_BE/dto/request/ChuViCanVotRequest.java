package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChuViCanVotRequest {
    @NotBlank(message = "Mã chu vi cán vợt không được để trống")
    private String maChuViCanVot;

    @NotBlank(message = "Tên chu vi cán vợt không được để trống")
    private String tenChuViCanVot;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;
}
