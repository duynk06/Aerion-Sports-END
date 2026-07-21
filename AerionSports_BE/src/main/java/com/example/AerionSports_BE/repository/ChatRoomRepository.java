package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findFirstByKhachHang_IdOrderByIdDesc(Long khachHangId);
    List<ChatRoom> findByChatMode(String chatMode);
}