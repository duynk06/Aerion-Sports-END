package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.VaiTro;
import com.example.AerionSports_BE.entity.TaiKhoan; // 🌟 THÊM IMPORT
import com.example.AerionSports_BE.repository.NhanVienRepository;
import com.example.AerionSports_BE.repository.VaiTroRepository;
import com.example.AerionSports_BE.repository.TaiKhoanRepository; // 🌟 THÊM IMPORT
import com.example.AerionSports_BE.service.NhanVienService;
import com.example.AerionSports_BE.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🌟 THÊM ĐỂ ĐẢM BẢO ATOMICITY

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NhanVienServiceImpl implements NhanVienService {
    private final NhanVienRepository nhanVienRepository;
    private final VaiTroRepository vaiTroRepository;
    private final EmailService emailService;
    private final TaiKhoanRepository taiKhoanRepository; //
    private final PasswordEncoder passwordEncoder;   // 🔧 THÊM DÒNG NÀY
// 🌟 TIÊM REPOSITORY TÀI KHOẢN

    @Override
    public List<NhanVien> findAll() {
        return nhanVienRepository.findAll();
    }

    @Override
    public NhanVien findById(Integer id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
    }

    @Override
    public void changeStatus(Integer id, Integer trangThai) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
        nv.setTrangThai(trangThai);
        nhanVienRepository.save(nv);
    }

    @Override
    @Transactional // 🌟 Đảm bảo nếu tạo tài khoản lỗi thì sẽ Rollback không tạo Nhân viên lỗi
    public NhanVien add(NhanVien nhanVien) {

        // --- LOGIC TỰ TĂNG MÃ NHÂN VIÊN TUẦN TỰ ---
        if (nhanVien.getMaNv() == null || nhanVien.getMaNv().trim().isEmpty()) {
            Optional<NhanVien> maxIdEmployee = nhanVienRepository.findAll()
                    .stream()
                    .max((nv1, nv2) -> nv1.getId().compareTo(nv2.getId()));

            int nextId = 1;
            if (maxIdEmployee.isPresent()) {
                nextId = maxIdEmployee.get().getId() + 1;
            }

            String maTuTang = String.format("NV%03d", nextId);
            nhanVien.setMaNv(maTuTang);
        }

        if (nhanVien.getSdt() != null && nhanVienRepository.existsBySdt(nhanVien.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (nhanVien.getEmail() != null && !nhanVien.getEmail().trim().isEmpty() && nhanVienRepository.existsByEmail(nhanVien.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Integer vaiTroId = nhanVien.getVaiTro().getId();
        VaiTro vaiTro = vaiTroRepository.findById(vaiTroId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));

        nhanVien.setVaiTro(vaiTro);
        nhanVien.setNgayTao(LocalDateTime.now());
        nhanVien.setNgaySua(LocalDateTime.now());

        // Thực hiện lưu thông tin nhân viên mới xuống Database SQL Server trước để lấy ID tự tăng
        NhanVien savedEmployee = nhanVienRepository.save(nhanVien);

        // ==========================================================================
        // ⚡ ĐÃ CẬP NHẬT: TỰ ĐỘNG ĐỒNG BỘ ĐỒNG THỜI TÀI KHOẢN HỆ THỐNG
        // ==========================================================================
        String matKhauTamThoi = UUID.randomUUID().toString().substring(0, 8); // Sinh 8 ký tự mật khẩu

        TaiKhoan tkMoi = new TaiKhoan();
        tkMoi.setTenDangNhap(savedEmployee.getEmail());
        tkMoi.setMatKhauHash(passwordEncoder.encode(matKhauTamThoi)); // 🔧 THÊM DÒNG NÀY — bắt buộc để tránh lỗi NOT NULL
// Lấy luôn Email làm tên đăng nhập hệ thống
        tkMoi.setLoaiTaiKhoan("NHAN_VIEN");
        tkMoi.setIdChuTaiKhoan(savedEmployee.getId()); // Gắn kết ID của NhanVien vừa sinh ra
        tkMoi.setNgayTao(LocalDateTime.now());
        tkMoi.setNgayCapNhat(LocalDateTime.now());
        tkMoi.setTrangThai(1);

        taiKhoanRepository.save(tkMoi); // Lưu trực tiếp bản ghi Authentication

        // 3. Kích hoạt luồng gửi mail ngầm trả tài khoản về hòm thư Gmail của nhân viên
        if (savedEmployee.getEmail() != null && !savedEmployee.getEmail().trim().isEmpty()) {
            emailService.sendAccountCreationEmail(
                    savedEmployee.getEmail(),
                    savedEmployee.getTenNv(),
                    matKhauTamThoi // Gửi mật khẩu dạng text thô qua email cho nhân viên đọc
            );
        }

        return savedEmployee;
    }

    @Override
    public NhanVien update(Integer id, NhanVien nhanVien) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        if (nhanVien.getSdt() != null && !nhanVien.getSdt().equals(nv.getSdt()) && nhanVienRepository.existsBySdt(nhanVien.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (nhanVien.getEmail() != null && !nhanVien.getEmail().trim().isEmpty() && !nhanVien.getEmail().equalsIgnoreCase(nv.getEmail()) && nhanVienRepository.existsByEmail(nhanVien.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        VaiTro vaiTro = vaiTroRepository.findById(nhanVien.getVaiTro().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));

        nv.setVaiTro(vaiTro);

        if (nhanVien.getMaNv() != null && !nhanVien.getMaNv().trim().isEmpty()) {
            nv.setMaNv(nhanVien.getMaNv());
        }

        nv.setTenNv(nhanVien.getTenNv());
        nv.setSdt(nhanVien.getSdt());
        nv.setEmail(nhanVien.getEmail());
        nv.setGioiTinh(nhanVien.getGioiTinh());
        nv.setAvatar(nhanVien.getAvatar());
        nv.setNgaySinh(nhanVien.getNgaySinh());
        nv.setDiaChi(nhanVien.getDiaChi());
        nv.setTinhThanh(nhanVien.getTinhThanh());
        nv.setQuanHuyen(nhanVien.getQuanHuyen());
        nv.setPhuongXa(nhanVien.getPhuongXa());
        nv.setDiaChiChiTiet(nhanVien.getDiaChiChiTiet());
        nv.setTrangThai(nhanVien.getTrangThai());
        nv.setNgaySua(LocalDateTime.now());

        return nhanVienRepository.save(nv);
    }

    @Override
    public void delete(Integer id) {
        NhanVien nv = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        nhanVienRepository.delete(nv);
    }

    @Override
    public List<NhanVien> search(String tenNv) {
        return nhanVienRepository.findByTenNvContaining(tenNv);
    }
}