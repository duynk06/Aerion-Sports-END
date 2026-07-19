package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.DoCung;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DoCungRepository extends JpaRepository<DoCung, Integer> {
    boolean existsByMaDoCung(String ma);

    @Query("SELECT d FROM DoCung d WHERE " +
            "(:k IS NULL OR d.maDoCung LIKE %:k% OR d.tenDoCung LIKE %:k%) " +
            "AND (:t IS NULL OR d.trangThai = :t)")
    Page<DoCung> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);
}
