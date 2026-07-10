package com.example.AerionSports_BE.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_giao_ca")
@Getter
@Setter
public class LichSuGiaoCa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_ca", unique = true, nullable = false, length = 50)
    private String maCa;

    @Column(name = "thoi_gian_vao")
    private LocalDateTime thoiGianVao;

    @Column(name = "thoi_gian_ra")
    private LocalDateTime thoiGianRa;

    @Column(name = "tien_ban_dau", nullable = false)
    private BigDecimal tienBanDau;

    @Column(name = "tien_mat_doanh_thu")
    private BigDecimal tienMatDoanhThu;

    @Column(name = "tien_chuyen_khoan")
    private BigDecimal tienChuyenKhoan;

    @Column(name = "tien_mat_thuc_te")
    private BigDecimal tienMatThucTe;

    @Column(name = "tien_mat_chenh_lech")
    private BigDecimal tienMatChenhLech;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Ca đang mở, 1: Ca đã chốt bàn giao

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhan_vien_ca_truoc", nullable = false)
    private NhanVien nhanVienCaTruoc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhan_vien_ca_sau")
    private NhanVien nhanVienCaSau;
}