package com.example.AerionSports_BE.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DotGiamGiaDTO {

    private Integer id;
    private String maDotGiamGia;
    private String tenDotGiamGia;
    private BigDecimal giaTriGiam;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private String moTa;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai; // 1: Sắp diễn ra, 2: Đang diễn ra, 3: Đã kết thúc, 0: Hủy
    private String trangThaiText; // human-readable status

    private List<ChiTietDotGiamGiaDTO> chiTietList;
}
