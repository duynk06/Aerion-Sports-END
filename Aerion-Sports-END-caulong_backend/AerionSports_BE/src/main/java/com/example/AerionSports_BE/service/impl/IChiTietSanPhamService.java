package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.ChiTietSanPhamFilter;
import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IChiTietSanPhamService {
    List<ChiTietSanPhamResponse> getAll();
    Page<ChiTietSanPhamResponse> search(ChiTietSanPhamFilter f);
    ChiTietSanPhamResponse save(ChiTietSanPhamRequest r);
    ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest r);
    void delete(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    List<SanPhamResponse> getAllProductsWithVariantsForCheck();
}
