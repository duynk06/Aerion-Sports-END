package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BanHangOnlineKhachHangRepository extends JpaRepository<KhachHang, Integer> {
    Optional<KhachHang> findFirstByOrderByIdDesc();

    Optional<KhachHang> findBySdt(String sdt);

    Optional<KhachHang> findByEmail(String email);
}
