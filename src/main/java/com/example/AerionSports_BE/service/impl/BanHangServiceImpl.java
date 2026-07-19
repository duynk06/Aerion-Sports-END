package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.SanPhamPosDTO;
import com.example.AerionSports_BE.dto.request.DiaChiGiaoHangThuCongRequest;
import com.example.AerionSports_BE.dto.request.DiaChiRequest;
import com.example.AerionSports_BE.dto.request.ThanhToanRequest;
import com.example.AerionSports_BE.dto.request.ThemSanPhamRequest;
import com.example.AerionSports_BE.dto.response.*;
import com.example.AerionSports_BE.entity.*;
import com.example.AerionSports_BE.repository.*;
import com.example.AerionSports_BE.service.BanHangService;
import com.example.AerionSports_BE.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BanHangServiceImpl implements BanHangService {

    private final HoaDonRepository hoaDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final ThanhToanRepository thanhToanRepository;
    private final PhuongThucThanhToanRepository phuongThucThanhToanRepository;
    private final LichSuThanhToanRepository lichSuThanhToanRepository;
    private final DiaChiKhachHangRepository diaChiKhachHangRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LichSuHoaDonRepository lichSuHoaDonRepository;
    private final EmailService emailService;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;

    @org.springframework.beans.factory.annotation.Value("${app.base-url}")
    private String baseUrl;
    @Override
    public BanHangResponse taoHoaDonCho(String username) {
        long soHoaDonCho = hoaDonRepository.countByTrangThai(0);
        if (soHoaDonCho >= 5) {
            throw new RuntimeException("Đã đạt tối đa 5 hóa đơn chờ!");
        }

        HoaDon hoaDon = new HoaDon();
        String maHoaDon = "HD" + System.currentTimeMillis();
        hoaDon.setMaHoaDon(maHoaDon);
        hoaDon.setLoaiHoaDon(0);
        hoaDon.setTongTienHang(BigDecimal.ZERO);
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTienVanChuyen(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(BigDecimal.ZERO);
        hoaDon.setTrangThai(0);

        // Gán khách vãng lai
        KhachHang khachVangLai = khachHangRepository.findById(999).orElse(null);
        hoaDon.setKhachHang(khachVangLai);

        // ✅ Tìm nhân viên theo email (username trong JWT)
        NhanVien nhanVien = null;
        if (username != null) {
            nhanVien = nhanVienRepository.findByEmail(username).orElse(null);
        }
        // Fallback về NV id=1 nếu không tìm được
        if (nhanVien == null) {
            nhanVien = nhanVienRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        }
        hoaDon.setNhanVien(nhanVien);

        HoaDon saved = hoaDonRepository.save(hoaDon);

        // Lưu lịch sử
        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(saved);
        lichSu.setNhanVien(nhanVien);
        lichSu.setTrangThaiCu(0);
        lichSu.setTrangThaiMoi(0);
        lichSu.setHanhDong("Tạo hóa đơn chờ");
        lichSu.setGhiChu("Tạo bởi: " + nhanVien.getTenNv());
        lichSu.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSu);

        return new BanHangResponse(saved);
    }

    @Override
    @Transactional
    public BanHangResponse capNhatKhachHangVaoHoaDon(Integer idHoaDon, Integer idKhachHang) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // SỬA LẠI ĐOẠN NÀY: Nếu là null hoặc 999 thì đều gán về khách vãng lai
        if (idKhachHang == null || idKhachHang == 999) {
            KhachHang khachVangLai = khachHangRepository.findById(999)
                    .orElseThrow(() -> new RuntimeException("Chưa tạo dữ liệu Khách hàng vãng lai (ID: 999) trong CSDL"));
            hoaDon.setKhachHang(khachVangLai);
        } else {
            KhachHang khachHang = khachHangRepository.findById(idKhachHang)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
            hoaDon.setKhachHang(khachHang);
        }

        HoaDon hoaDonSaved = hoaDonRepository.save(hoaDon);
        return new BanHangResponse(hoaDonSaved);
    }

    // Đừng quên inject thêm SanPhamChiTietRepository và ChiTietHoaDonRepository vào Service nhé
    @Override
    @Transactional
    public BanHangResponse themSanPhamVaoHoaDon(ThemSanPhamRequest request) {

        HoaDon hoaDon = hoaDonRepository.findByIdWithChiTiet(request.getIdHoaDon());
        if (hoaDon == null) throw new RuntimeException("Không tìm thấy hóa đơn!");

        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(request.getIdSanPhamChiTiet())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));

        // Kiểm tra tồn kho
        if (chiTietSanPham.getSoLuong() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng!");
        }

        BigDecimal giaMoi = tinhGiaSauGiam(chiTietSanPham); // thay cho chiTietSanPham.getGiaBan() // Giá hiện tại của SP

        // ✅ Tìm dòng CTHD trùng idSanPham VÀ cùng giá → mới được gộp
        Optional<ChiTietHoaDon> existingOpt = hoaDon.getChiTietHoaDons().stream()
                .filter(ct ->
                        ct.getChiTietSanPham().getId().equals(request.getIdSanPhamChiTiet())
                                && ct.getDonGia().compareTo(giaMoi) == 0  // ✅ chỉ gộp nếu cùng giá
                )
                .findFirst();

        if (existingOpt.isPresent()) {
            // Cùng SP + cùng giá → cộng dồn
            ChiTietHoaDon existing = existingOpt.get();
            int soLuongTang = request.getSoLuong();

            if (chiTietSanPham.getSoLuong() < soLuongTang) {
                throw new RuntimeException("Không đủ tồn kho! Còn lại: "
                        + chiTietSanPham.getSoLuong());
            }

            existing.setSoLuong(existing.getSoLuong() + soLuongTang);
            existing.setThanhTien(existing.getDonGia()
                    .multiply(BigDecimal.valueOf(existing.getSoLuong())));

            // Trừ tồn kho
            chiTietSanPham.setSoLuong(chiTietSanPham.getSoLuong() - soLuongTang);
            chiTietSanPhamRepository.save(chiTietSanPham);

        } else {
            // ✅ SP mới HOẶC cùng SP nhưng khác giá → tạo dòng mới riêng
            int soLuong = request.getSoLuong();

            if (chiTietSanPham.getSoLuong() < soLuong) {
                throw new RuntimeException("Không đủ tồn kho! Còn lại: "
                        + chiTietSanPham.getSoLuong());
            }

            ChiTietHoaDon chiTietMoi = new ChiTietHoaDon();
            chiTietMoi.setHoaDon(hoaDon);
            chiTietMoi.setChiTietSanPham(chiTietSanPham);
            chiTietMoi.setSoLuong(soLuong);
            chiTietMoi.setDonGia(giaMoi);  // ✅ Luôn dùng giá hiện tại
            chiTietMoi.setThanhTien(giaMoi.multiply(BigDecimal.valueOf(soLuong)));
            chiTietMoi.setTrangThai(1);
            chiTietMoi.setMaHoaDonChiTiet(
                    hoaDon.getMaHoaDon() + "-CT" + (hoaDon.getChiTietHoaDons().size() + 1)
            );
            chiTietHoaDonRepository.save(chiTietMoi);

            // Trừ tồn kho
            chiTietSanPham.setSoLuong(chiTietSanPham.getSoLuong() - soLuong);
            chiTietSanPhamRepository.save(chiTietSanPham);
        }

        // Tính lại tổng tiền hàng
        BigDecimal tongTien = chiTietHoaDonRepository.tinhTongTienHang(hoaDon.getId());
        hoaDon.setTongTienHang(tongTien != null ? tongTien : BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(
                hoaDon.getTongTienHang()
                        .subtract(hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO)
                        .add(hoaDon.getTienVanChuyen() != null ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO)
        );
        hoaDonRepository.save(hoaDon);

        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }

    // BanHangServiceImpl.java — thêm implement
    @Override
    @Transactional
    public BanHangResponse capNhatSoLuong(Integer idChiTiet, Integer soLuong) {

        ChiTietHoaDon cthd = chiTietHoaDonRepository.findById(idChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn"));

        ChiTietSanPham spct = cthd.getChiTietSanPham();
        int chenhLenh = soLuong - cthd.getSoLuong();

        if (chenhLenh > 0 && spct.getSoLuong() < chenhLenh) {
            throw new RuntimeException("Không đủ hàng trong kho!");
        }

        // ✅ Điều chỉnh tồn kho theo chênh lệch
        // Tăng SL → trừ thêm | Giảm SL → hoàn lại
        spct.setSoLuong(spct.getSoLuong() - chenhLenh);
        chiTietSanPhamRepository.save(spct);

        cthd.setSoLuong(soLuong);
        cthd.setThanhTien(cthd.getDonGia().multiply(BigDecimal.valueOf(soLuong)));
        chiTietHoaDonRepository.save(cthd);

        HoaDon hoaDon = cthd.getHoaDon();
        BigDecimal tongMoi = chiTietHoaDonRepository.tinhTongTienHang(hoaDon.getId());
        if (tongMoi == null) tongMoi = BigDecimal.ZERO;

        BigDecimal tienGiam = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        BigDecimal tienVC = hoaDon.getTienVanChuyen() != null ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO;

        hoaDon.setTongTienHang(tongMoi);
        hoaDon.setTongTienThanhToan(tongMoi.subtract(tienGiam).add(tienVC));
        hoaDonRepository.save(hoaDon);

        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }

    // BanHangServiceImpl.java — implement
    // BanHangServiceImpl.java
    @Override
    @Transactional(readOnly = true)   // ✅ thêm dòng này
    public List<BanHangResponse> getHoaDonCho() {
        return hoaDonRepository.findByTrangThai(0)
                .stream()
                .map(hd -> {
                    HoaDon hdFull = hoaDonRepository.findByIdWithChiTiet(hd.getId());
                    BanHangResponse response = new BanHangResponse(hdFull);

                    // ✅ Lấy địa chỉ mặc định qua native query — không lazy load
                    if (hdFull.getKhachHang() != null
                            && hdFull.getKhachHang().getId() != 999) {

                        Object[] diaChi = khachHangRepository
                                .findDiaChiMacDinh(hdFull.getKhachHang().getId());

                        if (diaChi != null && diaChi.length >= 2) {
                            response.setDiaChiKhachHang(
                                    diaChi[0] != null ? diaChi[0].toString() : ""
                            );
                            response.setTinhThanhKhachHang(
                                    diaChi[1] != null ? diaChi[1].toString() : ""
                            );
                        }
                    }

                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void xoaChiTietHoaDon(Integer idChiTiet) {
        ChiTietHoaDon cthd = chiTietHoaDonRepository.findById(idChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn!"));
        ChiTietSanPham spct = cthd.getChiTietSanPham();
        spct.setSoLuong(spct.getSoLuong() + cthd.getSoLuong());
        chiTietSanPhamRepository.save(spct);

        HoaDon hoaDon = cthd.getHoaDon();
        BigDecimal tongMoi = hoaDon.getTongTienHang().subtract(cthd.getThanhTien());
        if (tongMoi.compareTo(BigDecimal.ZERO) < 0) tongMoi = BigDecimal.ZERO;
        hoaDon.setTongTienHang(tongMoi);
        hoaDon.setTongTienThanhToan(
                tongMoi
                        .subtract(hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO)
                        .add(hoaDon.getTienVanChuyen() != null ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO)
        );
        hoaDonRepository.save(hoaDon);
        chiTietHoaDonRepository.deleteById(idChiTiet);
    }
    @Override
    @Transactional
    public BanHangResponse thanhToan(ThanhToanRequest request) {

        HoaDon hoaDon = hoaDonRepository.findByIdWithChiTiet(request.getIdHoaDon());
        if (hoaDon == null) throw new RuntimeException("Không tìm thấy hóa đơn!");

        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository
                .findById(request.getIdHinhThucThanhToan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hình thức thanh toán!"));
        if (hoaDon.getLoaiHoaDon() == 0) {
            hoaDon.setTrangThai(5);
        } else {
            hoaDon.setTrangThai(1);
        }
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        if (request.getGhiChu() != null && !request.getGhiChu().isBlank()) {
            hoaDon.setGhiChu(request.getGhiChu());
        }
        if (hoaDon.getLoaiHoaDon() == 1
                && hoaDon.getKhachHang() != null
                && hoaDon.getKhachHang().getId() != 999) {

            // Lấy địa chỉ mặc định từ khách hàng nếu dia_chi_nhan chưa được set
            if (hoaDon.getDiaChiNhan() == null || hoaDon.getDiaChiNhan().isBlank()) {
                hoaDon.getKhachHang().getAddresses().stream()
                        .filter(dc -> Boolean.TRUE.equals(dc.getMacDinh()))
                        .findFirst()
                        .ifPresent(dc -> {
                            hoaDon.setDiaChiNhan(
                                    dc.getDiaChiChiTiet() + ", " + dc.getPhuongXa() + ", " + dc.getTinhThanh()
                            );
                            hoaDon.setSdtNguoiNhan(dc.getSdt());
                            hoaDon.setTenNguoiNhan(dc.getNguoiNhan());
                        });
            }
        }
        if (hoaDon.getPhieuGiamGia() != null) {
            PhieuGiamGia phieu = phieuGiamGiaRepository.findById(hoaDon.getPhieuGiamGia().getId())
                    .orElse(null);
            if (phieu != null) {
                int daSuDung = phieu.getSoLuongDaSuDung() != null ? phieu.getSoLuongDaSuDung() : 0;
                phieu.setSoLuongDaSuDung(daSuDung + 1);
                phieu.setNgayCapNhat(LocalDateTime.now());
                phieuGiamGiaRepository.save(phieu);

                // ✅ Nếu phiếu này có gán riêng cho khách hàng hiện tại, đánh dấu đã dùng
                if (hoaDon.getKhachHang() != null) {
                    phieuGiamGiaKhachHangRepository
                            .findChuaSuDung(phieu.getId(), hoaDon.getKhachHang().getId())
                            .ifPresent(pgk -> {
                                pgk.setDaSuDung(true);
                                pgk.setDaSuDungNgay(LocalDateTime.from(java.time.Instant.now()));
                                pgk.setNgaySuDung(LocalDateTime.from(java.time.Instant.now()));
                                phieuGiamGiaKhachHangRepository.save(pgk);
                            });
                }
            }
        }
        hoaDonRepository.save(hoaDon);
        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setHinhThucThanhToan(phuongThuc);
        thanhToan.setSoTien(hoaDon.getTongTienThanhToan());
        thanhToan.setGhiChu(request.getGhiChu());
        thanhToan.setTrangThaiThanhToan("Thành công");
        thanhToan.setTrangThai(1);
        thanhToanRepository.save(thanhToan);
        LichSuThanhToan lichSu = new LichSuThanhToan();
        lichSu.setHoaDon(hoaDon);
        lichSu.setSoTien(hoaDon.getTongTienThanhToan());
        lichSu.setPhuongThucThanhToan(phuongThuc.getTenHinhThuc());
        lichSu.setTrangThaiThanhToan("Thành công");
        String ghiChuLichSu = (request.getGhiChu() != null && !request.getGhiChu().isBlank())
                ? request.getGhiChu()
                : "Thanh toán " + phuongThuc.getTenHinhThuc() + " - HĐ " + hoaDon.getMaHoaDon();
        lichSu.setGhiChu(ghiChuLichSu);
        lichSuThanhToanRepository.save(lichSu);
        if (hoaDon.getLoaiHoaDon() == 1
                && hoaDon.getKhachHang() != null
                && hoaDon.getKhachHang().getEmail() != null
                && !hoaDon.getKhachHang().getId().equals(999)) {
            List<ChiTietEmailDTO> spEmail = hoaDon.getChiTietHoaDons().stream()
                    .map(ct -> new ChiTietEmailDTO(
                            ct.getChiTietSanPham().getIdSanPham().getTenSanPham(),
                            ct.getChiTietSanPham().getIdMauSac() != null
                                    ? ct.getChiTietSanPham().getIdMauSac().getTenMauSac() : "",
                            ct.getChiTietSanPham().getIdTrongLuong() != null
                                    ? ct.getChiTietSanPham().getIdTrongLuong().getTenTrongLuong() : "",
                            ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getThanhTien()
                    ))
                    .collect(Collectors.toList());
            emailService.sendOrderStatusEmail(
                    hoaDon.getKhachHang().getEmail(),
                    hoaDon.getKhachHang().getHoTen(),
                    hoaDon.getMaHoaDon(),
                    "Đã xác nhận đơn hàng",
                    hoaDon.getDiaChiNhan(),          // ✅ Đã được set ở trên
                    hoaDon.getSdtNguoiNhan(),
                    hoaDon.getTongTienHang(),
                    hoaDon.getTienGiam(),
                    hoaDon.getTienVanChuyen(),
                    hoaDon.getTongTienThanhToan(),
                    spEmail,
                    phuongThuc.getTenHinhThuc()      // ✅ Lấy từ DB thay vì null
            );
        }

        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }
    @Override
    @Transactional
    public void huyHoaDon(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithChiTiet(id);
        if (hoaDon == null) throw new RuntimeException("Không tìm thấy hóa đơn!");

        if (hoaDon.getChiTietHoaDons() != null && !hoaDon.getChiTietHoaDons().isEmpty()) {
            // Có sản phẩm → hoàn tồn kho + set trạng thái 6
            for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
                ChiTietSanPham sp = ct.getChiTietSanPham();
                sp.setSoLuong(sp.getSoLuong() + ct.getSoLuong());
                chiTietSanPhamRepository.save(sp);
            }
            hoaDon.setTrangThai(6);
            hoaDonRepository.save(hoaDon);
        } else {
            lichSuHoaDonRepository.deleteByHoaDonId(id);
            hoaDonRepository.deleteById(id);
        }
    }
    @Override
    @Transactional
    public BanHangResponse capNhatLoaiHoaDon(Integer idHoaDon, Integer loaiHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
        hoaDon.setLoaiHoaDon(loaiHoaDon);
        if (loaiHoaDon == 0) {
            hoaDon.setTienVanChuyen(BigDecimal.ZERO);
            BigDecimal tienGiam = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
            hoaDon.setTongTienThanhToan(hoaDon.getTongTienHang().subtract(tienGiam));
        }
        hoaDonRepository.save(hoaDon);
        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }
    @Override
    @Transactional
    public BanHangResponse capNhatPhiVanChuyen(Integer idHoaDon, BigDecimal phiVanChuyen) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
        hoaDon.setTienVanChuyen(phiVanChuyen);
        BigDecimal tienGiam = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        hoaDon.setTongTienThanhToan(
                hoaDon.getTongTienHang().subtract(tienGiam).add(phiVanChuyen)
        );
        hoaDonRepository.save(hoaDon);
        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }

    @Override
    public List<DiaChiKhachHangResponse> getDiaChiKhachHang(Integer idKhachHang) {
        return diaChiKhachHangRepository.findDiaChiByKhachHang(idKhachHang);
    }
    @Override
    public PhieuGiamGiaPosResponse timPhieuGiamGiaTotNhat(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
        Integer idKhachHang = hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null;

        BigDecimal tongTienHang = hoaDon.getTongTienHang();
        BigDecimal tienVanChuyen = hoaDon.getTienVanChuyen() != null
                ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO;
        List<PhieuGiamGia> danhSachPhieu = phieuGiamGiaRepository
                .findPhieuConHieuLucChoKhachHang(LocalDateTime.now(), idKhachHang);
        if (danhSachPhieu.isEmpty()) return null;
        danhSachPhieu = danhSachPhieu.stream()
                .filter(p -> p.getSoLuong() == null || p.getSoLuongDaSuDung() == null
                        || p.getSoLuongDaSuDung() < p.getSoLuong())
                .collect(Collectors.toList());
        if (danhSachPhieu.isEmpty()) return null;
        PhieuGiamGia phieuTotNhat = null;
        BigDecimal soTienGiamMax = BigDecimal.ZERO;
        for (PhieuGiamGia p : danhSachPhieu) {
            BigDecimal toiThieu = p.getGiaTriDonToiThieu() != null
                    ? p.getGiaTriDonToiThieu() : BigDecimal.ZERO;
            if (tongTienHang.compareTo(toiThieu) >= 0) {
                BigDecimal soTienGiam = tinhTienGiam(p, tongTienHang);
                if (soTienGiam.compareTo(soTienGiamMax) > 0) {
                    soTienGiamMax = soTienGiam;
                    phieuTotNhat = p;
                }
            }
        }
        PhieuGiamGia phieuGoiY = null;
        BigDecimal canMuaThemMin = null;
        BigDecimal giamGoiYMax = BigDecimal.ZERO;
        for (PhieuGiamGia p : danhSachPhieu) {
            BigDecimal toiThieu = p.getGiaTriDonToiThieu() != null
                    ? p.getGiaTriDonToiThieu() : BigDecimal.ZERO;
            if (tongTienHang.compareTo(toiThieu) < 0) {
                BigDecimal canThem = toiThieu.subtract(tongTienHang);
                BigDecimal giamNeuDat = tinhTienGiam(p, toiThieu);
                boolean totHonPhieuHienTai = giamNeuDat.compareTo(soTienGiamMax) > 0;
                boolean ganDatHon = canMuaThemMin == null
                        || canThem.compareTo(canMuaThemMin) < 0;
                if (totHonPhieuHienTai && ganDatHon) {
                    canMuaThemMin = canThem;
                    phieuGoiY = p;
                    giamGoiYMax = giamNeuDat;
                }
            }
        }
        PhieuGiamGiaPosResponse response = null;
        if (phieuTotNhat != null) {
            response = new PhieuGiamGiaPosResponse();
            response.setId(phieuTotNhat.getId());
            response.setMaPhieuGiamGia(phieuTotNhat.getMaPhieuGiamGia());
            response.setTenPhieuGiamGia(phieuTotNhat.getTenPhieuGiamGia());
            response.setLoaiPhieuGiamGia(phieuTotNhat.getLoaiPhieuGiamGia());
            response.setGiaTriGiam(phieuTotNhat.getGiaTriGiam());
            response.setGiaTriDonToiThieu(phieuTotNhat.getGiaTriDonToiThieu());
            response.setGiaTriGiamToiDa(phieuTotNhat.getGiaTriGiamToiDa());
            response.setNgayKetThuc(phieuTotNhat.getNgayKetThuc());
            response.setSoTienGiamThucTe(soTienGiamMax);
            response.setCoTheApDung(true);
        } else if (phieuGoiY != null) {
            response = new PhieuGiamGiaPosResponse();
            response.setCoTheApDung(false);
        }
        if (phieuGoiY != null && response != null) {
            PhieuGiamGiaPosResponse.PhieuGoiYResponse goiY =
                    new PhieuGiamGiaPosResponse.PhieuGoiYResponse();
            goiY.setId(phieuGoiY.getId());
            goiY.setMaPhieuGiamGia(phieuGoiY.getMaPhieuGiamGia());
            goiY.setTenPhieuGiamGia(phieuGoiY.getTenPhieuGiamGia());
            goiY.setLoaiPhieuGiamGia(phieuGoiY.getLoaiPhieuGiamGia());
            goiY.setGiaTriGiam(phieuGoiY.getGiaTriGiam());
            goiY.setGiaTriDonToiThieu(phieuGoiY.getGiaTriDonToiThieu());
            goiY.setGiaTriGiamToiDa(phieuGoiY.getGiaTriGiamToiDa());
            goiY.setSoTienCanMuaThem(canMuaThemMin);
            goiY.setSoTienGiamNeuDat(giamGoiYMax);
            response.setPhieuGoiY(goiY);
        }
        return response;
    }
    private BigDecimal tinhTienGiam(PhieuGiamGia p, BigDecimal tongTienHang) {
        if (p.getGiaTriGiam() == null) return BigDecimal.ZERO;
        String loai = p.getLoaiPhieuGiamGia();
        boolean laPhanTram = "PHAN_TRAM".equalsIgnoreCase(loai)
                || (loai != null && loai.contains("%"));
        if (laPhanTram) {
            BigDecimal giam = tongTienHang
                    .multiply(p.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            if (p.getGiaTriGiamToiDa() != null
                    && giam.compareTo(p.getGiaTriGiamToiDa()) > 0) {
                giam = p.getGiaTriGiamToiDa();
            }
            return giam;

        } else {
            BigDecimal giam = p.getGiaTriGiam();
            if (p.getGiaTriGiamToiDa() != null
                    && giam.compareTo(p.getGiaTriGiamToiDa()) > 0) {
                giam = p.getGiaTriGiamToiDa();
            }
            return giam;
        }
    }

    @Override
    @Transactional
    public BanHangResponse apDungPhieuGiamGia(Integer idHoaDon, Integer idPhieuGiamGia) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(idPhieuGiamGia)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá!"));
        if (phieu.getSoLuong() != null && phieu.getSoLuongDaSuDung() != null
                && phieu.getSoLuongDaSuDung() >= phieu.getSoLuong()) {
            throw new RuntimeException("Phiếu giảm giá đã hết lượt sử dụng!");
        }
        BigDecimal toiThieu = phieu.getGiaTriDonToiThieu() != null
                ? phieu.getGiaTriDonToiThieu() : BigDecimal.ZERO;
        if (hoaDon.getTongTienHang().compareTo(toiThieu) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng phiếu!");
        }

        hoaDon.setPhieuGiamGia(phieu);

        BigDecimal tienVanChuyen = hoaDon.getTienVanChuyen() != null
                ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO;

        BigDecimal soTienGiam = tinhTienGiam(phieu, hoaDon.getTongTienHang());
        hoaDon.setTienGiam(soTienGiam);
        hoaDon.setTongTienThanhToan(
                hoaDon.getTongTienHang()
                        .subtract(soTienGiam)
                        .add(tienVanChuyen)
        );

        hoaDonRepository.save(hoaDon);
        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }

    @Override
    public List<KiemTraGiaResponse> kiemTraGiaThayDoi(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithChiTiet(idHoaDon);
        if (hoaDon == null || hoaDon.getChiTietHoaDons() == null) return List.of();

        return hoaDon.getChiTietHoaDons().stream()
                .map(cthd -> {
                    ChiTietSanPham spct = cthd.getChiTietSanPham();
                    BigDecimal giaMoi = tinhGiaSauGiam(spct);   // ✅ thay cho spct.getGiaBan()
                    BigDecimal giaCu = cthd.getDonGia();
                    boolean daThayDoi = giaMoi.compareTo(giaCu) != 0;
                    KiemTraGiaResponse res = new KiemTraGiaResponse(
                            cthd.getId(),
                            spct.getMaCtsp(),
                            giaCu,
                            giaMoi,
                            daThayDoi,
                            spct.getTrangThai() == 1
                    );
                    return res;
                })
                .collect(Collectors.toList());
    }

    private BigDecimal tinhGiaSauGiam(ChiTietSanPham ctsp) {
        BigDecimal giaGoc = ctsp.getGiaBan();
        LocalDateTime gioHienTaiVietNam = LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        List<com.example.AerionSports_BE.entity.ChiTietDotGiamGia> discountLinks =
                chiTietDotGiamGiaRepository.findBestActiveByChiTietSanPhamId(ctsp.getId(), gioHienTaiVietNam);

        if (discountLinks != null && !discountLinks.isEmpty()) {
            com.example.AerionSports_BE.entity.DotGiamGia dgg = discountLinks.get(0).getDotGiamGia();
            if (dgg != null && dgg.getGiaTriGiam() != null) {
                BigDecimal heSo = BigDecimal.valueOf(100).subtract(dgg.getGiaTriGiam());
                return giaGoc.multiply(heSo).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            }
        }
        return giaGoc;
    }
    @Override
    @Transactional(readOnly = true)
    public SanPhamPosDTO timSanPhamTheoMa(String maCtsp) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findByMaCtsp(maCtsp)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maCtsp));

        if (ctsp.getTrangThai() != 1) {
            throw new RuntimeException("Sản phẩm này đã ngừng kinh doanh!");
        }

        SanPhamPosDTO dto = new SanPhamPosDTO();
        dto.setId(ctsp.getId());
        dto.setMa(ctsp.getMaCtsp());
        dto.setTen(ctsp.getIdSanPham().getTenSanPham());
        dto.setMauSac(ctsp.getIdMauSac() != null ? ctsp.getIdMauSac().getTenMauSac() : "");
        dto.setTrongLuong(ctsp.getIdTrongLuong() != null ? ctsp.getIdTrongLuong().getTenTrongLuong() : "");

        BigDecimal giaGoc = ctsp.getGiaBan();
        BigDecimal giaSauGiam = tinhGiaSauGiam(ctsp);
        dto.setGia(giaSauGiam);
        dto.setGiaGoc(giaGoc);

        dto.setSoLuongTon(ctsp.getSoLuong());

        if (ctsp.getHinhAnhs() != null) {
            ctsp.getHinhAnhs().stream()
                    .filter(h -> Boolean.TRUE.equals(h.getLaAnhChinh()))
                    .findFirst()
                    .ifPresent(h -> dto.setAnh(baseUrl + h.getDuongDanAnh()));
        }

        return dto;
    }
    @Override
    @Transactional
    public List<DiaChiKhachHangResponse> themDiaChiKhachHang(Integer idKhachHang, DiaChiRequest request) {
        KhachHang khachHang = khachHangRepository.findById(idKhachHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng!"));

        if (Boolean.TRUE.equals(request.getMacDinh())) {
            diaChiKhachHangRepository.boMacDinhTheoKhachHang(idKhachHang);
        }

        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setKhachHang(khachHang);
        diaChi.setNguoiNhan(request.getNguoiNhan());
        diaChi.setSdt(request.getSdt());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setDiaChiChiTiet(request.getDiaChiChiTiet());
        diaChi.setMacDinh(Boolean.TRUE.equals(request.getMacDinh()));
        diaChiKhachHangRepository.save(diaChi);

        return diaChiKhachHangRepository.findDiaChiByKhachHang(idKhachHang);
    }

    @Override
    @Transactional
    public List<DiaChiKhachHangResponse> capNhatDiaChiKhachHang(Integer idDiaChi, DiaChiRequest request) {
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(idDiaChi)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        Integer idKhachHang = diaChi.getKhachHang().getId();
        if (Boolean.TRUE.equals(request.getMacDinh())) {
            diaChiKhachHangRepository.boMacDinhTheoKhachHang(idKhachHang);
        }

        diaChi.setNguoiNhan(request.getNguoiNhan());
        diaChi.setSdt(request.getSdt());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setDiaChiChiTiet(request.getDiaChiChiTiet());
        diaChi.setMacDinh(Boolean.TRUE.equals(request.getMacDinh()));
        diaChiKhachHangRepository.save(diaChi);

        return diaChiKhachHangRepository.findDiaChiByKhachHang(idKhachHang);
    }

    @Override
    @Transactional
    public BanHangResponse capNhatDiaChiGiaoHang(Integer idHoaDon, Integer idDiaChi) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
        DiaChiKhachHang diaChi = diaChiKhachHangRepository.findById(idDiaChi)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ!"));

        hoaDon.setTenNguoiNhan(diaChi.getNguoiNhan());
        hoaDon.setSdtNguoiNhan(diaChi.getSdt());
        hoaDon.setDiaChiNhan(
                diaChi.getDiaChiChiTiet() + ", " + diaChi.getPhuongXa() + ", " + diaChi.getTinhThanh()
        );
        hoaDonRepository.save(hoaDon);

        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }
    @Override
    @Transactional
    public BanHangResponse capNhatDiaChiGiaoHangThuCong(Integer idHoaDon, DiaChiGiaoHangThuCongRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));
        hoaDon.setTenNguoiNhan(request.getNguoiNhan());
        hoaDon.setSdtNguoiNhan(request.getSdt());
        hoaDon.setDiaChiNhan(
                request.getDiaChiChiTiet() + ", " + request.getPhuongXa() + ", " + request.getTinhThanh()
        );
        hoaDonRepository.save(hoaDon);
        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }
}