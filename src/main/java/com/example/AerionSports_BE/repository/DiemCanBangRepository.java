package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.DiemCanBang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiemCanBangRepository extends JpaRepository<DiemCanBang, Integer> {
    boolean existsByMaDiemCanBang(String ma);

    @Query("SELECT d FROM DiemCanBang d WHERE " +
            "(:k IS NULL OR d.maDiemCanBang LIKE %:k% OR d.tenDiemCanBang LIKE %:k%) " +
            "AND (:t IS NULL OR d.trangThai = :t)")
    Page<DiemCanBang> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<DiemCanBang> findByTrangThai(Integer trangThai, Pageable pageable);
}
