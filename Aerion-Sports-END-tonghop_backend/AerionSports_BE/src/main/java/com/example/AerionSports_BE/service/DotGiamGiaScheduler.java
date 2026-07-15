package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.DotGiamGia;
import com.example.AerionSports_BE.repository.DotGiamGiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled task tự động cập nhật trạng thái đợt giảm giá
 * - Chuyển "Sắp diễn ra" -> "Đang diễn ra" khi đến ngày bắt đầu
 * - Chuyển "Đang diễn ra" -> "Đã kết thúc" khi quá ngày kết thúc
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DotGiamGiaScheduler {

    private final DotGiamGiaRepository dotGiamGiaRepository;

    /**
     * Chạy mỗi phút để cập nhật trạng thái
     */
    @Scheduled(fixedRate = 60000) // 1 phút
    @Transactional
    public void updateTrangThaiDotGiamGia() {
        LocalDateTime now = LocalDateTime.now();

        // Chuyển "Sắp diễn ra" (1) -> "Đang diễn ra" (2)
        List<DotGiamGia> sapDienRa = dotGiamGiaRepository.findByTrangThai(1);
        for (DotGiamGia dgg : sapDienRa) {
            if (dgg.getNgayBatDau() != null && !now.isBefore(dgg.getNgayBatDau())) {
                dgg.setTrangThai(2);
                dotGiamGiaRepository.save(dgg);
                log.info("Đợt giảm giá {} đã chuyển sang trạng thái 'Đang diễn ra'", dgg.getMaDotGiamGia());
            }
        }

        // Chuyển "Đang diễn ra" (2) -> "Đã kết thúc" (3)
        List<DotGiamGia> dangDienRa = dotGiamGiaRepository.findByTrangThai(2);
        for (DotGiamGia dgg : dangDienRa) {
            if (dgg.getNgayKetThuc() != null && now.isAfter(dgg.getNgayKetThuc())) {
                dgg.setTrangThai(3);
                dotGiamGiaRepository.save(dgg);
                log.info("Đợt giảm giá {} đã chuyển sang trạng thái 'Đã kết thúc'", dgg.getMaDotGiamGia());
            }
        }
    }
}
