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
@Table(name = "chat_lieu_khung_vot")
public class ChatLieuKhungVot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_chat_lieu_khung_vot", nullable = false, unique = true, length = 50)
    private String maChatLieuKhungVot;

    @Column(name = "ten_chat_lieu_khung_vot", nullable = false, length = 255)
    private String tenChatLieuKhungVot;

    @Column(name = "trang_thai")
    private Integer trangThai;
}
