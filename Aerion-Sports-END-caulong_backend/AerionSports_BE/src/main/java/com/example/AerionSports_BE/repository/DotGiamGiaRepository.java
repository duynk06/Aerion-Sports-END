package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia, Integer> {

    Optional<DotGiamGia> findByMaDotGiamGia(String maDotGiamGia);

    boolean existsByMaDotGiamGia(String maDotGiamGia);

    @Query("SELECT d FROM DotGiamGia d WHERE " +
           "(:keyword IS NULL OR d.maDotGiamGia LIKE %:keyword% OR d.tenDotGiamGia LIKE %:keyword%) " +
           "AND (:trangThai IS NULL OR d.trangThai = :trangThai) " +
           "AND (:tuNgay IS NULL OR d.ngayBatDau >= :tuNgay) " +
           "AND (:denNgay IS NULL OR d.ngayBatDau <= :denNgay) " +
           "ORDER BY d.id DESC")
    Page<DotGiamGia> searchDotGiamGia(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") LocalDateTime tuNgay,
            @Param("denNgay") LocalDateTime denNgay,
            Pageable pageable);

    @Query("SELECT d FROM DotGiamGia d WHERE d.trangThai = :trangThai ORDER BY d.id DESC")
    List<DotGiamGia> findByTrangThai(@Param("trangThai") Integer trangThai);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(d.maDotGiamGia, 4, LENGTH(d.maDotGiamGia)) AS int)), 0) FROM DotGiamGia d WHERE d.maDotGiamGia LIKE 'DGG%'")
    Integer findMaxMaDotGiamGia();
}
