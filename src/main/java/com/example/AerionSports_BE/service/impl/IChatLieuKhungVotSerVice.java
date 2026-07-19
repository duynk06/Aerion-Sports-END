package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.ChatLieuKhungVotRequest;
import com.example.AerionSports_BE.dto.response.ChatLieuKhungVotResponse;
import com.example.AerionSports_BE.entity.ChatLieuKhungVot;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IChatLieuKhungVotSerVice {
    List<ChatLieuKhungVotResponse> getAll();
    Page<ChatLieuKhungVot> search(int page, int size, Integer trangThai);
    ChatLieuKhungVotResponse save(ChatLieuKhungVotRequest r);
    ChatLieuKhungVotResponse update(Integer id, ChatLieuKhungVotRequest r);
    void delete(Integer id);

}
