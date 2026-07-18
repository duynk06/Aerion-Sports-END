package com.example.AerionSports_BE.dto;

import lombok.Data;
import java.util.List;

@Data
public class DatHangRequest {
    private Integer idDiaChi;          // chọn từ sổ địa chỉ đã lưu, hoặc null nếu nhập tay
    private String tenNguoiNhan;       // dùng nếu không chọn idDiaChi
    private String sdtNguoiNhan;
    private String diaChiNhan;
    private String ghiChu;
    private Integer idPhieuGiamGia;    // optional
    private List<SanPhamDatHangItem> items;
}