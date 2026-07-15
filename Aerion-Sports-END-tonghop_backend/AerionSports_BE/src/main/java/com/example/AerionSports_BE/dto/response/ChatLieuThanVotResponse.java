package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatLieuThanVotResponse {
    private Integer id;
    private String maChatLieuThanVot;
    private String tenChatLieuThanVot;
    private Integer trangThai;
}
