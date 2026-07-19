package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.LichSuGiaoCa;
import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.repository.LichSuGiaoCaRepository;
import com.example.AerionSports_BE.repository.NhanVienRepository;
import com.example.AerionSports_BE.repository.LichLamViecRepository;
import com.example.AerionSports_BE.service.LichSuGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class LichSuGiaoCaServiceImpl implements LichSuGiaoCaService {

    @Autowired
    private LichSuGiaoCaRepository giaoCaRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private LichLamViecRepository lichLamViecRepository;

    @Override
    @Transactional
    public LichSuGiaoCa moCa(Integer idNhanVien, BigDecimal tienBanDau) {
        giaoCaRepository.findActiveCaByNhanVien(idNhanVien).ifPresent(ca -> {
            throw new RuntimeException("Bạn đang có một ca làm việc chưa chốt! Không thể mở ca mới.");
        });

        LocalDate homNay = LocalDate.now();
        long coLichHienTai = lichLamViecRepository.countLichCheckMoCa(idNhanVien, homNay);

        if (coLichHienTai == 0) {
            throw new RuntimeException("Hôm nay bạn không có lịch làm việc được xếp trên hệ thống! Không thể mở ca.");
        }

        String lastMaCa = giaoCaRepository.findLastMaCa();
        String nextMaCa = "CA01";
        if (lastMaCa != null && lastMaCa.startsWith("CA")) {
            try {
                int nextNum = Integer.parseInt(lastMaCa.replace("CA", "")) + 1;
                nextMaCa = String.format("CA%02d", nextNum);
            } catch (Exception e) {
                nextMaCa = "CA01";
            }
        }

        NhanVien nv = nhanVienRepository.findById(idNhanVien)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));

        LichSuGiaoCa caMoi = new LichSuGiaoCa();
        caMoi.setNhanVienCaTruoc(nv);
        caMoi.setMaCa(nextMaCa);
        caMoi.setThoiGianVao(LocalDateTime.now());
        caMoi.setTienBanDau(tienBanDau);
        caMoi.setTienMatDoanhThu(BigDecimal.ZERO);
        caMoi.setTienChuyenKhoan(BigDecimal.ZERO);
        caMoi.setTrangThai(0); // 0: Đang mở ca hoạt động

        return giaoCaRepository.save(caMoi);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> layThongTinCaHienTai(Integer idNhanVien) {
        LichSuGiaoCa caHienTai = giaoCaRepository.findActiveCaByNhanVien(idNhanVien)
                .orElseThrow(() -> new RuntimeException("Bạn chưa mở ca làm việc!"));

        NhanVien nv = nhanVienRepository.findById(idNhanVien)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu nhân viên hiện tại!"));

        BigDecimal tienMatDoanhThu = giaoCaRepository.tinhTienMatDoanhThu(idNhanVien, caHienTai.getThoiGianVao());
        BigDecimal tienChuyenKhoan = giaoCaRepository.tinhTienChuyenKhoanDoanhThu(idNhanVien, caHienTai.getThoiGianVao());

        Map<String, Object> response = new HashMap<>();
        response.put("id", caHienTai.getId());
        response.put("maCa", caHienTai.getMaCa());
        response.put("tenNhanVien", nv.getTenNv());
        response.put("thoiGianVao", caHienTai.getThoiGianVao().toString());
        response.put("tienBanDau", caHienTai.getTienBanDau());
        response.put("tienMatDoanhThu", tienMatDoanhThu);
        response.put("tienChuyenKhoan", tienChuyenKhoan);

        return response;
    }

    @Override
    @Transactional
    public LichSuGiaoCa ketThucCa(Integer idNhanVien, Integer idNhanVienCaSau, BigDecimal tienMatDoanhThuMoi, BigDecimal tienChuyenKhoanMoi, String ghiChu) {
        LichSuGiaoCa caHienTai = giaoCaRepository.findActiveCaByNhanVien(idNhanVien)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc đang hoạt động để đóng két!"));

        NhanVien nvCaSau = nhanVienRepository.findById(idNhanVienCaSau)
                .orElseThrow(() -> new RuntimeException("Vui lòng chỉ định chính xác nhân viên nhận ca sau!"));

        BigDecimal tongTienMatBanGiao = caHienTai.getTienBanDau().add(tienMatDoanhThuMoi);

        caHienTai.setThoiGianRa(LocalDateTime.now());
        caHienTai.setTienMatDoanhThu(tienMatDoanhThuMoi);
        caHienTai.setTienChuyenKhoan(tienChuyenKhoanMoi);
        caHienTai.setTienMatThucTe(tongTienMatBanGiao);
        caHienTai.setTienMatChenhLech(BigDecimal.ZERO);
        caHienTai.setNhanVienCaSau(nvCaSau);
        caHienTai.setGhiChu(ghiChu);
        caHienTai.setTrangThai(1); // 🌟 Đã chốt ca thành công

        giaoCaRepository.save(caHienTai);
        return caHienTai;
    }
}