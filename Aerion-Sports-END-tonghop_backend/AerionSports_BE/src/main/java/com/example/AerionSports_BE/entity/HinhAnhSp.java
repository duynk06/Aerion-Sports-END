    package com.example.AerionSports_BE.entity;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;
    import org.hibernate.annotations.ColumnDefault;

    import java.time.Instant;

    @Getter
    @Setter
    @Entity
    @Table(name = "hinh_anh_sp")
    public class HinhAnhSp {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Integer id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
        private ChiTietSanPham idSanPhamChiTiet;

        @ColumnDefault("0")
        @Column(name = "la_anh_chinh")
        private Boolean laAnhChinh;

        @Column(name = "duong_dan_anh", length = 500)
        private String duongDanAnh;

        @ColumnDefault("getdate()")
        @Column(name = "ngay_tao", insertable = false, updatable = false)
        private Instant ngayTao;

        @ColumnDefault("1")
        @Column(name = "trang_thai")
        private Integer trangThai;

    }