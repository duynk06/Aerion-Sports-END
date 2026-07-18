package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.response.LichSuThanhToanResponse;
import com.example.AerionSports_BE.entity.LichSuThanhToan;
import com.example.AerionSports_BE.repository.LichSuThanhToanRepository;
import com.example.AerionSports_BE.service.LichSuThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class LichSuThanhToanImpl implements LichSuThanhToanService {


    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;
    @Override
    public List<LichSuThanhToanResponse>
    getByHoaDon(Integer idHoaDon) {

        List<LichSuThanhToan> list =
                lichSuThanhToanRepository.findByHoaDonId(idHoaDon);

        return list.stream()
                .map(LichSuThanhToanResponse::new)
                .toList();
    }
}
