package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.response.SanPhamPosResponse;
import com.example.AerionSports_BE.repository.ChiTietSanPhamRepository;
import com.example.AerionSports_BE.repository.SanPhamRepository;
import com.example.AerionSports_BE.service.SanPhamPosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SanPhamPosServiceImpl
        implements SanPhamPosService {

    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    // SanPhamPosServiceImpl.java — sửa query
    @Override
    public Page<SanPhamPosResponse> locSanPham(
            String keyword, Integer idMauSac, Integer idTrongLuong,
            BigDecimal giaMin, BigDecimal giaMax, Integer trangThai,
            int page, int size) {

        // ✅ Luôn chỉ lấy trangThai = 1 (hoạt động)
        // Nếu caller truyền trangThai=null thì mặc định = 1
        // Nếu caller truyền trangThai=0 (hết hàng) thì lấy soLuong=0 nhưng vẫn phải trangThai=1
        Integer trangThaiFilter = 1; // ✅ Luôn cố định = 1

        Pageable pageable = PageRequest.of(page, size);
        return chiTietSanPhamRepository.locSanPhamPos(
                keyword, idMauSac, idTrongLuong,
                giaMin, giaMax,
                trangThai,         // ← tồn kho filter (null/0/1)
                trangThaiFilter,   // ← trangThai CTSP luôn = 1
                pageable
        ).map(SanPhamPosResponse::new);
    }
    @Override
    public Map<String, BigDecimal> getKhoangGia() {

        Map<String, BigDecimal> data = new HashMap<>();

        data.put(
                "giaMin",
                chiTietSanPhamRepository.getGiaMin()
        );

        data.put(
                "giaMax",
                chiTietSanPhamRepository.getGiaMax()
        );

        return data;
    }
}