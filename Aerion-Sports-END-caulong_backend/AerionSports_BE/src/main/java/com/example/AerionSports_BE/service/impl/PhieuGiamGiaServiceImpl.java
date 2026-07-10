package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.VoucherRequestDTO;
import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.PhieuGiamGiaKhachHang;
import com.example.AerionSports_BE.repository.PhieuGiamGiaRepository;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import com.example.AerionSports_BE.repository.PhieuGiamGiaKhachHangRepository; // Import Repo bảng trung gian của bạn
import com.example.AerionSports_BE.service.PhieuGiamGiaService;
import com.example.AerionSports_BE.service.EmailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    private final PhieuGiamGiaRepository repository;
    private final KhachHangRepository khachHangRepository;
    private final PhieuGiamGiaKhachHangRepository pggKhachHangRepository; // Repo bảng trung gian
    private final EmailService emailService;

    // Constructor Injection nạp đầy đủ cả 4 thành phần phụ thuộc
    public PhieuGiamGiaServiceImpl(PhieuGiamGiaRepository repository,
                                   KhachHangRepository khachHangRepository,
                                   PhieuGiamGiaKhachHangRepository pggKhachHangRepository,
                                   EmailService emailService) {
        this.repository = repository;
        this.khachHangRepository = khachHangRepository;
        this.pggKhachHangRepository = pggKhachHangRepository;
        this.emailService = emailService;
    }

    @Override
    public List<PhieuGiamGia> getAll() {
        return repository.findAll();
    }

    @Override
    public PhieuGiamGia getById(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá"));
    }

    @Override
    @Transactional // Đảm bảo nếu lưu lỗi hoặc gửi mail lỗi (nếu cần) có thể Rollback dữ liệu an toàn
    public PhieuGiamGia add(VoucherRequestDTO dto) {

        if (repository.existsByMaPhieuGiamGia(dto.getMaPhieuGiamGia())) {
            throw new RuntimeException("Mã phiếu giảm giá đã tồn tại");
        }

        // 1. Map dữ liệu từ DTO sang đối tượng PhieuGiamGia Entity để lưu bảng gốc
        PhieuGiamGia pgg = new PhieuGiamGia();
        pgg.setMaPhieuGiamGia(dto.getMaPhieuGiamGia());
        pgg.setTenPhieuGiamGia(dto.getTenPhieuGiamGia());
        pgg.setLoaiPhieuGiamGia(dto.getLoaiPhieuGiamGia());
        pgg.setGiaTriGiam(dto.getGiaTriGiam());
        pgg.setGiaTriDonToiThieu(dto.getGiaTriDonToiThieu());
        pgg.setGiaTriGiamToiDa(dto.getGiaTriGiamToiDa());
        pgg.setSoLuong(dto.getSoLuong());
        pgg.setNgayBatDau(dto.getNgayBatDau());
        pgg.setNgayKetThuc(dto.getNgayKetThuc());
        pgg.setMoTa(dto.getMoTa());
        pgg.setTrangThai(dto.getTrangThai());
        pgg.setNgayTao(LocalDateTime.now());
        pgg.setNgayCapNhat(LocalDateTime.now());

        PhieuGiamGia savedPgg = repository.save(pgg);

        // 2. Kiểm tra nếu đối tượng áp dụng là PERSONAL và có truyền mảng ID khách hàng từ Vue lên
        if ("PERSONAL".equals(dto.getDoiTuongApDung()) && dto.getKhachHangIds() != null && !dto.getKhachHangIds().isEmpty()) {

            // Tìm toàn bộ thực thể Khách hàng dựa vào danh sách ID
            List<KhachHang> listKhach = khachHangRepository.findAllById(dto.getKhachHangIds());

            // Định dạng chuỗi hiển thị số tiền/phần trăm giảm trong Email (Ví dụ: 15% hoặc 30.000 VNĐ)
            String hienThiGiaTriGiam = "Sale %".equals(savedPgg.getLoaiPhieuGiamGia())
                    ? savedPgg.getGiaTriGiam() + "%"
                    : String.format("%,.0f VNĐ", savedPgg.getGiaTriGiam());

            for (KhachHang kh : listKhach) {
                // Luồng A: Lưu dữ liệu vào bảng liên kết trung gian phieu_giam_gia_khach_hang
                PhieuGiamGiaKhachHang pggKhachHang = new PhieuGiamGiaKhachHang();
                pggKhachHang.setPhieuGiamGia(savedPgg);
                pggKhachHang.setKhachHang(kh);
                pggKhachHang.setDaSuDung(false); // Mặc định chưa sử dụng
                pggKhachHang.setNgayNhan(LocalDateTime.now());
                pggKhachHang.setTrangThai(1);

                pggKhachHangRepository.save(pggKhachHang);

                // Luồng B: Bắn Mail thông báo trực tiếp sang hòm thư của khách hàng
                if (kh.getEmail() != null && !kh.getEmail().trim().isEmpty()) {
                    try {
                        emailService.sendVoucherEmail(
                                kh.getEmail(),
                                kh.getHoTen(),
                                savedPgg.getMaPhieuGiamGia(),
                                hienThiGiaTriGiam,
                                savedPgg.getNgayKetThuc().toString()
                        );
                    } catch (Exception e) {
                        System.err.println("Lỗi gửi mail cho khách hàng " + kh.getEmail() + ": " + e.getMessage());
                    }
                }
            }
        }

        return savedPgg;
    }

    @Override
    public PhieuGiamGia update(Integer id, PhieuGiamGia pgg) {
        PhieuGiamGia old = getById(id);

        old.setTenPhieuGiamGia(pgg.getTenPhieuGiamGia());
        old.setLoaiPhieuGiamGia(pgg.getLoaiPhieuGiamGia());
        old.setGiaTriGiam(pgg.getGiaTriGiam());
        old.setGiaTriDonToiThieu(pgg.getGiaTriDonToiThieu());
        old.setGiaTriGiamToiDa(pgg.getGiaTriGiamToiDa());
        old.setSoLuong(pgg.getSoLuong());
        old.setNgayBatDau(pgg.getNgayBatDau());
        old.setNgayKetThuc(pgg.getNgayKetThuc());
        old.setTrangThai(pgg.getTrangThai());
        old.setMoTa(pgg.getMoTa());
        old.setNgayCapNhat(LocalDateTime.now());

        return repository.save(old);
    }

    @Override
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}