package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.ChatMessageDto;
import com.example.AerionSports_BE.entity.ChatRoom;
import com.example.AerionSports_BE.repository.ChatMessageRepository;
import com.example.AerionSports_BE.repository.ChatRoomRepository;
import com.example.AerionSports_BE.service.AIService;
import com.example.AerionSports_BE.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
public class ChatWebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private AIService aiService;

    @MessageMapping("/chat.sendFromClient")
    public void handleIncomingMessage(@Payload ChatMessageDto messageDto) {
        Long roomId = messageDto.getRoomId();
        Long khachHangId = messageDto.getKhachHangId();
        String nguoiGui = messageDto.getNguoiGui();
        String noiDung = messageDto.getNoiDung();
        Long nguoiGuiId = messageDto.getNguoiGuiId();

        ChatRoom room = null;
        if (roomId != null) {
            room = chatRoomRepository.findById(roomId).orElse(null);
        }

        if (room == null) {
            room = chatService.getOrCreateRoom(khachHangId);
            roomId = room.getId();
            messageDto.setRoomId(roomId);
        }

        chatService.saveMessage(room, nguoiGui, nguoiGuiId, noiDung);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, messageDto);
        if (khachHangId != null) {
            messagingTemplate.convertAndSend("/topic/room/customer/" + khachHangId, messageDto);
        }
        messagingTemplate.convertAndSend("/topic/room/staff/" + roomId, messageDto);

        String currentMode = room.getChatMode();
        boolean isAiMode = (currentMode == null || currentMode.trim().isEmpty() || "AI_REPLY".equalsIgnoreCase(currentMode.trim()));
        boolean isKhachHang = nguoiGui != null &&
                !"AI".equalsIgnoreCase(nguoiGui) &&
                !nguoiGui.toUpperCase().contains("NHAN_VIEN") &&
                !nguoiGui.toUpperCase().contains("STAFF");

        if (isAiMode && isKhachHang) {
            String aiAnswer = aiService.getAiReply(noiDung);

            chatService.saveMessage(room, "AI", 0L, aiAnswer);

            ChatMessageDto aiResponseDto = new ChatMessageDto(khachHangId, "AI", aiAnswer, 0L);
            aiResponseDto.setRoomId(roomId);

            messagingTemplate.convertAndSend("/topic/room/" + roomId, aiResponseDto);
            if (khachHangId != null) {
                messagingTemplate.convertAndSend("/topic/room/customer/" + khachHangId, aiResponseDto);
            }
            messagingTemplate.convertAndSend("/topic/room/staff/" + roomId, aiResponseDto);
        } else {
            messagingTemplate.convertAndSend("/topic/room/staff/global-updates", messageDto);
        }
    }

    @MessageMapping("/chat.sendFromStaff")
    public void handleStaffMessage(@Payload ChatMessageDto messageDto) {
        Long roomId = messageDto.getRoomId();

        if (roomId == null) return;

        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) return;

        room.setChatMode("STAFF_REPLY");
        chatRoomRepository.save(room);

        chatService.saveMessage(room, "NHAN_VIEN", messageDto.getNguoiGuiId(), messageDto.getNoiDung());

        messageDto.setNguoiGui("NHAN_VIEN");
        messageDto.setRoomId(roomId);

        Long khachHangId = null;
        if (room.getKhachHang() != null && room.getKhachHang().getId() != null) {
            khachHangId = room.getKhachHang().getId().longValue();
        }
        messageDto.setKhachHangId(khachHangId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, messageDto);
        messagingTemplate.convertAndSend("/topic/room/staff/" + roomId, messageDto);
        if (khachHangId != null) {
            messagingTemplate.convertAndSend("/topic/room/customer/" + khachHangId, messageDto);
        }
        messagingTemplate.convertAndSend("/topic/room/staff/global-updates", messageDto);
    }

    @MessageMapping("/chat.requestStaff")
    public void handleRequestStaff(@Payload ChatMessageDto messageDto) {
        Long roomId = messageDto.getRoomId();
        ChatRoom room = null;

        if (roomId != null) {
            room = chatRoomRepository.findById(roomId).orElse(null);
        }
        if (room == null && messageDto.getKhachHangId() != null) {
            room = chatService.getOrCreateRoom(messageDto.getKhachHangId());
            roomId = room.getId();
        }

        if (room == null) return;

        room.setChatMode("STAFF_REPLY");
        chatRoomRepository.save(room);

        String noticeText = "Chuyển giao cuộc hội thoại cho nhân viên hỗ trợ.";
        chatService.saveMessage(room, "AI", 0L, noticeText);

        ChatMessageDto systemNotice = new ChatMessageDto(messageDto.getKhachHangId(), "AI", noticeText, 0L);
        systemNotice.setRoomId(roomId);

        messagingTemplate.convertAndSend("/topic/room/" + roomId, systemNotice);
        messagingTemplate.convertAndSend("/topic/room/staff/new-request", "Yêu cầu mới!");
        messagingTemplate.convertAndSend("/topic/room/staff/global-updates", systemNotice);
    }

    @MessageMapping("/chat.switchToStaff")
    public void handleStaffTakeOver(@Payload Map<String, Object> payload) {
        if (payload.get("roomId") == null) return;

        Long roomId = Long.valueOf(payload.get("roomId").toString());

        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.setChatMode("STAFF_REPLY");
            chatRoomRepository.save(room);

            Long khachHangId = null;
            if (room.getKhachHang() != null && room.getKhachHang().getId() != null) {
                khachHangId = room.getKhachHang().getId().longValue();
            }

            String sysMsg = "Nhân viên đã tiếp nhận cuộc trò chuyện.";
            chatService.saveMessage(room, "AI", 0L, sysMsg);

            ChatMessageDto noticeDto = new ChatMessageDto(khachHangId, "AI", sysMsg, 0L);
            noticeDto.setRoomId(roomId);

            messagingTemplate.convertAndSend("/topic/room/" + roomId, Map.of(
                    "type", "MODE_CHANGE",
                    "chatMode", "STAFF_REPLY",
                    "noiDung", sysMsg
            ));

            messagingTemplate.convertAndSend("/topic/room/" + roomId, noticeDto);
            messagingTemplate.convertAndSend("/topic/room/staff/global-updates", noticeDto);
        }
    }

    @Transactional
    @MessageMapping("/chat.endSession")
    public void endSession(@Payload ChatMessageDto messageDTO) {
        if (messageDTO.getRoomId() == null) return;

        Long roomId = messageDTO.getRoomId();
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) return;

        room.setChatMode("AI_REPLY");
        chatRoomRepository.save(room);

        ChatMessageDto notifyDto = new ChatMessageDto();
        notifyDto.setType("END_CHAT");
        notifyDto.setRoomId(roomId);
        notifyDto.setNoiDung("Cuộc trò chuyện đã kết thúc bởi nhân viên. Bạn có thể tiếp tục chat với AI hoặc yêu cầu gặp nhân viên lại.");

        messagingTemplate.convertAndSend("/topic/room/" + roomId, notifyDto);
        if (room.getKhachHang() != null && room.getKhachHang().getId() != null) {
            Long khachHangId = room.getKhachHang().getId().longValue();
            messagingTemplate.convertAndSend("/topic/room/customer/" + khachHangId, notifyDto);
        }
        messagingTemplate.convertAndSend("/topic/room/staff/global-updates", notifyDto);
    }

    @GetMapping("/chat-nhan-vien")
    public String chatNhanVien() {
        return "chat-staff";
    }

    @PostMapping("/api/chat/switch-to-staff")
    @ResponseBody
    public ResponseEntity<?> switchToStaff(@RequestParam Long roomId) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room != null) {
            room.setChatMode("STAFF_REPLY");
            chatRoomRepository.save(room);
            messagingTemplate.convertAndSend("/topic/room/staff/new-request", "🔄 Yêu cầu mới!");
            return ResponseEntity.ok(Map.of("status", "success"));
        }
        return ResponseEntity.badRequest().body(Map.of("status", "error", "message", "Phòng không tồn tại"));
    }

    @GetMapping("/api/chat/history/{roomId}")
    @ResponseBody
    public ResponseEntity<?> getHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getChatHistory(roomId));
    }

    @GetMapping("/api/chat/rooms/staff")
    @ResponseBody
    public List<ChatRoom> getAllRoomsForStaff() {
        return chatRoomRepository.findByChatMode("STAFF_REPLY");
    }
}