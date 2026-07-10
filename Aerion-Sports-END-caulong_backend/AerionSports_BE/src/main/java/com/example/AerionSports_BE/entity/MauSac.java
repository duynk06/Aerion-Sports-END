package com.example.AerionSports_BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "mau_sac")
public class MauSac {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_mau_sac", nullable = false, length = 50)
    private String maMauSac;

    @Nationalized
    @Column(name = "ten_mau_sac", nullable = false, length = 100)
    private String tenMauSac;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "idMauSac")
    private Set<ChiTietSanPham> chiTietSanPhams = new LinkedHashSet<>();

}