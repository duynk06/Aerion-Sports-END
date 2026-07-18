package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.repository.ChiTietHoaDonRepository;
import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import com.example.AerionSports_BE.service.ChiTietHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ChiTietHoaDonServiceImpl implements ChiTietHoaDonService {
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Override
    public List<ChiTietHoaDonResponse> getChiTietHoaDon(Integer idHoaDon) {
        return chiTietHoaDonRepository.getChiTietHoaDon(idHoaDon);
    }
}
