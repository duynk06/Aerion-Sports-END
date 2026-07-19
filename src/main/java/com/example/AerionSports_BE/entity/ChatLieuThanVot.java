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
@Table(name = "chat_lieu_than_vot")
public class ChatLieuThanVot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "ma_chat_lieu_than_vot", nullable = false, length = 50)
    private String maChatLieuThanVot;

    @Nationalized
    @Column(name = "ten_chat_lieu_than_vot", nullable = false)
    private String tenChatLieuThanVot;

    @ColumnDefault("1")
    @Column(name = "trang_thai")
    private Integer trangThai;

    @JsonIgnore
    @OneToMany(mappedBy = "idChatLieuThanVot")
    private Set<SanPham> sanPhams = new LinkedHashSet<>();

}