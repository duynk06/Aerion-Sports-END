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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "lich_su_giao_ca")
public class LichSuGiaoCa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phien", nullable = false, unique = true, length = 50)
    private String maPhien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ca_lam_viec", nullable = false)
    private CaLamViec caLamViec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_ca_truoc", nullable = false)
    private NhanVien nhanVienCaTruoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_ca_sau")
    private NhanVien nhanVienCaSau;

    @Column(name = "thoi_gian_vao", nullable = false)
    private LocalDateTime thoiGianVao;

    @Column(name = "thoi_gian_ra")
    private LocalDateTime thoiGianRa;

    @Column(name = "tien_ban_dau", precision = 18, scale = 2)
    private BigDecimal tienBanDau;

    @Column(name = "tien_mat_doanh_thu", precision = 18, scale = 2)
    private BigDecimal tienMatDoanhThu;

    @Column(name = "tien_chuyen_khoan", precision = 18, scale = 2)
    private BigDecimal tienChuyenKhoan;

    @Column(name = "tien_mat_thuc_te", precision = 18, scale = 2)
    private BigDecimal tienMatThucTe;

    @Column(name = "tien_mat_chenh_lech", precision = 18, scale = 2)
    private BigDecimal tienMatChenhLech;

    @Column(name = "ghi_chu", columnDefinition = "NVARCHAR(MAX)")
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
