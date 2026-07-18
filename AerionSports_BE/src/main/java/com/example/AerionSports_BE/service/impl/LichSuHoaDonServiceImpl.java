package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.LichSuHoaDon;
import com.example.AerionSports_BE.repository.LichSuHoaDonRepository;
import com.example.AerionSports_BE.dto.response.LichSuHoaDonResponse;
import com.example.AerionSports_BE.service.LichSuHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LichSuHoaDonServiceImpl implements LichSuHoaDonService {

    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;

    @Override
    public List<LichSuHoaDonResponse> getByHoaDon(Integer idHoaDon) {

        List<LichSuHoaDon> list =
                lichSuHoaDonRepository
                        .findByHoaDon_IdOrderByThoiGianHanhDongDesc(idHoaDon);

        return list.stream()
                .map(LichSuHoaDonResponse::new)
                .toList();
    }
}
