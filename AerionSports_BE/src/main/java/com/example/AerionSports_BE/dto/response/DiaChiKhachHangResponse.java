package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// DTO

public interface DiaChiKhachHangResponse {
    Integer getId();
    String getNguoiNhan();
    String getSdt();
    String getTinhThanh();
    String getPhuongXa();
    String getDiaChiChiTiet();
    Boolean getMacDinh();
    String getDiaChiDayDu();
}