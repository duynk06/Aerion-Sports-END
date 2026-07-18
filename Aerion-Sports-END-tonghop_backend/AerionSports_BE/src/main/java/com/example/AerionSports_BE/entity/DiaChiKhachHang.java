package com.example.AerionSports_BE.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi_khach_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChiKhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 🌟 SỬA: quan hệ ManyToOne thay vì lưu Integer thô, khớp với cách
    // BanHangServiceImpl và Controller đang dùng (.khachHang(kh))
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
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
    private Boolean macDinh = false;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();
}