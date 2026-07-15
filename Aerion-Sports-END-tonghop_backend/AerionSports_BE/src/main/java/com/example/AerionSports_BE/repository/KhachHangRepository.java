package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.dto.response.KhachHangPosResponse;
import com.example.AerionSports_BE.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    boolean existsByMaKhachHang(String maKhachHang);
    boolean existsBySdt(String sdt);
    boolean existsByEmail(String email);
    Optional<KhachHang> findFirstByOrderByIdDesc();
    @Query("SELECT DISTINCT kh FROM KhachHang kh LEFT JOIN FETCH kh.addresses WHERE kh.id = :id")
    Optional<KhachHang> findByIdWithAddresses(@Param("id") Integer id);
    @EntityGraph(attributePaths = {"addresses"})
    List<KhachHang> findAll();

    @Query(value = "SELECT kh.id, kh.ma_khach_hang, kh.ho_ten, kh.email, kh.sdt, kh.ngay_sinh, " +
            "(SELECT COUNT(*) FROM hoa_don hd WHERE hd.id_khach_hang = kh.id AND hd.trang_thai IN (1, 5)) as tongSoDonHang, " +
            "(SELECT MAX(hd.ngay_tao) FROM hoa_don hd WHERE hd.id_khach_hang = kh.id AND hd.trang_thai IN (1, 5)) as donHangGanNhat " +
                    "FROM khach_hang kh WHERE kh.trang_thai = 1", nativeQuery = true)
    List<Object[]> findAllKhachHangWithOrderSummary();

    // Bên trong interface KhachHangRepository của bạn:

    // ... (giữ nguyên phần trên)

    @Query(value = """
    SELECT
        kh.id AS id,
        kh.ho_ten AS hoTen,
        kh.sdt AS sdt,
        kh.email AS email,
        ISNULL(
            dc.dia_chi_chi_tiet + ', ' + dc.phuong_xa + ', ' + dc.tinh_thanh,
            N'Chưa cập nhật'
        ) AS diaChi,
        ISNULL(dc.tinh_thanh, '') AS tinhThanh
    FROM khach_hang kh
    LEFT JOIN dia_chi_khach_hang dc
        ON kh.id = dc.id_khach_hang AND dc.mac_dinh = 1
    WHERE kh.trang_thai = 1
      AND kh.id != 999
      AND (
          :keyword IS NULL OR :keyword = ''
          OR kh.ho_ten LIKE %:keyword%
          OR kh.sdt LIKE %:keyword%
      )
""", countQuery = """
    SELECT COUNT(kh.id)
    FROM khach_hang kh
    WHERE kh.trang_thai = 1
      AND kh.id != 999
      AND (
          :keyword IS NULL OR :keyword = ''
          OR kh.ho_ten LIKE %:keyword%
          OR kh.sdt LIKE %:keyword%
      )
""", nativeQuery = true)
    Page<KhachHangPosResponse> timKhachHangChoPos(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // KhachHangRepository.java — thêm query lấy địa chỉ mặc định
    @Query(value = """
    SELECT 
        ISNULL(dc.dia_chi_chi_tiet + ', ' + dc.phuong_xa + ', ' + dc.tinh_thanh, '') AS diaChi,
        ISNULL(dc.tinh_thanh, '') AS tinhThanh
    FROM dia_chi_khach_hang dc
    WHERE dc.id_khach_hang = :idKhachHang 
      AND dc.mac_dinh = 1
""", nativeQuery = true)
    Object[] findDiaChiMacDinh(@Param("idKhachHang") Integer idKhachHang);
}