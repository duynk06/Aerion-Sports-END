package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.response.KhachHangResponse;
import com.example.AerionSports_BE.entity.KhachHang;

import java.util.List;

public interface KhachHangService {
    List<KhachHang> getAll();

    KhachHang getById(Integer id);

    KhachHang add(KhachHang khachHang);

    KhachHang update(Integer id, KhachHang khachHang);

    void delete(Integer id);

    List<KhachHangResponse> getAllSummary();
}
