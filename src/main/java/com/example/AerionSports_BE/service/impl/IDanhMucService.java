package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.DanhMucRequest;
import com.example.AerionSports_BE.dto.response.DanhMucResponse;
import com.example.AerionSports_BE.entity.DanhMuc;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IDanhMucService {
    List<DanhMucResponse> getAll();
    Page<DanhMuc> search(int page, int size, Integer trangThai);
    DanhMucResponse save(DanhMucRequest r);
    DanhMucResponse update(Integer id, DanhMucRequest r);
    void delete(Integer id);
}
