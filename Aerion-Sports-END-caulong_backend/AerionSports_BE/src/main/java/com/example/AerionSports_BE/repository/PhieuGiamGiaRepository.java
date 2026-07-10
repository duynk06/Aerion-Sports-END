package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {

    Optional<PhieuGiamGia> findByMaPhieuGiamGia(String maPhieuGiamGia);

    boolean existsByMaPhieuGiamGia(String maPhieuGiamGia);

    // PhieuGiamGiaRepository.java — thêm query
    @Query("""
    SELECT p FROM PhieuGiamGia p
    WHERE p.trangThai = 1
      AND p.ngayBatDau <= :now
      AND p.ngayKetThuc >= :now
      AND (p.soLuong IS NULL OR p.soLuongDaSuDung < p.soLuong)
    ORDER BY p.giaTriDonToiThieu ASC
""")
    List<PhieuGiamGia> findPhieuConHieuLuc(@Param("now") LocalDateTime now);
}