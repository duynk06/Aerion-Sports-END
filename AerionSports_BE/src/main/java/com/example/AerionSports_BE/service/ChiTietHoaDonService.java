package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;

import java.util.List;

public interface ChiTietHoaDonService {
    List<ChiTietHoaDonResponse> getChiTietHoaDon(Integer idHoaDon);
}
