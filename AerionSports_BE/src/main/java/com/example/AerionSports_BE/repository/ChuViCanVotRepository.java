package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.ChuViCanVot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChuViCanVotRepository extends JpaRepository<ChuViCanVot, Integer> {
    List<ChuViCanVot> findByTrangThai(Integer trangThai);

    boolean existsByMaChuViCanVot(String maChuViCanVot);

    boolean existsByMaChuViCanVotAndIdNot(String maChuViCanVot, Integer id);

    @Query("SELECT c FROM ChuViCanVot c WHERE " +
            "(:k IS NULL OR c.maChuViCanVot LIKE %:k% OR c.tenChuViCanVot LIKE %:k%) " +
            "AND (:tt IS NULL OR c.trangThai = :tt)")
    Page<ChuViCanVot> search(@Param("k") String keyword, @Param("tt") Integer trangThai, Pageable p);
}
