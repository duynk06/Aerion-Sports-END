package com.example.AerionSports_BE.dto;

import lombok.Data;

@Data
public class CapNhatHoSoRequest {
    private String hoTen;
    private String email;
    private String ngaySinh;   // "yyyy-MM-dd"
    private Integer gioiTinh;
}