package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrongLuongResponse {
    private Integer id;
    private String maTrongLuong;
    private String tenTrongLuong;
    private Integer trangThai;
}
