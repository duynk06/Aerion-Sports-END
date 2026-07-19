package com.example.AerionSports_BE.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tai_khoan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_dang_nhap", unique = true, nullable = false, length = 50)
    private String tenDangNhap;

    @Column(name = "mat_khau_hash", nullable = false, length = 255)
    private String matKhauHash;

    @Column(name = "loai_tai_khoan", nullable = false, length = 20)
    private String loaiTaiKhoan; // 'NHAN_VIEN' hoặc 'KHACH_HANG'

    @Column(name = "id_chu_tai_khoan", nullable = false)
    private Integer idChuTaiKhoan;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Column(name = "trang_thai")
    private Integer trangThai = 1;
}