package com.example.AerionSports_BE.dto;
import lombok.Data;

@Data
public class DiaChiRequest {
    private String nguoiNhan;
    private String sdt;
    private String tinhThanh;
    private String phuongXa;      // đã gộp sẵn "phường/xã, quận/huyện" theo cách bạn đang lưu
    private String diaChiChiTiet;
    private Boolean macDinh;
}