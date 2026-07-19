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
@Table(name = "thanh_toan")
public class ThanhToan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne
    @JoinColumn(name = "id_hinh_thuc_thanh_toan", nullable = false)
    private PhuongThucThanhToan hinhThucThanhToan;

    @Column(name = "ma_giao_dich", length = 100)
    private String maGiaoDich;

    @Column(name = "so_tien", nullable = false, precision = 18, scale = 2)
    private BigDecimal soTien;

    @Column(name = "thoi_gian_thanh_toan")
    private LocalDateTime thoiGianThanhToan = LocalDateTime.now();

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "trang_thai_thanh_toan", length = 100)
    private String trangThaiThanhToan;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao = LocalDateTime.now();

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat = LocalDateTime.now();

    @Column(name = "trang_thai")
    private Integer trangThai = 1;

    @Column(name = "tien_khach_dua", precision = 18, scale = 2)
    private BigDecimal tienKhachDua;

    @Column(name = "tien_thua", precision = 18, scale = 2)
    private BigDecimal tienThua;
}
