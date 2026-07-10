package com.example.AerionSports_BE.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dot_giam_gia")
public class DotGiamGia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_dot_giam_gia", nullable = false, unique = true, length = 50)
    private String maDotGiamGia;

    @Column(name = "ten_dot_giam_gia", nullable = false, length = 255)
    private String tenDotGiamGia;

    @Column(name = "gia_tri_giam", precision = 18, scale = 2)
    private BigDecimal giaTriGiam;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "trang_thai")
    private Integer trangThai = 1;

    @OneToMany(mappedBy = "dotGiamGia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietDotGiamGia> chiTietDotGiamGiaList = new ArrayList<>();
}
