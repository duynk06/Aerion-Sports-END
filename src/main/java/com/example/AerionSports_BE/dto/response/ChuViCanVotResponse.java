package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChuViCanVotResponse {
    private Integer id;
    private String maChuViCanVot;
    private String tenChuViCanVot;
    private Integer trangThai;
}
