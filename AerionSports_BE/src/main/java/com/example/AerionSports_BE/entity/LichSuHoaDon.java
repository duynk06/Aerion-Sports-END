package com.example.AerionSports_BE.entity;

import jakarta.persistence.*;
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
@Table(name = "lich_su_hoa_don")
public class LichSuHoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "trang_thai_cu")
    private Integer trangThaiCu;

    @Column(name = "trang_thai_moi")
    private Integer trangThaiMoi;

    @Column(name = "hanh_dong", nullable = false, length = 255)
    private String hanhDong;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "thoi_gian_hanh_dong")
    private LocalDateTime thoiGianHanhDong;

}