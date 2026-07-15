package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.ThuongHieuRequest;
import com.example.AerionSports_BE.dto.response.ThuongHieuResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IThuongHieuService {
    List<ThuongHieuResponse> getAll();
    Page<ThuongHieuResponse> search(int page, int size, Integer trangThai, String keyword);
    ThuongHieuResponse getById(Integer id);
    ThuongHieuResponse save(ThuongHieuRequest r);
    ThuongHieuResponse update(Integer id, ThuongHieuRequest r);
    void delete(Integer id);
}
