package com.example.AerionSports_BE.repository;
import com.example.AerionSports_BE.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatRoomIdOrderByThoiGianGuiAsc(Long chatRoomId);

    void deleteByChatRoomId(Long chatRoomId);
}