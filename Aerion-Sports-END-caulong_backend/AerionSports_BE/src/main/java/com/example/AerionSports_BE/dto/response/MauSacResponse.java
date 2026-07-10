package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MauSacResponse {
    private Integer id;
    private String maMauSac;
    private String tenMauSac;
    private Integer trangThai;
}
