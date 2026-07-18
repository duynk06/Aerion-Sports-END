package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.ChatLieuThanVot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuThanVotRepository extends JpaRepository<ChatLieuThanVot, Integer> {
    boolean existsByMaChatLieuThanVot(String ma);

    @Query("SELECT c FROM ChatLieuThanVot c WHERE " +
            "(:k IS NULL OR c.maChatLieuThanVot LIKE %:k% OR c.tenChatLieuThanVot LIKE %:k%) " +
            "AND (:t IS NULL OR c.trangThai = :t)")
    Page<ChatLieuThanVot> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<ChatLieuThanVot> findByTrangThai(Integer trangThai, Pageable pageable);
}
