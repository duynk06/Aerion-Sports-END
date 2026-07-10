package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.DiemCanBangRequest;
import com.example.AerionSports_BE.dto.response.DiemCanBangResponse;
import com.example.AerionSports_BE.entity.DiemCanBang;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IDiemCanBangService {
    List<DiemCanBangResponse> getAll();
    Page<DiemCanBang> search(int page, int size, Integer trangThai);
    DiemCanBangResponse save(DiemCanBangRequest r);
    DiemCanBangResponse update(Integer id, DiemCanBangRequest r);
    void delete(Integer id);
}
