package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    List<NhanVien> findByTenNvContaining(String tenNv);

    boolean existsBySdt(String sdt);

    boolean existsByEmail(String email);

    boolean existsBySdtOrEmail(String sdt, String email);

    boolean existsBySdtAndIdNot(String sdt, Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<NhanVien> findByEmail(String email);
}