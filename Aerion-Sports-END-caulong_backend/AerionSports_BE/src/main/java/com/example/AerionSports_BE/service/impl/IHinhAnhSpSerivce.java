package com.example.AerionSports_BE.service.impl;



import com.example.AerionSports_BE.dto.request.HinhAnhSpFilter;
import com.example.AerionSports_BE.dto.request.HinhAnhSpRequest;
import com.example.AerionSports_BE.dto.response.HinhAnhSpResponse;

import java.util.List;

public interface IHinhAnhSpSerivce {
    List<HinhAnhSpResponse> getImages(HinhAnhSpFilter f);
    HinhAnhSpResponse save(HinhAnhSpRequest r);
    void delete(Integer id);
}
