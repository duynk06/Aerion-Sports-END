package com.example.AerionSports_BE.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal; // ⚡ Nhớ import thư viện này
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VoucherRequestDTO {
    private String maPhieuGiamGia;
    private String tenPhieuGiamGia;
    private String loaiPhieuGiamGia;

    // ⚡ Đổi 3 trường này từ Double sang BigDecimal
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriDonToiThieu;
    private BigDecimal giaTriGiamToiDa;

    private Integer soLuong;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String moTa;
    private Integer trangThai;
    private String doiTuongApDung;
    private List<Integer> khachHangIds;
}