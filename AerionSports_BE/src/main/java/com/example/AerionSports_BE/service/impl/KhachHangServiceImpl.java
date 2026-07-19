package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.response.KhachHangResponse;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import com.example.AerionSports_BE.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {
    private final KhachHangRepository khachHangRepository;

    // Giới hạn độ dài các cột liên quan tới SĐT theo đúng schema DB (VARCHAR(15))
    private static final int MAX_SDT_LENGTH = 15;

    @Override
    public List<KhachHang> getAll() {
        return khachHangRepository.findAllByOrderByNgayTaoDesc();
    }

    @Override
    public KhachHang getById(Integer id) {
        return khachHangRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng có id = " + id));
    }

    @Override
    @Transactional
    public KhachHang add(KhachHang khachHang) {

        if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
            Optional<KhachHang> lastCustomer = khachHangRepository.findFirstByOrderByIdDesc();

            int nextId = 1;
            if (lastCustomer.isPresent()) {
                nextId = lastCustomer.get().getId() + 1;
            }

            String maTuTang = String.format("KH%03d", nextId);
            khachHang.setMaKhachHang(maTuTang);
        }

        if (khachHangRepository.existsByMaKhachHang(khachHang.getMaKhachHang())) {
            throw new RuntimeException("Mã khách hàng đã tồn tại");
        }

        if (khachHang.getSdt() != null
                && khachHangRepository.existsBySdt(khachHang.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (khachHang.getEmail() != null
                && !khachHang.getEmail().trim().isEmpty()
                && khachHangRepository.existsByEmail(khachHang.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 🌟 THÊM: Kiểm tra độ dài SĐT trong sổ địa chỉ trước khi lưu, tránh vỡ UNIQUE/VARCHAR
        // constraint và văng lỗi SQL thô ra người dùng (bảng dia_chi_khach_hang cột sdt VARCHAR(15))
        validateDoDaiSdtDiaChi(khachHang.getAddresses());

        khachHang.setNgayTao(LocalDateTime.now());
        khachHang.setNgayCapNhat(LocalDateTime.now());

        if (khachHang.getAddresses() != null) {
            for (DiaChiKhachHang addr : khachHang.getAddresses()) {
                addr.setKhachHang(khachHang);
                addr.setNgayTao(LocalDateTime.now());
                addr.setNgayCapNhat(LocalDateTime.now());
            }
        }

        try {
            return khachHangRepository.save(khachHang);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Dữ liệu nhập vào không hợp lệ (có trường vượt quá độ dài cho phép). Vui lòng kiểm tra lại số điện thoại và các trường thông tin!");
        }
    }

    @Override
    @Transactional
    public KhachHang update(Integer id, KhachHang khachHang) {

        KhachHang oldKhachHang = getById(id);

        if (khachHang.getSdt() != null
                && !khachHang.getSdt().equals(oldKhachHang.getSdt())
                && khachHangRepository.existsBySdt(khachHang.getSdt())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        if (khachHang.getEmail() != null
                && !khachHang.getEmail().trim().isEmpty()
                && !khachHang.getEmail().equalsIgnoreCase(oldKhachHang.getEmail())
                && khachHangRepository.existsByEmail(khachHang.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        // 🌟 THÊM: kiểm tra độ dài SĐT trong sổ địa chỉ mới trước khi ghi đè
        validateDoDaiSdtDiaChi(khachHang.getAddresses());

        if (khachHang.getMaKhachHang() != null &&
                !khachHang.getMaKhachHang().trim().isEmpty()) {
            oldKhachHang.setMaKhachHang(khachHang.getMaKhachHang());
        }

        if (khachHang.getHoTen() != null) {
            oldKhachHang.setHoTen(khachHang.getHoTen());
        }

        if (khachHang.getNgaySinh() != null) {
            oldKhachHang.setNgaySinh(khachHang.getNgaySinh());
        }

        if (khachHang.getGioiTinh() != null) {
            oldKhachHang.setGioiTinh(khachHang.getGioiTinh());
        }

        if (khachHang.getSdt() != null) {
            oldKhachHang.setSdt(khachHang.getSdt());
        }

        if (khachHang.getEmail() != null) {
            oldKhachHang.setEmail(khachHang.getEmail());
        }

        if (khachHang.getAvatar() != null) {
            oldKhachHang.setAvatar(khachHang.getAvatar());
        }

        if (khachHang.getTrangThai() != null) {
            oldKhachHang.setTrangThai(khachHang.getTrangThai());
        }

        oldKhachHang.getAddresses().clear();

        if (khachHang.getAddresses() != null) {
            for (DiaChiKhachHang addr : khachHang.getAddresses()) {
                addr.setKhachHang(oldKhachHang);

                if (addr.getId() == null) {
                    addr.setNgayTao(LocalDateTime.now());
                }

                addr.setNgayCapNhat(LocalDateTime.now());
                oldKhachHang.getAddresses().add(addr);
            }
        }

        oldKhachHang.setNgayCapNhat(LocalDateTime.now());

        try {
            return khachHangRepository.save(oldKhachHang);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Dữ liệu nhập vào không hợp lệ (có trường vượt quá độ dài cho phép). Vui lòng kiểm tra lại số điện thoại và các trường thông tin!");
        }
    }

    @Override
    public void delete(Integer id) {
        KhachHang khachHang = getById(id);
        khachHangRepository.delete(khachHang);
    }

    @Override
    public List<KhachHangResponse> getAllSummary() {
        List<Object[]> rawData = khachHangRepository.findAllKhachHangWithOrderSummary();
        List<KhachHangResponse> resultList = new ArrayList<>();

        for (Object[] row : rawData) {
            KhachHangResponse dto = new KhachHangResponse();
            dto.setId((Integer) row[0]);
            dto.setMaKhachHang((String) row[1]);
            dto.setHoTen((String) row[2]);
            dto.setEmail((String) row[3]);
            dto.setSdt((String) row[4]);

            if (row[5] != null) {
                dto.setNgaySinh(((java.sql.Date) row[5]).toLocalDate());
            }

            dto.setTongSoDonHang(row[6] != null ? ((Number) row[6]).longValue() : 0L);

            if (row[7] != null) {
                dto.setDonHangGanNhat(((Timestamp) row[7]).toLocalDateTime());
            }

            resultList.add(dto);
        }
        return resultList;
    }

    // 🌟 THÊM: hàm dùng chung để validate độ dài SĐT trong sổ địa chỉ trước khi ghi DB
    private void validateDoDaiSdtDiaChi(List<DiaChiKhachHang> addresses) {
        if (addresses == null) return;
        for (DiaChiKhachHang addr : addresses) {
            if (addr.getSdt() != null && addr.getSdt().length() > MAX_SDT_LENGTH) {
                throw new RuntimeException("Số điện thoại người nhận \"" + addr.getSdt()
                        + "\" vượt quá độ dài cho phép (tối đa " + MAX_SDT_LENGTH + " ký tự)!");
            }
        }
    }
}