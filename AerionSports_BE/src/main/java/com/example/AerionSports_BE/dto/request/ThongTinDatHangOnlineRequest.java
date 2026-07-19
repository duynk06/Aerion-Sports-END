package com.example.AerionSports_BE.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThongTinDatHangOnlineRequest {

    private String hoTen;
    private String soDienThoai;
    private String email;
    private String tinhThanh;
    private String quanHuyen;
    private String phuongXa;
    private String diaChiChiTiet;
    private String ghiChu;
    private String phuongThucThanhToan;
}
