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
/**
 * DTO dùng để đẩy dữ liệu đợt giảm giá giữa controller, service và template/API.
 * Tất cả thông tin của một campaign đều đi qua object này để tránh map rải rác nhiều nơi.
 */
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
    // 0 = hủy, 1 = sắp diễn ra, 2 = đang diễn ra, 3 = đã kết thúc.
    private Integer trangThai; // 1: Sắp diễn ra, 2: Đang diễn ra, 3: Đã kết thúc, 0: Hủy
    // Text đã format sẵn để view hiển thị trực tiếp mà không cần if/else phức tạp.
    private String trangThaiText; // human-readable status

    // Danh sách sản phẩm/biến thể được áp dụng vào đợt giảm giá này.
    private List<ChiTietDotGiamGiaDTO> chiTietList;
}
