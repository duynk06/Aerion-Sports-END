package com.example.AerionSports_BE.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "phieu_giam_gia")
public class PhieuGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phieu_giam_gia", nullable = false, unique = true, length = 50)
    private String maPhieuGiamGia;

    @Column(name = "ten_phieu_giam_gia", nullable = false, length = 255)
    private String tenPhieuGiamGia;

    @Column(name = "loai_phieu_giam_gia", length = 50)
    private String loaiPhieuGiamGia;

    @Column(name = "dot_luong_su_dung")
    private Integer dotLuongSuDung;

    @Column(name = "gia_tri_giam", precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "gia_tri_don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal giaTriDonToiThieu;

    @Column(name = "gia_tri_giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giaTriGiamToiDa;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "so_luong_da_su_dung")
    private Integer soLuongDaSuDung = 0;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "trang_thai_phieu_giam_gia")
    private Integer trangThaiPhieuGiamGia;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Column(name = "trang_thai")
    private Integer trangThai = 1;
}
