package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.LichSuGiaoCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface LichSuGiaoCaRepository extends JpaRepository<LichSuGiaoCa, Integer> {
    @Query(value = "SELECT TOP 1 * FROM lich_su_giao_ca " +
            "WHERE id_nhan_vien_ca_truoc = :idNhanVien AND trang_thai = 0 " +
            "ORDER BY thoi_gian_vao DESC", nativeQuery = true)
    Optional<LichSuGiaoCa> findActiveCaByNhanVien(@Param("idNhanVien") Integer idNhanVien);

    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) FROM thanh_toan tt " +
            "JOIN hoa_don hd ON tt.id_hoa_don = hd.id " +
            "JOIN phuong_thuc_thanh_toan pttt ON tt.id_hinh_thuc_thanh_toan = pttt.id " +
            "WHERE hd.id_nhan_vien = :idNhanVien " +
            "AND tt.trang_thai_thanh_toan = N'Thành công' " +
            "AND pttt.ma_hinh_thuc = 'TT01' " +
            "AND tt.ngay_tao >= :thoiGianVao", nativeQuery = true)
    BigDecimal tinhTienMatDoanhThu(@Param("idNhanVien") Integer idNhanVien,
                                   @Param("thoiGianVao") LocalDateTime thoiGianVao);

    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) FROM thanh_toan tt " +
            "JOIN hoa_don hd ON tt.id_hoa_don = hd.id " +
            "JOIN phuong_thuc_thanh_toan pttt ON tt.id_hinh_thuc_thanh_toan = pttt.id " +
            "WHERE hd.id_nhan_vien = :idNhanVien " +
            "AND tt.trang_thai_thanh_toan = N'Thành công' " +
            "AND pttt.ma_hinh_thuc IN ('TT02', 'TT03') " +
            "AND tt.ngay_tao >= :thoiGianVao", nativeQuery = true)
    BigDecimal tinhTienChuyenKhoanDoanhThu(@Param("idNhanVien") Integer idNhanVien,
                                           @Param("thoiGianVao") LocalDateTime thoiGianVao);

    @Query(value = "SELECT TOP 1 ma_ca FROM lich_su_giao_ca " +
            "WHERE ma_ca LIKE 'CA%' " +
            "ORDER BY id DESC", nativeQuery = true)
    String findLastMaCa();
}