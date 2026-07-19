package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BanHangOnlineHoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("""
    SELECT DISTINCT hd FROM HoaDon hd
    LEFT JOIN FETCH hd.khachHang kh
    LEFT JOIN FETCH kh.addresses dc
    LEFT JOIN FETCH hd.phieuGiamGia
    LEFT JOIN FETCH hd.nhanVien
    WHERE hd.maHoaDon = :maHoaDon
    """)
    Optional<HoaDon> findByMaHoaDonWithThongTin(@Param("maHoaDon") String maHoaDon);

    @Query("""
    SELECT DISTINCT hd FROM HoaDon hd
    LEFT JOIN FETCH hd.khachHang kh
    LEFT JOIN FETCH kh.addresses dc
    LEFT JOIN FETCH hd.phieuGiamGia
    LEFT JOIN FETCH hd.nhanVien
    WHERE kh.id = :idKhachHang
      AND hd.loaiHoaDon = 1
      AND (:trangThai IS NULL OR hd.trangThai = :trangThai)
      AND (
          :keyword IS NULL
          OR LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(COALESCE(hd.tenNguoiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(COALESCE(hd.sdtNguoiNhan, '')) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    ORDER BY hd.ngayTao DESC
    """)
    List<HoaDon> findDonHangOnlineTheoKhachHang(
            @Param("idKhachHang") Integer idKhachHang,
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai
    );
}
