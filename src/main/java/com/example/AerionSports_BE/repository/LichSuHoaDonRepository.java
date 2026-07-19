package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LichSuHoaDonRepository
        extends JpaRepository<LichSuHoaDon, Integer> {

    List<LichSuHoaDon>
    findByHoaDon_IdOrderByThoiGianHanhDongDesc(
            Integer idHoaDon
    );
    // LichSuHoaDonRepository.java
    @Modifying
    @Query("DELETE FROM LichSuHoaDon l WHERE l.hoaDon.id = :idHoaDon")
    void deleteByHoaDonId(@Param("idHoaDon") Integer idHoaDon);
    @Query("""
    SELECT lshd FROM LichSuHoaDon lshd
    LEFT JOIN FETCH lshd.nhanVien nv
    WHERE lshd.hoaDon.id = :idHoaDon
    ORDER BY lshd.thoiGianHanhDong DESC
    """)
    List<LichSuHoaDon> findByHoaDonIdWithNhanVien(@Param("idHoaDon") Integer idHoaDon);
}