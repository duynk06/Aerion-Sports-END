package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("""
        SELECT hd
        FROM HoaDon hd
        LEFT JOIN hd.nhanVien nv
        WHERE
            LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(nv.tenNv) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR hd.sdtNguoiNhan LIKE CONCAT('%', :keyword, '%')
    """)
    List<HoaDon> search(@Param("keyword") String keyword);

    @Query(value = """
    SELECT hd
    FROM HoaDon hd
    LEFT JOIN hd.khachHang kh
    LEFT JOIN hd.nhanVien nv
    WHERE
        (:keyword IS NULL OR :keyword = '' OR
            LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(nv.tenNv) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR hd.sdtNguoiNhan LIKE CONCAT('%', :keyword, '%')
        )
        AND (:loaiHoaDon IS NULL OR hd.loaiHoaDon = :loaiHoaDon)
        AND (:trangThai IS NULL OR hd.trangThai = :trangThai)
        AND (:tuNgay IS NULL OR CAST(hd.ngayTao AS date) >= :tuNgay)
        AND (:denNgay IS NULL OR CAST(hd.ngayTao AS date) <= :denNgay)
    ORDER BY hd.id DESC
""", countQuery = """
    SELECT COUNT(hd)
    FROM HoaDon hd
    LEFT JOIN hd.nhanVien nv
    WHERE
        (:keyword IS NULL OR :keyword = '' OR
            LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(nv.tenNv) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR hd.sdtNguoiNhan LIKE CONCAT('%', :keyword, '%')
        )
        AND (:loaiHoaDon IS NULL OR hd.loaiHoaDon = :loaiHoaDon)
        AND (:trangThai IS NULL OR hd.trangThai = :trangThai)
        AND (:tuNgay IS NULL OR CAST(hd.ngayTao AS date) >= :tuNgay)
        AND (:denNgay IS NULL OR CAST(hd.ngayTao AS date) <= :denNgay)
""")
    Page<HoaDon> filterHoaDon(
            @Param("keyword") String keyword,
            @Param("loaiHoaDon") Integer loaiHoaDon, // Sửa thành Integer
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay,
            Pageable pageable
    );

    // ✅ THÊM: giống hệt filterHoaDon ở trên nhưng KHÔNG phân trang — dùng riêng cho xuất Excel
    // (xuất toàn bộ dữ liệu khớp bộ lọc hiện tại, không giới hạn theo trang đang xem)
    @Query("""
    SELECT hd
    FROM HoaDon hd
    LEFT JOIN hd.khachHang kh
    LEFT JOIN hd.nhanVien nv
    WHERE
        (:keyword IS NULL OR :keyword = '' OR
            LOWER(hd.maHoaDon) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(nv.tenNv) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(hd.tenNguoiNhan) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR hd.sdtNguoiNhan LIKE CONCAT('%', :keyword, '%')
        )
        AND (:loaiHoaDon IS NULL OR hd.loaiHoaDon = :loaiHoaDon)
        AND (:trangThai IS NULL OR hd.trangThai = :trangThai)
        AND (:tuNgay IS NULL OR CAST(hd.ngayTao AS date) >= :tuNgay)
        AND (:denNgay IS NULL OR CAST(hd.ngayTao AS date) <= :denNgay)
    ORDER BY hd.id DESC
""")
    List<HoaDon> filterHoaDonKhongPhanTrang(
            @Param("keyword") String keyword,
            @Param("loaiHoaDon") Integer loaiHoaDon,
            @Param("trangThai") Integer trangThai,
            @Param("tuNgay") LocalDate tuNgay,
            @Param("denNgay") LocalDate denNgay
    );

    // HoaDonRepository.java

    @Query("""
    SELECT hd FROM HoaDon hd
    LEFT JOIN FETCH hd.chiTietHoaDons cthd
    LEFT JOIN FETCH cthd.chiTietSanPham ctsp
    LEFT JOIN FETCH ctsp.idSanPham sp
    LEFT JOIN FETCH ctsp.idMauSac
    LEFT JOIN FETCH ctsp.idTrongLuong
    LEFT JOIN FETCH hd.khachHang
    WHERE hd.id = :id
""")
    HoaDon findByIdWithChiTiet(@Param("id") Integer id);


    // ================= TRUY VẤN JPQL PHỤC VỤ 4 Ô THÈ THỐNG KÊ TỔNG QUAN HÀNG TRÊN =================
    @Query("SELECT COALESCE(SUM(h.tongTienThanhToan), 0) FROM HoaDon h WHERE h.trangThai = 5 AND h.ngayTao BETWEEN :start AND :end")
    BigDecimal sumDoanhThuThucTe(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(cd.soLuong), 0) FROM ChiTietHoaDon cd WHERE cd.hoaDon.trangThai = 5 AND cd.hoaDon.ngayTao BETWEEN :start AND :end")
    Integer countSanPhamDaBanThucTe(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :start AND :end")
    Integer countTongDonHangPhatSinh(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.trangThai = :status AND h.ngayTao BETWEEN :start AND :end")
    Integer countDonHangTheoTrangThaiCucBo(@Param("status") Integer status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // ================= TRUY VẤN NATIVE SQL ĐỘNG ĐÃ TÍCH HỢP BỘ LỌC NGÀY CHO 4 KHỐI BẢNG DƯỚI =================

    // 1. Quét Top 10 sản phẩm bán chạy nhất trong khoảng ngày chọn
    @Query(value = "SELECT TOP 10 sp.ten_san_pham, SUM(cthd.so_luong) as da_ban, SUM(ctsp.so_luong) as ton " +
            "FROM chi_tiet_hoa_don cthd " +
            "JOIN chi_tiet_san_pham ctsp ON cthd.id_chi_tiet_san_pham = ctsp.id " +
            "JOIN san_pham sp ON ctsp.id_san_pham = sp.id " +
            "JOIN hoa_don hd ON cthd.id_hoa_don = hd.id " +
            "WHERE hd.trang_thai = 5 AND CAST(hd.ngay_tao AS date) BETWEEN :tuNgay AND :denNgay " +
            "GROUP BY sp.ten_san_pham ORDER BY da_ban DESC", nativeQuery = true)
    List<Object[]> queryTopBanChay(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    // 2. Gom nhóm đầy đủ 10 trạng thái đơn hàng (Có tính tỷ lệ hoàn thành ở Frontend)
    @Query(value = "SELECT " +
            "  CASE hd.trang_thai " +
            "    WHEN 0 THEN N'Đã hủy' WHEN 1 THEN N'Chờ xác nhận' WHEN 2 THEN N'Đang xử lý' " +
            "    WHEN 3 THEN N'Đang giao' WHEN 4 THEN N'Đã giao' WHEN 5 THEN N'Hoàn thành' " +
            "    WHEN 6 THEN N'Yêu cầu hoàn' WHEN 7 THEN N'Đã hoàn tiền' WHEN 8 THEN N'Đã xác nhận' " +
            "    ELSE N'Yêu cầu hủy' END, COUNT(hd.id) " +
            "FROM hoa_don hd WHERE CAST(hd.ngay_tao AS date) BETWEEN :tuNgay AND :denNgay " +
            "GROUP BY hd.trang_thai", nativeQuery = true)
    List<Object[]> queryTrangThaiDonHang(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    // 3. Quét danh sách khách hàng chi tiêu nhiều nhất trong khoảng ngày chọn
    @Query(value = "SELECT kh.ho_ten, kh.sdt, COUNT(hd.id) as so_don, SUM(hd.tong_tien_thanh_toan) as tong_chi " +
            "FROM hoa_don hd JOIN khach_hang kh ON hd.id_khach_hang = kh.id " +
            "WHERE hd.trang_thai = 5 AND CAST(hd.ngay_tao AS date) BETWEEN :tuNgay AND :denNgay " +
            "GROUP BY kh.ho_ten, kh.sdt ORDER BY tong_chi DESC", nativeQuery = true)
    List<Object[]> queryKhachHangTiemNang(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    // 4. Quét sản phẩm bán chậm & lượng tồn kho trong khoảng ngày chọn
    @Query(value = "SELECT sp.ten_san_pham, COALESCE(SUM(cthd.so_luong), 0) as da_ban, SUM(ctsp.so_luong) as ton " +
            "FROM san_pham sp " +
            "JOIN chi_tiet_san_pham ctsp ON sp.id = ctsp.id_san_pham " +
            "LEFT JOIN chi_tiet_hoa_don cthd ON ctsp.id = cthd.id_chi_tiet_san_pham " +
            "WHERE CAST(ctsp.ngay_tao AS date) <= :denNgay " +
            "GROUP BY sp.ten_san_pham ORDER BY da_ban ASC, ton DESC", nativeQuery = true)
    List<Object[]> querySanPhamBanChamTonKho(@Param("denNgay") LocalDate denNgay);


    // ================= TRUY VẤN NATIVE SQL ĐỘNG CHO BIỂU ĐỒ (ĐÃ SỬA CONVERT DATE CHUẨN XÁC) =================

    // 5. 🌟 ĐÃ SỬA: Truy vấn doanh thu theo từng ngày của tháng (Ép kiểu DATE phẳng tránh lệch múi giờ)
    @Query(value = "SELECT DAY(CONVERT(DATE, hd.ngay_tao)) as ngay, SUM(hd.tong_tien_thanh_toan) " +
            "FROM hoa_don hd " +
            "WHERE hd.trang_thai = 5 " +
            "  AND MONTH(CONVERT(DATE, hd.ngay_tao)) = :thang " +
            "  AND YEAR(CONVERT(DATE, hd.ngay_tao)) = :nam " +
            "GROUP BY DAY(CONVERT(DATE, hd.ngay_tao))", nativeQuery = true)
    List<Object[]> queryDoanhThuTheoTungNgayTrongThang(@Param("thang") Integer thang, @Param("nam") Integer nam);

    // 6. 🌟 ĐÃ SỬA: Truy vấn doanh thu theo 12 tháng của Năm chọn
    @Query(value = "SELECT MONTH(CONVERT(DATE, hd.ngay_tao)) as thang, SUM(hd.tong_tien_thanh_toan) " +
            "FROM hoa_don hd " +
            "WHERE hd.trang_thai = 5 " +
            "  AND YEAR(CONVERT(DATE, hd.ngay_tao)) = :nam " +
            "GROUP BY MONTH(CONVERT(DATE, hd.ngay_tao))", nativeQuery = true)
    List<Object[]> queryDoanhThu12ThangTheoNam(@Param("nam") Integer nam);

    // 7. 🌟 ĐÃ SỬA: Truy vấn doanh thu theo 4 Quý của Năm chọn
    @Query(value = "SELECT DATEPART(QUARTER, CONVERT(DATE, hd.ngay_tao)) as quy, SUM(hd.tong_tien_thanh_toan) " +
            "FROM hoa_don hd " +
            "WHERE hd.trang_thai = 5 " +
            "  AND YEAR(CONVERT(DATE, hd.ngay_tao)) = :nam " +
            "GROUP BY DATEPART(QUARTER, CONVERT(DATE, hd.ngay_tao))", nativeQuery = true)
    List<Object[]> queryDoanhThu4QuyTheoNam(@Param("nam") Integer nam);
    List<HoaDon> findByTrangThai(Integer trangThai);
    long countByTrangThai(Integer trangThai);
}