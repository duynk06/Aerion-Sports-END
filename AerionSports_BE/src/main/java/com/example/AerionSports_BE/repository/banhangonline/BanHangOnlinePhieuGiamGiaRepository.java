package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BanHangOnlinePhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    Optional<PhieuGiamGia> findByMaPhieuGiamGia(String maPhieuGiamGia);

    @Query("""
        SELECT p FROM PhieuGiamGia p
        WHERE p.trangThai = 1
          AND (p.ngayBatDau IS NULL OR p.ngayBatDau <= :now)
          AND (p.ngayKetThuc IS NULL OR p.ngayKetThuc >= :now)
          AND (p.soLuong IS NULL OR COALESCE(p.soLuongDaSuDung, 0) < p.soLuong)
        ORDER BY p.giaTriDonToiThieu ASC
    """)
    List<PhieuGiamGia> findPhieuConHieuLuc(@Param("now") LocalDateTime now);
}
