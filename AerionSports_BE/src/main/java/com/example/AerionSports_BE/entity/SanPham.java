package com.example.AerionSports_BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "san_pham")
@org.hibernate.annotations.BatchSize(size = 20)
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thuong_hieu")
    @JsonIgnore
    private ThuongHieu idThuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_xuat_xu")
    @JsonIgnore
    private XuatXu idXuatXu;

    // 🌟 6 THUỘC TÍNH NỀN ĐƯỢC DỊCH CHUYỂN LÊN ĐÂY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_do_cung")
    @JsonIgnore
    private DoCung idDoCung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diem_can_bang")
    @JsonIgnore
    private DiemCanBang idDiemCanBang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu_than_vot")
    @JsonIgnore
    private ChatLieuThanVot idChatLieuThanVot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu_khung_vot")
    @JsonIgnore
    private ChatLieuKhungVot idChatLieuKhungVot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc")
    @JsonIgnore
    private DanhMuc idDanhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chu_vi_can_vot")
    @JsonIgnore
    private ChuViCanVot idChuViCanVot;

    @Column(name = "ma_san_pham", nullable = false, length = 50)
    private String maSanPham;

    @Nationalized
    @Column(name = "ten_san_pham", nullable = false)
    private String tenSanPham;

    @Nationalized
    @Column(name = "mo_ta", length = 1000)
    private String moTa;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_tao")
    private Instant ngayTao;

    @ColumnDefault("getdate()")
    @Column(name = "ngay_sua")
    private Instant ngaySua;

    @Nationalized
    @Column(name = "bao_hanh", length = 100)
    private String baoHanh;

    @OneToMany(mappedBy = "idSanPham", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("idSanPham")
    private Set<ChiTietSanPham> chiTietSanPhams = new LinkedHashSet<>();
}