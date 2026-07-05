package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.LichSuThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface LichSuThanhToanRepository extends JpaRepository<LichSuThanhToan, Integer> {
    List<LichSuThanhToan> findByHoaDon_IdOrderByNgayThanhToanDesc(Integer idHoaDon);
    List<LichSuThanhToan> findByHoaDonId(Integer hoaDonId);
}
