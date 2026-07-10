package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.response.SanPhamPosResponse;
import org.springframework.data.domain.Page;
import java.util.Map;
import java.math.BigDecimal;

public interface SanPhamPosService {

    Page<SanPhamPosResponse> locSanPham(
            String keyword,
            Integer idMauSac,
            Integer idTrongLuong,
            BigDecimal giaMin,
            BigDecimal giaMax,
            Integer trangThai,
            int page,
            int size
    );


    Map<String, BigDecimal> getKhoangGia();
}