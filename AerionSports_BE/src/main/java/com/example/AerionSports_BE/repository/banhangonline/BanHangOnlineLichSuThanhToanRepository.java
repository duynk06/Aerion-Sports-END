package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BanHangOnlineLichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {

    Optional<LichSuThanhToan> findFirstByHoaDon_IdAndTrangThaiThanhToanOrderByNgayThanhToanDesc(
            Integer idHoaDon,
            String trangThaiThanhToan
    );

    boolean existsByHoaDon_IdAndTrangThaiThanhToan(Integer idHoaDon, String trangThaiThanhToan);

    List<LichSuThanhToan> findByHoaDon_IdOrderByNgayThanhToanDesc(Integer idHoaDon);
}
