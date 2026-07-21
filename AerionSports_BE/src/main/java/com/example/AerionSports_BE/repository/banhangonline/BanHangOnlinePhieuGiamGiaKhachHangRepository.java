package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.PhieuGiamGiaKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BanHangOnlinePhieuGiamGiaKhachHangRepository extends JpaRepository<PhieuGiamGiaKhachHang, Integer> {

    boolean existsByPhieuGiamGia_Id(Integer idPhieuGiamGia);

    @Query("""
        SELECT pgk FROM PhieuGiamGiaKhachHang pgk
        WHERE pgk.phieuGiamGia.id = :idPhieuGiamGia
          AND pgk.khachHang.id = :idKhachHang
          AND (pgk.daSuDung IS NULL OR pgk.daSuDung = false)
          AND (pgk.trangThai IS NULL OR pgk.trangThai = 1)
    """)
    Optional<PhieuGiamGiaKhachHang> findChuaSuDung(
            @Param("idPhieuGiamGia") Integer idPhieuGiamGia,
            @Param("idKhachHang") Integer idKhachHang
    );
}
