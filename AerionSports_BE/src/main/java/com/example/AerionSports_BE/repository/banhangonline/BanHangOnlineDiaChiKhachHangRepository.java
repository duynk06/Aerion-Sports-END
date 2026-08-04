package com.example.AerionSports_BE.repository.banhangonline;

import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BanHangOnlineDiaChiKhachHangRepository extends JpaRepository<DiaChiKhachHang, Integer> {

    List<DiaChiKhachHang> findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(Integer idKhachHang);

    @Modifying
    @Query("UPDATE DiaChiKhachHang d SET d.macDinh = false WHERE d.khachHang.id = :idKhachHang")
    void boMacDinhTheoKhachHang(@Param("idKhachHang") Integer idKhachHang);
}
