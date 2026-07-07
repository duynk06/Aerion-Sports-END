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

    // Tra theo mã tự sinh DGGxxxx, thường dùng khi mở màn sửa hoặc kiểm tra tồn tại.
    Optional<DotGiamGia> findByMaDotGiamGia(String maDotGiamGia);

    // Kiểm tra nhanh xem mã đợt giảm giá đã tồn tại chưa.
    boolean existsByMaDotGiamGia(String maDotGiamGia);

    // Query lọc danh sách theo keyword, trạng thái và khoảng ngày bắt đầu, trả về dạng page.
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

    // Dùng cho scheduler để lấy tất cả campaign đang ở một trạng thái nhất định.
    @Query("SELECT d FROM DotGiamGia d WHERE d.trangThai = :trangThai ORDER BY d.id DESC")
    List<DotGiamGia> findByTrangThai(@Param("trangThai") Integer trangThai);

    // Sinh mã kế tiếp bằng cách lấy phần số lớn nhất hiện có trong mã DGGxxxx.
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(d.maDotGiamGia, 4, LENGTH(d.maDotGiamGia)) AS int)), 0) FROM DotGiamGia d WHERE d.maDotGiamGia LIKE 'DGG%'")
    Integer findMaxMaDotGiamGia();
}
