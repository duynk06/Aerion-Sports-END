package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.SanPhamFilter;
import com.example.AerionSports_BE.dto.request.SanPhamRequest;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ISanPhamService {
    Page<SanPhamResponse> search(SanPhamFilter f);
    void updateTrangThai(Integer id, Integer trangThai);
    SanPhamResponse save(SanPhamRequest r, List<MultipartFile> files);
    SanPhamResponse update(Integer id, SanPhamRequest r);
    void delete(Integer id);
}
