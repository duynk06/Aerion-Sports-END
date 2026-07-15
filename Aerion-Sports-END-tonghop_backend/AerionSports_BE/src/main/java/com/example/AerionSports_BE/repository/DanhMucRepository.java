package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.DanhMuc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DanhMucRepository extends JpaRepository<DanhMuc, Integer> {
    boolean existsByMaDanhMuc(String ma);

    @Query("SELECT d FROM DanhMuc d WHERE " +
            "(:k IS NULL OR d.maDanhMuc LIKE %:k% OR d.tenDanhMuc LIKE %:k%) " +
            "AND (:t IS NULL OR d.trangThai = :t)")
    Page<DanhMuc> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<DanhMuc> findByTrangThai(Integer trangThai, Pageable pageable);
}
