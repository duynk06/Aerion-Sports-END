package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.request.SanPhamFilter;
import com.example.AerionSports_BE.dto.request.SanPhamRequest;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ISanPhamService {
    Page<SanPhamResponse> search(SanPhamFilter f);
    List<SanPhamResponse> getAll();
    long countTotal();
    SanPhamResponse findById(Integer id);
    void updateTrangThai(Integer id, Integer trangThai);
    SanPhamResponse save(SanPhamRequest r); // Chữ ký hàm tinh gọn nhận tệp tin đóng gói trực tiếp trong Request DTO
    SanPhamResponse update(Integer id, SanPhamRequest r);
    void delete(Integer id);
}