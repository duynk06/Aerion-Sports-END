package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


// PhieuGiamGiaPosResponse.java — thêm field phiếu gợi ý
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PhieuGiamGiaPosResponse {
    private Integer id;
    private String maPhieuGiamGia;
    private String tenPhieuGiamGia;
    private String loaiPhieuGiamGia;
    private BigDecimal giaTriGiam;
    private BigDecimal giaTriDonToiThieu;
    private BigDecimal giaTriGiamToiDa;
    private LocalDateTime ngayKetThuc;
    private BigDecimal soTienGiamThucTe;
    private boolean coTheApDung;

    // ✅ Thêm thông tin phiếu gợi ý tốt hơn
    private PhieuGoiYResponse phieuGoiY;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class PhieuGoiYResponse {
        private Integer id;
        private String maPhieuGiamGia;
        private String tenPhieuGiamGia;
        private String loaiPhieuGiamGia;
        private BigDecimal giaTriGiam;
        private BigDecimal giaTriDonToiThieu;
        private BigDecimal giaTriGiamToiDa;
        private BigDecimal soTienCanMuaThem;    // Cần mua thêm bao nhiêu
        private BigDecimal soTienGiamNeuDat;    // Sẽ giảm được bao nhiêu nếu đạt
    }
}
