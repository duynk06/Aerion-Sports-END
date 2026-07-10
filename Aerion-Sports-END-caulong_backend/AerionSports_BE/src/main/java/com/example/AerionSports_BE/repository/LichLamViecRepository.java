package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, Integer> {

    @Query("SELECT l.id AS id, l.ngayLamViec AS ngayLamViec, l.trangThai AS trangThai, " +
            "c AS caLamViec, nv.maNv AS maNhanVien, nv.tenNv AS tenNhanVien " +
            "FROM LichLamViec l " +
            "JOIN CaLamViec c ON l.caLamViec.id = c.id " +
            "JOIN NhanVien nv ON l.idNhanVien = nv.id " +
            "WHERE l.ngayLamViec BETWEEN :tuNgay AND :denNgay")
    List<Map<String, Object>> findLichTrongKhoangNgay(@Param("tuNgay") LocalDate tuNgay, @Param("denNgay") LocalDate denNgay);

    @Query("SELECT COUNT(l) FROM LichLamViec l WHERE l.idNhanVien = :idNhanVien AND l.ngayLamViec = :ngayHienTai")
    long countLichCheckMoCa(@Param("idNhanVien") Integer idNhanVien, @Param("ngayHienTai") LocalDate ngayHienTai);
    boolean existsByIdNhanVienAndCaLamViecIdAndNgayLamViec(Integer idNhanVien, Integer idCaLamViec, LocalDate ngayLamViec);
    List<LichLamViec> findByNgayLamViecBetweenAndTrangThai(LocalDate tuNgay, LocalDate denNgay, Integer trangThai);
}