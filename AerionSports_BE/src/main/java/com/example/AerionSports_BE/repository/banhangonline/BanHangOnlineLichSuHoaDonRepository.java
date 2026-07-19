package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BanHangOnlineLichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    List<LichSuHoaDon> findByHoaDon_IdOrderByThoiGianHanhDongAsc(Integer idHoaDon);
}
