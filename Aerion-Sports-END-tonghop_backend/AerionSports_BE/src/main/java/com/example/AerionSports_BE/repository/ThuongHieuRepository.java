package com.example.AerionSports_BE.repository;


import com.example.AerionSports_BE.entity.ThuongHieu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    boolean existsByMaThuongHieu(String maThuongHieu);

    @Query("SELECT t FROM ThuongHieu t WHERE " +
            "(:k IS NULL OR t.maThuongHieu LIKE %:k% OR t.tenThuongHieu LIKE %:k%) " +
            "AND (:t IS NULL OR t.trangThai = :t)")
    Page<ThuongHieu> search(@Param("k") String keyword, @Param("t") Integer trangThai, Pageable p);

    Page<ThuongHieu> findByTrangThai(Integer trangThai, Pageable pageable);
}
