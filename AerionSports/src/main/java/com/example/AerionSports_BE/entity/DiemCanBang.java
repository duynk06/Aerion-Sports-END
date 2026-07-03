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
@Table(name = "diem_can_bang")
public class DiemCanBang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_diem_can_bang", nullable = false, unique = true, length = 50)
    private String maDiemCanBang;

    @Column(name = "ten_diem_can_bang", nullable = false, length = 100)
    private String tenDiemCanBang;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
