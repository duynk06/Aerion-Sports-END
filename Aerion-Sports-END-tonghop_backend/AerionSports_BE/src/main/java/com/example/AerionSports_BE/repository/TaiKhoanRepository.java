    package com.example.AerionSports_BE.repository;

    import com.example.AerionSports_BE.entity.TaiKhoan;
    import org.springframework.data.jpa.repository.JpaRepository;

    import java.util.Optional;

    public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Integer > {
        Optional<TaiKhoan> findByTenDangNhapAndTrangThai(String tenDangNhap, Integer trangThai);
    }
