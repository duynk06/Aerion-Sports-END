package com.example.AerionSports_BE.dto.request;

import com.example.AerionSports_BE.entity.HoaDon;
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
public class LichSuThanhToanRequest {

    private Integer id;

    private HoaDon hoaDon;

    private BigDecimal soTien;

    private String phuongThucThanhToan;

    private String trangThaiThanhToan;

    private LocalDateTime ngayThanhToan = LocalDateTime.now();

    private String ghiChu;
}
