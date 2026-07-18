package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.response.KhachHangPosResponse;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;


public interface KhachHangPosService{
    Page<KhachHangPosResponse> locKhachHangPos(String keyword, int page, int size);
}
