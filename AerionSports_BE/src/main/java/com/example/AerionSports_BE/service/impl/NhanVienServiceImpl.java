package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.entity.VaiTro;
import com.example.AerionSports_BE.repository.NhanVienRepository;
import com.example.AerionSports_BE.repository.TaiKhoanRepository;
import com.example.AerionSports_BE.repository.VaiTroRepository;
import com.example.AerionSports_BE.service.EmailService;
import com.example.AerionSports_BE.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<NhanVien> findAll() {
        return nhanVienRepository.findAllByOrderByNgayTaoDesc();
    }

    @Override
    public NhanVien findById(Integer id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + id));
    }

    @Override
    public void changeStatus(Integer id, Integer trangThai) {
        NhanVien nv = findById(id);
        nv.setTrangThai(trangThai);
        nhanVienRepository.save(nv);
    }

    @Override
    @Transactional
    public NhanVien add(NhanVien nhanVien) {
        // --- LOGIC TỰ TĂNG MÃ NHÂN VIÊN ---
        if (nhanVien.getMaNv() == null || nhanVien.getMaNv().trim().isEmpty()) {
            Optional<NhanVien> maxIdEmployee = nhanVienRepository.findAll().stream()
                    .max((nv1, nv2) -> nv1.getId().compareTo(nv2.getId()));
            int nextId = maxIdEmployee.map(nv -> nv.getId() + 1).orElse(1);
            nhanVien.setMaNv(String.format("NV%03d", nextId));
        }

        // Kiểm tra tồn tại
        if (nhanVien.getSdt() != null && nhanVienRepository.existsBySdt(nhanVien.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (nhanVien.getEmail() != null && !nhanVien.getEmail().trim().isEmpty() && nhanVienRepository.existsByEmail(nhanVien.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (nhanVien.getEmail() != null && taiKhoanRepository.existsByTenDangNhap(nhanVien.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng cho một tài khoản khác!");
        }

        VaiTro vaiTro = vaiTroRepository.findById(nhanVien.getVaiTro().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));

        nhanVien.setVaiTro(vaiTro);
        nhanVien.setNgayTao(LocalDateTime.now());
        nhanVien.setNgaySua(LocalDateTime.now());

        NhanVien savedEmployee = nhanVienRepository.save(nhanVien);

        // --- ĐỒNG BỘ TÀI KHOẢN ---
        String matKhauTamThoi = UUID.randomUUID().toString().substring(0, 8);

        // 🆕 loai_tai_khoan gán theo đúng vai trò được chọn khi tạo nhân viên
        String loaiTaiKhoan = "QL".equalsIgnoreCase(vaiTro.getMaVaiTro()) ? "QUẢN LÝ" : "NHÂN VIÊN";

        TaiKhoan tkMoi = new TaiKhoan();
        tkMoi.setTenDangNhap(savedEmployee.getEmail());
        tkMoi.setMatKhauHash(passwordEncoder.encode(matKhauTamThoi));
        tkMoi.setLoaiTaiKhoan(loaiTaiKhoan);
        tkMoi.setIdChuTaiKhoan(savedEmployee.getId());
        tkMoi.setNgayTao(LocalDateTime.now());
        tkMoi.setNgayCapNhat(LocalDateTime.now());
        tkMoi.setTrangThai(1);

        try {
            taiKhoanRepository.save(tkMoi);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Lỗi hệ thống: Không thể tạo tài khoản đăng nhập!");
        }

        // Gửi mail
        if (savedEmployee.getEmail() != null && !savedEmployee.getEmail().trim().isEmpty()) {
            emailService.sendAccountCreationEmail(savedEmployee.getEmail(), savedEmployee.getTenNv(), matKhauTamThoi);
        }

        return savedEmployee;
    }

    @Override
    @Transactional
    public NhanVien update(Integer id, NhanVien nhanVien) {
        NhanVien nv = findById(id);

        if (nhanVien.getSdt() != null && !nhanVien.getSdt().equals(nv.getSdt()) && nhanVienRepository.existsBySdt(nhanVien.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }
        if (nhanVien.getEmail() != null && !nhanVien.getEmail().equalsIgnoreCase(nv.getEmail()) && nhanVienRepository.existsByEmail(nhanVien.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (nhanVien.getEmail() != null && !nhanVien.getEmail().equalsIgnoreCase(nv.getEmail()) && taiKhoanRepository.existsByTenDangNhap(nhanVien.getEmail())) {
            throw new RuntimeException("Email này đã được sử dụng cho một tài khoản khác!");
        }

        VaiTro vaiTroMoi = vaiTroRepository.findById(nhanVien.getVaiTro().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vai trò"));

        // 🆕 Lưu lại vai trò cũ để biết có thật sự đổi vai trò hay không
        Integer idVaiTroCu = nv.getVaiTro() != null ? nv.getVaiTro().getId() : null;
        boolean doiVaiTro = idVaiTroCu == null || !idVaiTroCu.equals(vaiTroMoi.getId());

        nv.setVaiTro(vaiTroMoi);
        nv.setMaNv(nhanVien.getMaNv());
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

        NhanVien updated = nhanVienRepository.save(nv);

        // 🆕 Đồng bộ loai_tai_khoan nếu vai trò vừa bị đổi
        if (doiVaiTro) {
            List<TaiKhoan> taiKhoans = taiKhoanRepository.findByIdChuTaiKhoanAndLoaiTaiKhoanIn(
                    updated.getId(), List.of("QUẢN LÝ", "NHÂN VIÊN"));

            if (!taiKhoans.isEmpty()) {
                String loaiTaiKhoanMoi = "QL".equalsIgnoreCase(vaiTroMoi.getMaVaiTro()) ? "QUẢN LÝ" : "NHÂN VIÊN";
                TaiKhoan tk = taiKhoans.get(0);
                tk.setLoaiTaiKhoan(loaiTaiKhoanMoi);
                tk.setNgayCapNhat(LocalDateTime.now());
                taiKhoanRepository.save(tk);
            }
        }

        return updated;
    }

    @Override
    public void delete(Integer id) {
        NhanVien nv = findById(id);
        nhanVienRepository.delete(nv);
    }

    @Override
    public List<NhanVien> search(String tenNv) {
        return nhanVienRepository.findByTenNvContaining(tenNv);
    }
}