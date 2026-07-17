package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.MauSac;
import com.example.AerionSports_BE.entity.TrongLuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {

    List<ChiTietSanPham> findByTrangThai(Integer trangThai);
    boolean existsByMaCtsp(String maCtsp);
    boolean existsByIdSanPham_IdAndIdMauSac_IdAndIdTrongLuong_Id(
            Integer idSanPham, Integer idMauSac, Integer idTrongLuong);

    // 🟢 BỔ SUNG: Lấy nhanh thực thể Màu Sắc bằng ID
    @Query("SELECT m FROM MauSac m WHERE m.id = :id")
    MauSac findMauSacById(@Param("id") Integer id);

    // 🟢 BỔ SUNG: Lấy nhanh thực thể Trọng Lượng bằng ID
    @Query("SELECT t FROM TrongLuong t WHERE t.id = :id")
    TrongLuong findTrongLuongById(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"idSanPham", "idMauSac", "idTrongLuong"})
    @Query("SELECT c FROM ChiTietSanPham c " +
            "JOIN FETCH c.idSanPham sp " +
            "LEFT JOIN FETCH c.idMauSac ms " +
            "LEFT JOIN FETCH c.idTrongLuong tl " +
            "LEFT JOIN FETCH sp.idChuViCanVot cvc " +
            "LEFT JOIN FETCH sp.idDoCung dc " +
            "LEFT JOIN FETCH sp.idDiemCanBang dcb " +
            "LEFT JOIN FETCH sp.idChatLieuThanVot cltv " +
            "LEFT JOIN FETCH sp.idChatLieuKhungVot clk " +
            "LEFT JOIN FETCH sp.idDanhMuc dm " +
            "WHERE " +
            "(:k IS NULL OR LOWER(c.maCtsp) LIKE LOWER(CONCAT('%', :k, '%')) OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :k, '%')) OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :k, '%'))) " +
            "AND (:sp IS NULL OR sp.id = :sp) " +
            "AND (:dm IS NULL OR dm.id = :dm) " +
            "AND (:ms IS NULL OR ms.id = :ms) " +
            "AND (:tl IS NULL OR tl.id = :tl) " +
            "AND (:cvc IS NULL OR cvc.id = :cvc) " +
            "AND (:dc IS NULL OR dc.id = :dc) " +
            "AND (:dcb IS NULL OR dcb.id = :dcb) " +
            "AND (:tt IS NULL OR c.trangThai = :tt) " +
            "AND (:giaTu IS NULL OR c.giaBan >= :giaTu) " +
            "AND (:giaDen IS NULL OR c.giaBan <= :giaDen) " +
            "ORDER BY c.ngayCapNhat DESC, c.ngayTao DESC")
    Page<ChiTietSanPham> search(@Param("k") String keyword, @Param("sp") Integer idSanPham,
                                @Param("dm") Integer idDanhMuc, @Param("ms") Integer idMauSac,
                                @Param("tl") Integer idTrongLuong, @Param("cvc") Integer idChuViCanVot,
                                @Param("dc") Integer idDoCung, @Param("dcb") Integer idDiemCanBang,
                                @Param("tt") Integer trangThai, @Param("giaTu") BigDecimal giaTu,
                                @Param("giaDen") BigDecimal giaDen, Pageable p);

    @Query("SELECT c FROM ChiTietSanPham c " +
            "JOIN FETCH c.idSanPham sp " +
            "LEFT JOIN FETCH sp.idXuatXu xx " +
            "LEFT JOIN FETCH sp.idThuongHieu th " +
            "LEFT JOIN FETCH sp.idChuViCanVot cv " +
            "LEFT JOIN FETCH sp.idDoCung dc " +
            "LEFT JOIN FETCH sp.idDiemCanBang dcb " +
            "LEFT JOIN FETCH sp.idChatLieuThanVot cltv " +
            "LEFT JOIN FETCH sp.idChatLieuKhungVot clk " +
            "LEFT JOIN FETCH sp.idDanhMuc dm " +
            "LEFT JOIN FETCH c.idMauSac ms " +
            "LEFT JOIN FETCH c.idTrongLuong tl " +
            "WHERE c.trangThai = 1 AND " +
            "(:keyword IS NULL OR LOWER(c.maCtsp) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(sp.maSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY c.id DESC")
    List<ChiTietSanPham> searchActiveProducts(@Param("keyword") String keyword);

    @EntityGraph(attributePaths = {
            "idSanPham",
            "idMauSac",
            "idTrongLuong",
            "hinhAnhs"
    })
    @Query("""
    SELECT ct FROM ChiTietSanPham ct
    JOIN ct.idSanPham sp
    WHERE ct.trangThai = :trangThaiCtsp
    AND sp.trangThai = 1
    AND (:keyword IS NULL OR ct.maCtsp LIKE %:keyword%
         OR sp.tenSanPham LIKE %:keyword%)
    AND (:idMauSac IS NULL OR ct.idMauSac.id = :idMauSac)
    AND (:idTrongLuong IS NULL OR ct.idTrongLuong.id = :idTrongLuong)
    AND (:giaMin IS NULL OR ct.giaBan >= :giaMin)
    AND (:giaMax IS NULL OR ct.giaBan <= :giaMax)
    AND (:trangThai IS NULL
         OR (:trangThai = 1 AND ct.soLuong > 0)
         OR (:trangThai = 0 AND ct.soLuong = 0))
    ORDER BY ct.id DESC
    """)
    Page<ChiTietSanPham> locSanPhamPos(
            @Param("keyword") String keyword,
            @Param("idMauSac") Integer idMauSac,
            @Param("idTrongLuong") Integer idTrongLuong,
            @Param("giaMin") BigDecimal giaMin,
            @Param("giaMax") BigDecimal giaMax,
            @Param("trangThai") Integer trangThai,
            @Param("trangThaiCtsp") Integer trangThaiCtsp,
            Pageable pageable
    );

    @Query("""
    SELECT MIN(ct.giaBan)
    FROM ChiTietSanPham ct
    """)
    BigDecimal getGiaMin();

    @Query("""
    SELECT MAX(ct.giaBan)
    FROM ChiTietSanPham ct
    """)
    BigDecimal getGiaMax();

    Optional<ChiTietSanPham> findByMaCtsp(String maCtsp);
}