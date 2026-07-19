package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.XuatXu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface XuatXuRepository extends JpaRepository<XuatXu, Integer> {
    boolean existsByMaXuatXu(String ma);

    @Query("SELECT x FROM XuatXu x WHERE " +
            "(:k IS NULL OR x.maXuatXu LIKE %:k% OR x.tenXuatXu LIKE %:k%) " +
            "AND (:t IS NULL OR x.trangThai = :t)")
    Page<XuatXu> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<XuatXu> findByTrangThai(Integer trangThai, Pageable pageable);
}
