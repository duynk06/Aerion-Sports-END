package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.dto.response.ChiTietHoaDonProjection;
import com.example.AerionSports_BE.entity.ChiTietHoaDon;
import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    @Query(value = """
    SELECT
        cthd.id AS id,
        sp.ma_san_pham AS maSanPham,
        sp.ten_san_pham AS tenSanPham,
        ms.ten_mau_sac AS mauSac,
        tl.ten_trong_luong AS trongLuong,
        cthd.so_luong AS soLuong,
        cthd.don_gia AS donGia,
        cthd.thanh_tien AS thanhTien,
        ha.duong_dan_anh AS anh,
        ctsp.gia_ban AS giaGoc,
        dgg_apply.ten_dot_giam_gia AS tenDotGiamGia
    FROM chi_tiet_hoa_don cthd
    JOIN chi_tiet_san_pham ctsp ON cthd.id_chi_tiet_san_pham = ctsp.id
    JOIN san_pham sp ON ctsp.id_san_pham = sp.id
    LEFT JOIN mau_sac ms ON ctsp.id_mau_sac = ms.id
    LEFT JOIN trong_luong tl ON ctsp.id_trong_luong = tl.id
    LEFT JOIN hinh_anh_sp ha ON ha.id_san_pham_chi_tiet = ctsp.id AND ha.la_anh_chinh = 1
    JOIN hoa_don hd ON cthd.id_hoa_don = hd.id
    OUTER APPLY (
        SELECT TOP 1 dgg.ten_dot_giam_gia
        FROM chi_tiet_dot_giam_gia cdgg
        JOIN dot_giam_gia dgg ON cdgg.id_dot_giam_gia = dgg.id
        WHERE cdgg.id_chi_tiet_san_pham = ctsp.id
          AND hd.ngay_tao BETWEEN dgg.ngay_bat_dau AND dgg.ngay_ket_thuc
        ORDER BY dgg.gia_tri_giam DESC, dgg.id DESC
    ) dgg_apply
    WHERE cthd.id_hoa_don = :idHoaDon
    """, nativeQuery = true)
    List<ChiTietHoaDonProjection> getChiTietHoaDon(@Param("idHoaDon") Integer idHoaDon);

    @Query("SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.hoaDon.id = :idHoaDon")
    BigDecimal tinhTongTienHang(@Param("idHoaDon") Integer idHoaDon);
    @Query("""
    SELECT cthd FROM ChiTietHoaDon cthd
    JOIN FETCH cthd.chiTietSanPham ctsp
    JOIN FETCH ctsp.idSanPham sp
    JOIN FETCH ctsp.idTrongLuong tl
    JOIN FETCH ctsp.idMauSac ms
    WHERE cthd.hoaDon.id = :idHoaDon
    """)
    List<ChiTietHoaDon> findByHoaDonIdWithDetail(@Param("idHoaDon") Integer idHoaDon);
}
