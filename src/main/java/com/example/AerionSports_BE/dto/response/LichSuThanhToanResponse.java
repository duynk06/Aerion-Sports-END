package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.HoaDon;
import com.example.AerionSports_BE.entity.LichSuThanhToan;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LichSuThanhToanResponse {
    private Integer id;

    private Integer idHoaDon;

    private BigDecimal soTien;

    private String phuongThucThanhToan;

    private String trangThaiThanhToan;

    private LocalDateTime ngayThanhToan = LocalDateTime.now();

    private String ghiChu;

    public LichSuThanhToanResponse(LichSuThanhToan lichSuThanhToan) {
        this.id = lichSuThanhToan.getId();
        this.ghiChu = lichSuThanhToan.getGhiChu();
        this.soTien = lichSuThanhToan.getSoTien();
        this.phuongThucThanhToan = lichSuThanhToan.getPhuongThucThanhToan();
        this.trangThaiThanhToan = lichSuThanhToan.getTrangThaiThanhToan();
        this.ngayThanhToan = lichSuThanhToan.getNgayThanhToan();
        if (lichSuThanhToan.getHoaDon() != null) {
            this.idHoaDon = lichSuThanhToan.getHoaDon().getId();
        }
    }
}
