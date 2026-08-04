package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.ChatLieuThanVotRequest;
import com.example.AerionSports_BE.dto.response.ChatLieuThanVotResponse;
import com.example.AerionSports_BE.entity.ChatLieuThanVot;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IChatLieuThanVotService {
    List<ChatLieuThanVotResponse> getAll();
    Page<ChatLieuThanVot> search(int page, int size, Integer trangThai);
    ChatLieuThanVotResponse save(ChatLieuThanVotRequest r);
    ChatLieuThanVotResponse update(Integer id, ChatLieuThanVotRequest r);
    void delete(Integer id);
}
