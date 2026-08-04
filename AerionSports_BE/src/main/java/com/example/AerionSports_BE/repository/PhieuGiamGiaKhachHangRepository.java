package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.PhieuGiamGiaKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PhieuGiamGiaKhachHangRepository extends JpaRepository<PhieuGiamGiaKhachHang, Integer> {
    @Query("""
        SELECT pgk FROM PhieuGiamGiaKhachHang pgk
        WHERE pgk.phieuGiamGia.id = :idPhieu
          AND pgk.khachHang.id = :idKhachHang
          AND pgk.daSuDung = false
        """)
    Optional<PhieuGiamGiaKhachHang> findChuaSuDung(
            @Param("idPhieu") Integer idPhieu,
            @Param("idKhachHang") Integer idKhachHang
    );

    @Query("""
        SELECT pgk FROM PhieuGiamGiaKhachHang pgk
        WHERE pgk.phieuGiamGia.id = :idPhieu
          AND pgk.khachHang.id = :idKhachHang
          AND pgk.daSuDung = true
        """)
    Optional<PhieuGiamGiaKhachHang> findDaSuDung(
            @Param("idPhieu") Integer idPhieu,
            @Param("idKhachHang") Integer idKhachHang
    );
}
