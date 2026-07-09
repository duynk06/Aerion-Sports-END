package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.ChiTietSanPhamFilter;
import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IChiTietSanPhamService {
    List<ChiTietSanPhamResponse> getAll();

    // 🌟 BỔ SUNG: Hàm tìm kiếm biến thể theo ID phục vụ cho chức năng Sửa (Edit)
    ChiTietSanPhamResponse findById(Integer id);

    Page<ChiTietSanPhamResponse> search(ChiTietSanPhamFilter f);
    ChiTietSanPhamResponse save(ChiTietSanPhamRequest r);
    ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest r);
    void delete(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    List<SanPhamResponse> getAllProductsWithVariantsForCheck();
    ChiTietSanPhamResponse updateFullDetailsFromModal(Integer id, ChiTietSanPhamRequest r, MultipartFile fileAnh) throws Exception;
}
