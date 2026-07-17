package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    boolean existsByMaSanPham(String ma);

    boolean existsByMaSanPhamAndIdNot(String ma, Integer id);

    @Query(value = """
SELECT s
FROM SanPham s
LEFT JOIN s.idThuongHieu
LEFT JOIN s.idXuatXu
LEFT JOIN s.idDoCung
LEFT JOIN s.idDiemCanBang
LEFT JOIN s.idDanhMuc
LEFT JOIN s.idChuViCanVot
WHERE
    (:k IS NULL OR :k = '' OR
     LOWER(s.maSanPham) LIKE LOWER(CONCAT('%', :k, '%'))
     OR LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :k, '%')))
AND (:th IS NULL OR s.idThuongHieu.id = :th)
AND (:xx IS NULL OR s.idXuatXu.id = :xx)
AND (:dm IS NULL OR s.idDanhMuc.id = :dm)
AND (:cv IS NULL OR s.idChuViCanVot.id = :cv)
AND (:dc IS NULL OR s.idDoCung.id = :dc)
AND (:dcb IS NULL OR s.idDiemCanBang.id = :dcb)
AND (:t IS NULL OR s.trangThai = :t)
AND (:soLuongMin IS NULL OR (
    SELECT COALESCE(SUM(c.soLuong), 0)
    FROM ChiTietSanPham c
    WHERE c.idSanPham.id = s.id
) >= :soLuongMin)
AND (:giaMax IS NULL OR EXISTS (
    SELECT 1 FROM ChiTietSanPham c2
    WHERE c2.idSanPham.id = s.id
    AND c2.giaBan <= :giaMax
))
ORDER BY s.id DESC
""",
            countQuery = """
SELECT COUNT(s)
FROM SanPham s
WHERE
    (:k IS NULL OR :k = '' OR
     LOWER(s.maSanPham) LIKE LOWER(CONCAT('%', :k, '%'))
     OR LOWER(s.tenSanPham) LIKE LOWER(CONCAT('%', :k, '%')))
AND (:th IS NULL OR s.idThuongHieu.id = :th)
AND (:xx IS NULL OR s.idXuatXu.id = :xx)
AND (:dm IS NULL OR s.idDanhMuc.id = :dm)
AND (:cv IS NULL OR s.idChuViCanVot.id = :cv)
AND (:dc IS NULL OR s.idDoCung.id = :dc)
AND (:dcb IS NULL OR s.idDiemCanBang.id = :dcb)
AND (:t IS NULL OR s.trangThai = :t)
AND (:soLuongMin IS NULL OR (
    SELECT COALESCE(SUM(c.soLuong), 0)
    FROM ChiTietSanPham c
    WHERE c.idSanPham.id = s.id
) >= :soLuongMin)
AND (:giaMax IS NULL OR EXISTS (
    SELECT 1 FROM ChiTietSanPham c2
    WHERE c2.idSanPham.id = s.id
    AND c2.giaBan <= :giaMax
))
""")
    Page<SanPham> search(
            @Param("k") String keyword,
            @Param("th") Integer idThuongHieu,
            @Param("xx") Integer idXuatXu,
            @Param("dm") Integer idDanhMuc,
            @Param("cv") Integer idChuViCanVot,
            @Param("dc") Integer idDoCung,
            @Param("dcb") Integer idDiemCanBang,
            @Param("t") Integer trangThai,
            @Param("soLuongMin") Integer soLuongMin,
            @Param("giaMax") Long giaMax,
            Pageable pageable);
    @Modifying
    @Transactional
    @Query("UPDATE ChiTietSanPham c SET c.trangThai = :trangThaiMoi WHERE c.idSanPham.id = :sanPhamId")
    void updateTrangThaiBienTheTheoSanPhamCha(@Param("sanPhamId") Integer sanPhamId, @Param("trangThaiMoi") Integer trangThaiMoi);
}