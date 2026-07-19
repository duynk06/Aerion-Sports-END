package com.example.AerionSports_BE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatLieuThanVotRequest {
    @NotBlank(message = "Mã chất liệu thân không được để trống")
    @Size(max = 50, message = "Mã không được quá 50 ký tự")
    private String maChatLieuThanVot;

    @NotBlank(message = "Tên chất liệu thân không được để trống")
    private String tenChatLieuThanVot;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

}
