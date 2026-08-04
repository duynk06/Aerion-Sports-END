package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface BanHangOnlineKhachHangRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findFirstByOrderByIdDesc();

    boolean existsByMaKhachHang(String maKhachHang);

    @Query(value = """
        SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(ma_khach_hang, 3, LEN(ma_khach_hang)))), 0)
        FROM khach_hang
        WHERE ma_khach_hang LIKE 'KH%'
    """, nativeQuery = true)
    Integer findMaxSoThuTuMaKhachHang();

    Optional<KhachHang> findBySdt(String sdt);

    Optional<KhachHang> findByEmail(String email);
}
