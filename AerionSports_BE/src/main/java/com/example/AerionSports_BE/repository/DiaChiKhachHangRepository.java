package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.dto.response.DiaChiKhachHangResponse;
import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Integer> {

    // 🌟 SỬA: đổi tên method — Spring Data JPA tự sinh câu SQL dựa theo
    // đường dẫn field thật (khachHang.id), không phải "idKhachHang"
    List<DiaChiKhachHang> findByKhachHangId(Integer khachHangId);

    Optional<DiaChiKhachHang> findByIdAndKhachHangId(Integer id, Integer khachHangId);

    @Query(value = """
        SELECT
            dc.id AS id,
            dc.nguoi_nhan AS nguoiNhan,
            dc.sdt AS sdt,
            dc.tinh_thanh AS tinhThanh,
            dc.phuong_xa AS phuongXa,
            dc.dia_chi_chi_tiet AS diaChiChiTiet,
            dc.mac_dinh AS macDinh,
            ISNULL(
                dc.dia_chi_chi_tiet + ', ' + dc.phuong_xa + ', ' + dc.tinh_thanh,
                ''
            ) AS diaChiDayDu
        FROM dia_chi_khach_hang dc
        WHERE dc.id_khach_hang = :idKhachHang
        ORDER BY dc.mac_dinh DESC
    """, nativeQuery = true)
    List<DiaChiKhachHangResponse> findDiaChiByKhachHang(@Param("idKhachHang") Integer idKhachHang);

    @Modifying
    @Query("UPDATE DiaChiKhachHang d SET d.macDinh = false WHERE d.khachHang.id = :idKhachHang")
    void boMacDinhTheoKhachHang(@Param("idKhachHang") Integer idKhachHang);
}