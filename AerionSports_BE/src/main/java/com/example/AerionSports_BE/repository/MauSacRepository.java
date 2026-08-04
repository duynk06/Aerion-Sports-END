package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.MauSac;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MauSacRepository extends JpaRepository<MauSac, Integer> {
    boolean existsByMaMauSac(String ma);

    @Query("SELECT m FROM MauSac m WHERE " +
            "(:k IS NULL OR m.maMauSac LIKE %:k% OR m.tenMauSac LIKE %:k%) " +
            "AND (:t IS NULL OR m.trangThai = :t)")
    Page<MauSac> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<MauSac> findByTrangThai(Integer trangThai, Pageable pageable);
}
