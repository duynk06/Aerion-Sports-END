package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DoCungResponse {
    private Integer id;
    private String maDoCung;
    private String tenDoCung;
    private Integer trangThai;
}
