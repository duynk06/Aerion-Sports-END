package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.XuatXuRequest;
import com.example.AerionSports_BE.dto.response.XuatXuResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IXuatXuService {
    List<XuatXuResponse> getAll();
    Page<XuatXuResponse> search(int page, int size, Integer trangThai, String keyword);
    XuatXuResponse save(XuatXuRequest r);
    XuatXuResponse update(Integer id, XuatXuRequest r);
    void delete(Integer id);
}
