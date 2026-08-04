package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.ChatMessage;
import com.example.AerionSports_BE.entity.ChatRoom;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.repository.ChatMessageRepository;
import com.example.AerionSports_BE.repository.ChatRoomRepository;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private KhachHangRepository khachHangRepository; // Thêm repository này để fetch thông tin Khách hàng

    // Lấy phòng chat có sẵn hoặc tự động tạo mới
    @Transactional
    public ChatRoom getOrCreateRoom(Long khachHangId) {
        // 1. Xử lý trường hợp khách vãng lai / chưa đăng nhập (khachHangId bị NULL)
        if (khachHangId == null) {
            ChatRoom newGuestRoom = new ChatRoom();
            newGuestRoom.setChatMode("AI_REPLY");
            newGuestRoom.setNgayTao(LocalDateTime.now());
            newGuestRoom.setCapNhatCuoi(LocalDateTime.now());
            return chatRoomRepository.save(newGuestRoom);
        }

        // 2. Sử dụng findFirstByKhachHang_IdOrderByIdDesc để đảm bảo luôn chỉ lấy 1 phòng chat mới nhất
        return chatRoomRepository.findFirstByKhachHang_IdOrderByIdDesc(khachHangId)
                .orElseGet(() -> {
                    ChatRoom newRoom = new ChatRoom();

                    KhachHang khachHang = khachHangRepository.findById(khachHangId.intValue())
                            .orElse(null);
                    newRoom.setKhachHang(khachHang);

                    newRoom.setChatMode("AI_REPLY");
                    newRoom.setNgayTao(LocalDateTime.now());
                    newRoom.setCapNhatCuoi(LocalDateTime.now());
                    return chatRoomRepository.save(newRoom);
                });
    }

    // Lưu tin nhắn mới vào database
    @Transactional
    public ChatMessage saveMessage(ChatRoom room, String nguoiGui, Long nguoiGuiId, String noiDung) {
        ChatMessage message = new ChatMessage();
        message.setChatRoom(room);
        message.setNguoiGui(nguoiGui);
        message.setNguoiGuiId(nguoiGuiId);
        message.setNoiDung(noiDung);
        message.setThoiGianGui(LocalDateTime.now());
        message.setTrangThai(0); // 0: Chưa đọc, 1: Đã đọc

        room.setCapNhatCuoi(LocalDateTime.now());
        chatRoomRepository.save(room);

        return chatMessageRepository.save(message);
    }

    @Transactional
    public ChatRoom switchToStaffMode(Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng chat!"));
        room.setChatMode("ENDED");
        room.setCapNhatCuoi(LocalDateTime.now());

        return chatRoomRepository.save(room);
    }

    public List<ChatMessage> getChatHistory(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByThoiGianGuiAsc(roomId);
    }
}