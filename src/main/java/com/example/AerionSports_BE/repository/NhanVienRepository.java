package com.example.AerionSports_BE.repository;

import com.example.AerionSports_BE.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    // Tìm kiếm nhân viên theo tên (gần đúng)
    List<NhanVien> findByTenNvContaining(String tenNv);

    // Tìm kiếm theo email
    Optional<NhanVien> findByEmail(String email);

    // Kiểm tra sự tồn tại (dùng cho đăng ký mới)
    boolean existsBySdt(String sdt);
    boolean existsByEmail(String email);
    boolean existsBySdtOrEmail(String sdt, String email);

    // Kiểm tra sự tồn tại khi cập nhật (loại trừ chính ID đang chỉnh sửa)
    boolean existsBySdtAndIdNot(String sdt, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);

    // Lấy danh sách nhân viên mới nhất
    List<NhanVien> findAllByOrderByNgayTaoDesc();
}