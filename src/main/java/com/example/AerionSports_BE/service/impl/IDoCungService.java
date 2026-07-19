package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.DoCungRequest;
import com.example.AerionSports_BE.dto.response.DoCungResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IDoCungService {
    List<DoCungResponse> getAll();
    Page<DoCungResponse> search(int page, int size, Integer trangThai, String keyword);
    DoCungResponse save(DoCungRequest r);
    DoCungResponse update(Integer id, DoCungRequest r);
    void delete(Integer id);
}
