package com.example.AerionSports_BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "lich_lam_viec")
@Getter
@Setter
public class LichLamViec {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_nhan_vien", nullable = false)
    private Integer idNhanVien;

    @ManyToOne
    @JoinColumn(name = "id_ca_lam_viec", nullable = false)
    private CaLamViec caLamViec;

    @Column(name = "ngay_lam_viec", nullable = false)
    private LocalDate ngayLamViec;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Chờ duyệt, 1: Đi làm
}