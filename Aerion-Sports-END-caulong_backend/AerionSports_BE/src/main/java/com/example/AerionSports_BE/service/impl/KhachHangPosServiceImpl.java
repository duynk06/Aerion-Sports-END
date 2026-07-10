package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.response.KhachHangPosResponse;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import com.example.AerionSports_BE.service.KhachHangPosService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KhachHangPosServiceImpl implements KhachHangPosService {
    private final KhachHangRepository khachHangRepository;

    @Override
    public Page<KhachHangPosResponse> locKhachHangPos(String keyword, int page, int size) {
        // Nếu keyword truyền lên là chuỗi rỗng, đổi thành null để Query xử lý chính xác hơn
        if (keyword != null && keyword.trim().isEmpty()) {
            keyword = null;
        }
        return khachHangRepository.timKhachHangChoPos(keyword, PageRequest.of(page, size));
    }
}
