package com.example.AerionSports_BE.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "trong_luong")
public class TrongLuong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_trong_luong", nullable = false, unique = true, length = 50)
    private String maTrongLuong;

    @Column(name = "ten_trong_luong", nullable = false, length = 50)
    private String tenTrongLuong;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
