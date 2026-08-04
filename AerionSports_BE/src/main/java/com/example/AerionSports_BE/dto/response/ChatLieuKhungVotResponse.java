package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatLieuKhungVotResponse {
    private Integer id;
    private String maChatLieuKhungVot;
    private String tenChatLieuKhungVot;
    private Integer trangThai;
}
