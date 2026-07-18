package com.example.AerionSports_BE.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ThongKeChiTietResponse {
    private List<TopBanChay> topBanChay;
    private List<TrangThaiDonHang> trangThaiDonHang;
    private List<KhachHangTiemNang> khachHangTiemNang;
    private List<SanPhamTonKho> sanPhamTonKho;

    // Các class lồng tĩnh (Static inner classes) phục vụ mapping phẳng gọn nhẹ
    @Getter @Setter public static class TopBanChay {
        private String tenSanPham;
        private Long daBan;
        private Integer tonKho;
    }

    @Getter @Setter public static class TrangThaiDonHang {
        private String tenTrangThai;
        private Long soLuong;
    }

    @Getter @Setter public static class KhachHangTiemNang {
        private String hoTen;
        private String sdt;
        private Long soDon;
        private java.math.BigDecimal tongChiTieu;
    }

    @Getter @Setter public static class SanPhamTonKho {
        private String tenSanPham;
        private Long daBan;
        private Integer tonKho;
    }
}
