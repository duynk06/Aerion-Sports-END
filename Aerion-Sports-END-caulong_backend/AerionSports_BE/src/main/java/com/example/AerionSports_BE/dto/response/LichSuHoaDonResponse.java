package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.LichSuHoaDon;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuHoaDonResponse {

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

    public LichSuHoaDonResponse(LichSuHoaDon lichSuHoaDon) {

        this.id = lichSuHoaDon.getId();
        this.trangThaiCu = lichSuHoaDon.getTrangThaiCu();
        this.trangThaiMoi = lichSuHoaDon.getTrangThaiMoi();
        this.hanhDong = lichSuHoaDon.getHanhDong();
        this.thoiGianHanhDong = lichSuHoaDon.getThoiGianHanhDong();

        this.ghiChu = lichSuHoaDon.getGhiChu();

        // Hóa đơn
        if (lichSuHoaDon.getHoaDon() != null) {

            this.idHoaDon =
                    lichSuHoaDon.getHoaDon().getId();

            this.maHoaDon =
                    lichSuHoaDon.getHoaDon().getMaHoaDon();
            this.trangThaiHoaDon =
                    lichSuHoaDon.getHoaDon().getTrangThai();
        }

        // Nhân viên thao tác
        if (lichSuHoaDon.getNhanVien() != null) {

            this.idNhanVien =
                    lichSuHoaDon.getNhanVien().getId();

            this.tenNhanVien =
                    lichSuHoaDon.getNhanVien().getTenNv();
        }
    }
}