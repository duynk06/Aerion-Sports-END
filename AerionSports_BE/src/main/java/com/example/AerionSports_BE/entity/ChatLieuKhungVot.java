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
@Table(name = "chat_lieu_khung_vot")
public class ChatLieuKhungVot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_chat_lieu_khung_vot", nullable = false, length = 50)
    private String maChatLieuKhungVot;

    @Nationalized
    @Column(name = "ten_chat_lieu_khung_vot", nullable = false)
    private String tenChatLieuKhungVot;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "idChatLieuKhungVot")
    private Set<SanPham> sanPhams = new LinkedHashSet<>();

}