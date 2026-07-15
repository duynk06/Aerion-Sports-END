package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatLieuThanVotFilter {
    private String keyword;
    private Integer trangThai;
    private int page = 0;
    private int size = 10;
}
