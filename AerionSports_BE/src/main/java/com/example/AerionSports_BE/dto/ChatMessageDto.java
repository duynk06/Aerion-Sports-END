package com.example.AerionSports_BE.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatMessageDto {
    private Long roomId;
    private Long khachHangId;
    private String nguoiGui;
    private String noiDung;
    private Long nguoiGuiId;
    private String type;

    public ChatMessageDto(Long khachHangId, String nguoiGui, String noiDung, Long nguoiGuiId) {
        this.khachHangId = khachHangId;
        this.nguoiGui = nguoiGui;
        this.noiDung = noiDung;
        this.nguoiGuiId = nguoiGuiId;
    }

    public ChatMessageDto(Long roomId, Long khachHangId, String nguoiGui, String noiDung, Long nguoiGuiId) {
        this.roomId = roomId;
        this.khachHangId = khachHangId;
        this.nguoiGui = nguoiGui;
        this.noiDung = noiDung;
        this.nguoiGuiId = nguoiGuiId;
    }
}