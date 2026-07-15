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
public class SanPhamPosServiceImpl implements SanPhamPosService {

    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final com.example.AerionSports_BE.repository.ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository; // ✅ thêm

    @Override
    public Page<SanPhamPosResponse> locSanPham(
            String keyword, Integer idMauSac, Integer idTrongLuong,
            BigDecimal giaMin, BigDecimal giaMax, Integer trangThai,
            int page, int size) {

        Integer trangThaiFilter = 1;
        Pageable pageable = PageRequest.of(page, size);

        return chiTietSanPhamRepository.locSanPhamPos(
                keyword, idMauSac, idTrongLuong,
                giaMin, giaMax,
                trangThai,
                trangThaiFilter,
                pageable
        ).map(ct -> {
            SanPhamPosResponse res = new SanPhamPosResponse(ct);
            BigDecimal giaGoc = ct.getGiaBan();
            BigDecimal giaSauGiam = tinhGiaSauGiam(ct);
            res.setGia(giaSauGiam);      // ✅ giá thực tế thêm vào hóa đơn
            res.setGiaGoc(giaGoc);       // ✅ giá gốc để hiện gạch ngang
            return res;
        });
    }

    private BigDecimal tinhGiaSauGiam(com.example.AerionSports_BE.entity.ChiTietSanPham ctsp) {
        BigDecimal giaGoc = ctsp.getGiaBan();
        java.time.LocalDateTime gioHienTaiVietNam =
                java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        var discountLinks = chiTietDotGiamGiaRepository
                .findBestActiveByChiTietSanPhamId(ctsp.getId(), gioHienTaiVietNam);

        if (discountLinks != null && !discountLinks.isEmpty()) {
            var dgg = discountLinks.get(0).getDotGiamGia();
            if (dgg != null && dgg.getGiaTriGiam() != null) {
                BigDecimal heSo = BigDecimal.valueOf(100).subtract(dgg.getGiaTriGiam());
                return giaGoc.multiply(heSo)
                        .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
        }
        return giaGoc;
    }

    @Override
    public Map<String, BigDecimal> getKhoangGia() {
        Map<String, BigDecimal> data = new HashMap<>();
        data.put("giaMin", chiTietSanPhamRepository.getGiaMin());
        data.put("giaMax", chiTietSanPhamRepository.getGiaMax());
        return data;
    }
}