package com.example.AerionSports_BE.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dia_chi_khach_hang")
public class DiaChiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // --- MỐI QUAN HỆ NHIỀU - 1 VỚI BẢNG KHÁCH HÀNG ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    @JsonBackReference // Đầu nghịch của JsonManagedReference để chặn vòng lặp JSON
    private KhachHang khachHang;

    @Column(name = "nguoi_nhan", nullable = false, length = 255)
    private String nguoiNhan;

    @Column(name = "sdt", nullable = false, length = 15)
    private String sdt;

    @Column(name = "tinh_thanh", length = 100)
    private String tinhThanh;

    @Column(name = "phuong_xa", length = 100)
    private String phuongXa;

    @Column(name = "dia_chi_chi_tiet", length = 500)
    private String diaChiChiTiet;

    @Column(name = "mac_dinh")
    private Boolean macDinh = false; // Thuộc tính BIT trong SQL tương ứng với Boolean trong Java

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();
}