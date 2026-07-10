package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.MauSacRequest;
import com.example.AerionSports_BE.dto.response.MauSacResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IMauSacService {
    List<MauSacResponse> getAll();
    Page<MauSacResponse> search(int page, int size, Integer trangThai, String keyword);
    MauSacResponse save(MauSacRequest r);
    MauSacResponse update(Integer id, MauSacRequest r);
    void delete(Integer id);

}
