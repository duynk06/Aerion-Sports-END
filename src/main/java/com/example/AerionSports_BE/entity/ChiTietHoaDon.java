package com.example.AerionSports_BE.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "chi_tiet_hoa_don")
public class ChiTietHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_chi_tiet_san_pham", nullable = false)
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "ma_hoa_don_chi_tiet", length = 100)
    private String maHoaDonChiTiet;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", nullable = false, precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal thanhTien;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Column(name = "nguoi_cap_nhat", length = 100)
    private String nguoiCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai = 1;
}
