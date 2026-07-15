package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.ChiTietDotGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, Integer> {

    List<ChiTietDotGiamGia> findByDotGiamGia_Id(Integer dotGiamGiaId);

    void deleteByDotGiamGia_Id(Integer dotGiamGiaId);

    @Query("SELECT ct FROM ChiTietDotGiamGia ct " +
            "JOIN FETCH ct.dotGiamGia dgg " +
            "JOIN FETCH ct.chiTietSanPham ctsp " +
            "JOIN FETCH ctsp.idSanPham sp " +
            "LEFT JOIN FETCH sp.idXuatXu xx " +
            "LEFT JOIN FETCH sp.idThuongHieu th " +
            "LEFT JOIN FETCH ctsp.idMauSac ms " +
            "LEFT JOIN FETCH ctsp.idTrongLuong tl " +
            "LEFT JOIN FETCH sp.idChuViCanVot cv " +
            "LEFT JOIN FETCH sp.idDoCung dc " +
            "LEFT JOIN FETCH sp.idDiemCanBang dcb " +
            "LEFT JOIN FETCH sp.idChatLieuThanVot cltv " +
            "LEFT JOIN FETCH sp.idChatLieuKhungVot clk " +
            "LEFT JOIN FETCH sp.idDanhMuc dm " +
            "WHERE ct.dotGiamGia.id = :dotGiamGiaId")
    List<ChiTietDotGiamGia> findActiveByDotGiamGiaId(@Param("dotGiamGiaId") Integer dotGiamGiaId);

    @Query("SELECT ct FROM ChiTietDotGiamGia ct " +
            "JOIN FETCH ct.dotGiamGia dgg " +
            "WHERE ct.chiTietSanPham.id = :chiTietSanPhamId " +
            "AND dgg.trangThai = 2 " +
            "AND (:thoiDiem IS NOT NULL OR :thoiDiem IS NULL) " +
            "ORDER BY dgg.giaTriGiam DESC, dgg.id DESC")
    List<ChiTietDotGiamGia> findBestActiveByChiTietSanPhamId(
            @Param("chiTietSanPhamId") Integer chiTietSanPhamId,
            @Param("thoiDiem") LocalDateTime thoiDiem);

    @Query("SELECT ct.chiTietSanPham.id, dgg.giaTriGiam " +
            "FROM ChiTietDotGiamGia ct " +
            "JOIN ct.dotGiamGia dgg " +
            "WHERE ct.chiTietSanPham.id IN :chiTietSanPhamIds " +
            "AND dgg.trangThai = 2 " +
            "AND (:thoiDiem IS NOT NULL OR :thoiDiem IS NULL) " +
            "ORDER BY ct.chiTietSanPham.id ASC, dgg.giaTriGiam DESC, dgg.id DESC")
    List<Object[]> findBestActiveDiscountRows(
            @Param("chiTietSanPhamIds") List<Integer> chiTietSanPhamIds,
            @Param("thoiDiem") LocalDateTime thoiDiem);
}
