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
@Table(name = "xuat_xu")
public class XuatXu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_xuat_xu", nullable = false, length = 50)
    private String maXuatXu;

    @Nationalized
    @Column(name = "ten_xuat_xu", nullable = false)
    private String tenXuatXu;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "idXuatXu")
    @JsonIgnore
    private Set<SanPham> sanPhams = new LinkedHashSet<>();

}