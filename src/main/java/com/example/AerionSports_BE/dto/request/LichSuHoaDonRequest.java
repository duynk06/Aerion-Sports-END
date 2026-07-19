package com.example.AerionSports_BE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoaDonRequest {
    private Integer id;

    // Hóa đơn
    private Integer idHoaDon;
    private String maHoaDon;
    private Integer trangThaiHoaDon;

    // Nhân viên thao tác
    private Integer idNhanVien;
    private String tenNhanVien;

    // Lịch sử
    private Integer trangThaiCu;
    private Integer trangThaiMoi;
    private String hanhDong;
    private LocalDateTime thoiGianHanhDong;
    private String ghiChu;

}
