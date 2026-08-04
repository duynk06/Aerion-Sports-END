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
@Table(name = "diem_can_bang")
public class DiemCanBang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_diem_can_bang", nullable = false, length = 50)
    private String maDiemCanBang;

    @Column(name = "ten_diem_can_bang", nullable = false, length = 100)
    private String tenDiemCanBang;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "idDiemCanBang")
    private Set<SanPham> sanPhams = new LinkedHashSet<>();

}