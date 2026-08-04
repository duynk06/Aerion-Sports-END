package com.example.AerionSports_BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "trong_luong")
public class TrongLuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_trong_luong", nullable = false, length = 50)
    private String maTrongLuong;

    @Column(name = "ten_trong_luong", nullable = false, length = 50)
    private String tenTrongLuong;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "idTrongLuong")
    private Set<ChiTietSanPham> chiTietSanPhams = new LinkedHashSet<>();

}