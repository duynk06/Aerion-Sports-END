package com.example.AerionSports_BE.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ca_lam_viec")
public class CaLamViec {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_ca", nullable = false, unique = true, length = 50)
    private String maCa;

    @Column(name = "ten_ca", nullable = false, length = 100)
    private String tenCa;

    @Column(name = "gio_vao", nullable = false)
    private LocalTime gioVao;

    @Column(name = "gio_ra", nullable = false)
    private LocalTime gioRa;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
