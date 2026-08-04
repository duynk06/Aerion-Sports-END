package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KhachHangQuickResponse {
    private Integer id;
    private String hoTen;
    private String sdt;
    private String email;
    private String diaChi;
    private String tinhThanh;
}