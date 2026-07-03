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
@Table(name = "chu_vi_can_vot")
public class ChuViCanVot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chu_vi_can_vot", nullable = false, unique = true, length = 50)
    private String maChuViCanVot;

    @Column(name = "ten_chu_vi_can_vot", nullable = false, length = 100)
    private String tenChuViCanVot;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
