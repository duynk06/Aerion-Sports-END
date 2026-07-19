package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.ChatLieuKhungVot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuKhungVotRepository  extends JpaRepository<ChatLieuKhungVot, Integer> {
    boolean existsByMaChatLieuKhungVot(String ma);

    @Query("SELECT c FROM ChatLieuKhungVot c WHERE " +
            "(:k IS NULL OR c.maChatLieuKhungVot LIKE %:k% OR c.tenChatLieuKhungVot LIKE %:k%) " +
            "AND (:t IS NULL OR c.trangThai = :t)")
    Page<ChatLieuKhungVot> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<ChatLieuKhungVot> findByTrangThai(Integer trangThai, Pageable pageable);
}
