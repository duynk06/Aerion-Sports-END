package com.example.AerionSports_BE.dto.request;

import lombok.Data;

@Data
public class DiaChiRequest {
    private String nguoiNhan;
    private String sdt;
    private String tinhThanh;
    private String phuongXa;
    private String diaChiChiTiet;
    private Boolean macDinh;
}
