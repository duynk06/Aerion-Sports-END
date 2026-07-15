package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.TrongLuongRequest;
import com.example.AerionSports_BE.dto.response.TrongLuongResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ITrongLuongService {
    List<TrongLuongResponse> getAll();
    Page<TrongLuongResponse> search(int page, int size, Integer trangThai, String keyword);
    TrongLuongResponse save(TrongLuongRequest r);
    TrongLuongResponse update(Integer id, TrongLuongRequest r);
    void delete(Integer id);
}
