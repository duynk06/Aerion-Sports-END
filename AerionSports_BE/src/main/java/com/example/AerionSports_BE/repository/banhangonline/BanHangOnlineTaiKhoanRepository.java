package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BanHangOnlineTaiKhoanRepository extends JpaRepository<TaiKhoan, Integer> {
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);

    Optional<TaiKhoan> findByTenDangNhapAndTrangThai(String tenDangNhap, Integer trangThai);
}
