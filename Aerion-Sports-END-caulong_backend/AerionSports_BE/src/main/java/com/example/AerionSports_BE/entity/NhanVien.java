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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "nhan_vien")
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_vai_tro", nullable = false)
    private VaiTro vaiTro;

    @Column(name = "ma_nv", nullable = false, unique = true, length = 50)
    private String maNv;

    @Column(name = "ten_nv", nullable = false, length = 255)
    private String tenNv;

    @Column(name = "sdt", length = 15)
    private String sdt;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gioi_tinh")
    private Integer gioiTinh;

    @Column(name = "avatar", length = 500)
    private String avatar;

    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "dia_chi", length = 500)
    private String diaChi;

    @Column(name = "ngay_sua")
    private LocalDateTime ngaySua = LocalDateTime.now();

    @Column(name = "trang_thai")
    private Integer trangThai = 1;
}
