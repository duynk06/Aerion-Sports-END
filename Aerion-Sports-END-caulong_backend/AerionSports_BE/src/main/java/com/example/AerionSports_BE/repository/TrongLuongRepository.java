package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.TrongLuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrongLuongRepository extends JpaRepository<TrongLuong, Integer> {
    boolean existsByMaTrongLuong(String ma);

    @Query("SELECT t FROM TrongLuong t WHERE " +
            "(:k IS NULL OR t.maTrongLuong LIKE %:k% OR t.tenTrongLuong LIKE %:k%) " +
            "AND (:t IS NULL OR t.trangThai = :t)")
    Page<TrongLuong> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<TrongLuong> findByTrangThai(Integer trangThai, Pageable pageable);
}
