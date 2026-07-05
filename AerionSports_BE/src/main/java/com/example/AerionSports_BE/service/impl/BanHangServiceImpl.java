package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.SanPhamPosDTO;
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
import java.util.stream.Stream;

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
    // BanHangServiceImpl.java — sửa hàm taoHoaDonCho
    // BanHangServiceImpl.java
    @Override
    public BanHangResponse taoHoaDonCho(String username) {
        long soHoaDonCho = hoaDonRepository.countByTrangThaiAndLoaiHoaDon(0, 0);
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

        BigDecimal giaMoi = chiTietSanPham.getGiaBan(); // Giá hiện tại của SP

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
    public List<BanHangResponse> getHoaDonCho() {
        return hoaDonRepository.findByTrangThaiAndLoaiHoaDon(0, 0)
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

    // BanHangServiceImpl.java
    @Override
    @Transactional
    public void xoaChiTietHoaDon(Integer idChiTiet) {

        ChiTietHoaDon cthd = chiTietHoaDonRepository.findById(idChiTiet)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn!"));

        // ✅ Hoàn lại tồn kho
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
    // BanHangServiceImpl.java


    @Override
    @Transactional
    public BanHangResponse thanhToan(ThanhToanRequest request) {

        HoaDon hoaDon = hoaDonRepository.findByIdWithChiTiet(request.getIdHoaDon());
        if (hoaDon == null) throw new RuntimeException("Không tìm thấy hóa đơn!");

        PhuongThucThanhToan phuongThuc = phuongThucThanhToanRepository
                .findById(request.getIdHinhThucThanhToan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hình thức thanh toán!"));

        // Cập nhật trạng thái
        if (hoaDon.getLoaiHoaDon() == 0) {
            hoaDon.setTrangThai(5);
        } else {
            hoaDon.setTrangThai(1);
        }

        // ✅ Lưu ghi chú nếu có
        if (request.getGhiChu() != null && !request.getGhiChu().isBlank()) {
            hoaDon.setGhiChu(request.getGhiChu());
        }

        // ✅ Lưu địa chỉ giao hàng từ khách hàng vào hóa đơn (nếu giao hàng và chưa có)
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

        hoaDonRepository.save(hoaDon);

        // Lưu thanh toán
        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setHoaDon(hoaDon);
        thanhToan.setHinhThucThanhToan(phuongThuc);
        thanhToan.setSoTien(hoaDon.getTongTienThanhToan());
        thanhToan.setGhiChu(request.getGhiChu());
        thanhToan.setTrangThaiThanhToan("Thành công");
        thanhToan.setTrangThai(1);
        thanhToanRepository.save(thanhToan);

        // Lưu lịch sử thanh toán
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

        // ✅ Gửi mail nếu là đơn giao hàng
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
    // BanHangServiceImpl.java
    // BanHangServiceImpl.java — sửa hàm huyHoaDon
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
            // Không có sản phẩm → xóa hẳn, nhưng phải xóa lich_su_hoa_don trước
            lichSuHoaDonRepository.deleteByHoaDonId(id);  // ✅ xóa FK trước
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

    // BanHangServiceImpl.java
    // BanHangServiceImpl.java
    @Override
    public List<DiaChiKhachHangResponse> getDiaChiKhachHang(Integer idKhachHang) {
        // ✅ Dùng native query trực tiếp, không qua entity lazy load
        return diaChiKhachHangRepository.findDiaChiByKhachHang(idKhachHang);
    }


    // BanHangServiceImpl.java
    @Override
    public PhieuGiamGiaPosResponse timPhieuGiamGiaTotNhat(Integer idHoaDon) {

        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        BigDecimal tongTienHang = hoaDon.getTongTienHang();
        BigDecimal tienVanChuyen = hoaDon.getTienVanChuyen() != null
                ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO;

        List<PhieuGiamGia> danhSachPhieu = phieuGiamGiaRepository
                .findPhieuConHieuLuc(LocalDateTime.now());

        if (danhSachPhieu.isEmpty()) return null;

        // Tìm phiếu tốt nhất áp dụng được
        PhieuGiamGia phieuTotNhat = null;
        BigDecimal soTienGiamMax = BigDecimal.ZERO;

        // Tìm phiếu gợi ý tốt hơn (chưa đủ điều kiện nhưng giảm nhiều hơn phiếu hiện tại)
        PhieuGiamGia phieuGoiY = null;
        BigDecimal canMuaThemMin = null;
        BigDecimal giamGoiYMax = BigDecimal.ZERO;

        for (PhieuGiamGia p : danhSachPhieu) {
            BigDecimal toiThieu = p.getGiaTriDonToiThieu() != null
                    ? p.getGiaTriDonToiThieu() : BigDecimal.ZERO;

            if (tongTienHang.compareTo(toiThieu) >= 0) {
                // Đủ điều kiện → tính tiền giảm
                BigDecimal soTienGiam;
                if ("VAN_CHUYEN".equalsIgnoreCase(p.getLoaiPhieuGiamGia())) {
                    if (tienVanChuyen.compareTo(BigDecimal.ZERO) > 0) {
                        soTienGiam = p.getGiaTriGiam().min(tienVanChuyen);
                    } else continue;
                } else {
                    soTienGiam = tinhTienGiam(p, tongTienHang);
                }

                if (soTienGiam.compareTo(soTienGiamMax) > 0) {
                    soTienGiamMax = soTienGiam;
                    phieuTotNhat = p;
                }

            } else {
                // Chưa đủ điều kiện → xem xét làm phiếu gợi ý
                BigDecimal canThem = toiThieu.subtract(tongTienHang);
                // Tính xem nếu đạt thì giảm được bao nhiêu
                BigDecimal giamNeuDat = tinhTienGiam(p, toiThieu);

                // Ưu tiên gợi ý phiếu giảm nhiều hơn phiếu đang áp dụng
                // và gần đạt nhất (canThem nhỏ nhất trong số các phiếu tốt hơn)
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

        // Xây dựng response
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
            // Không có phiếu nào áp dụng được → trả về phiếu gợi ý
            response = new PhieuGiamGiaPosResponse();
            response.setCoTheApDung(false);
        }

        // ✅ Luôn kèm phiếu gợi ý nếu có (dù đang áp dụng phiếu hay không)
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

        // Phần trăm: PHAN_TRAM hoặc bất kỳ loại nào có chứa "%"
        boolean laPhanTram = "PHAN_TRAM".equalsIgnoreCase(loai)
                || (loai != null && loai.contains("%"));

        if (laPhanTram) {
            // Tính: tongTien * phanTram / 100
            BigDecimal giam = tongTienHang
                    .multiply(p.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            // Giới hạn tối đa nếu có
            if (p.getGiaTriGiamToiDa() != null
                    && giam.compareTo(p.getGiaTriGiamToiDa()) > 0) {
                giam = p.getGiaTriGiamToiDa();
            }
            return giam;

        } else if ("VAN_CHUYEN".equalsIgnoreCase(loai)) {
            // Giảm phí ship: trả về giá trị giảm (không vượt quá tiền ship thực tế)
            return p.getGiaTriGiam();

        } else {
            // TIEN_MAT: giảm thẳng số tiền cố định
            BigDecimal giam = p.getGiaTriGiam();

            // Giới hạn tối đa nếu có (phòng trường hợp giảm nhiều hơn đơn hàng)
            if (p.getGiaTriGiamToiDa() != null
                    && giam.compareTo(p.getGiaTriGiamToiDa()) > 0) {
                giam = p.getGiaTriGiamToiDa();
            }
            return giam;
        }
    }

    private PhieuGiamGiaPosResponse buildResponse(
            PhieuGiamGia p, BigDecimal soTienGiam, boolean coTheApDung
    ) {
        PhieuGiamGiaPosResponse response = new PhieuGiamGiaPosResponse();
        response.setId(p.getId());
        response.setMaPhieuGiamGia(p.getMaPhieuGiamGia());
        response.setTenPhieuGiamGia(p.getTenPhieuGiamGia());
        response.setLoaiPhieuGiamGia(p.getLoaiPhieuGiamGia());
        response.setGiaTriGiam(p.getGiaTriGiam());
        response.setGiaTriDonToiThieu(p.getGiaTriDonToiThieu());
        response.setGiaTriGiamToiDa(p.getGiaTriGiamToiDa());
        response.setNgayKetThuc(p.getNgayKetThuc());
        response.setSoTienGiamThucTe(soTienGiam);
        response.setCoTheApDung(coTheApDung);

        // Lưu ý: Không set canMuaThem ở đây vì biến này thuộc về PhieuGoiYResponse
        return response;
    }

    @Override
    @Transactional
    public BanHangResponse apDungPhieuGiamGia(Integer idHoaDon, Integer idPhieuGiamGia) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(idPhieuGiamGia)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu giảm giá!"));

        // Kiểm tra điều kiện đơn tối thiểu
        BigDecimal toiThieu = phieu.getGiaTriDonToiThieu() != null
                ? phieu.getGiaTriDonToiThieu() : BigDecimal.ZERO;
        if (hoaDon.getTongTienHang().compareTo(toiThieu) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng phiếu!");
        }

        hoaDon.setPhieuGiamGia(phieu);

        BigDecimal tienVanChuyen = hoaDon.getTienVanChuyen() != null
                ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO;

        String loai = phieu.getLoaiPhieuGiamGia();

        if ("VAN_CHUYEN".equalsIgnoreCase(loai)) {
            // Giảm phí ship — tienGiam = phí ship thực tế (không thể giảm nhiều hơn phí ship)
            BigDecimal giamShip = phieu.getGiaTriGiam().min(tienVanChuyen);
            hoaDon.setTienGiam(giamShip);
            hoaDon.setTongTienThanhToan(
                    hoaDon.getTongTienHang()
                            .subtract(BigDecimal.ZERO) // không giảm tiền hàng
                            .add(tienVanChuyen.subtract(giamShip)) // ship sau giảm
            );
        } else {
            BigDecimal soTienGiam = tinhTienGiam(phieu, hoaDon.getTongTienHang());
            hoaDon.setTienGiam(soTienGiam);
            hoaDon.setTongTienThanhToan(
                    hoaDon.getTongTienHang()
                            .subtract(soTienGiam)
                            .add(tienVanChuyen)
            );
        }

        hoaDonRepository.save(hoaDon);
        return new BanHangResponse(hoaDonRepository.findByIdWithChiTiet(hoaDon.getId()));
    }

    @Override
    @Transactional
    public BanHangResponse boPhieuGiamGia(Integer idHoaDon) {
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        hoaDon.setPhieuGiamGia(null);
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(
                hoaDon.getTongTienHang()
                        .add(hoaDon.getTienVanChuyen() != null ? hoaDon.getTienVanChuyen() : BigDecimal.ZERO)
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
                    BigDecimal giaMoi = spct.getGiaBan();
                    BigDecimal giaCu = cthd.getDonGia();
                    boolean daThayDoi = giaMoi.compareTo(giaCu) != 0;

                    KiemTraGiaResponse res = new KiemTraGiaResponse(
                            cthd.getId(),
                            spct.getMaCtsp(),
                            giaCu,
                            giaMoi,
                            daThayDoi,
                            spct.getTrangThai() == 1   // ✅ field trangThai đúng chỗ
                    );
                    return res;
                })
                .collect(Collectors.toList());
    }

    // BanHangServiceImpl.java
    @Override
    @Transactional(readOnly = true)   // ✅ Giữ session để load lazy entities
    public SanPhamPosDTO timSanPhamTheoMa(String maCtsp) {
        ChiTietSanPham ctsp = chiTietSanPhamRepository.findByMaCtsp(maCtsp)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm: " + maCtsp));

        if (ctsp.getTrangThai() != 1) {
            throw new RuntimeException("Sản phẩm này đã ngừng kinh doanh!");
        }

        SanPhamPosDTO dto = new SanPhamPosDTO();
        dto.setId(ctsp.getId());
        dto.setMa(ctsp.getMaCtsp());
        dto.setTen(ctsp.getIdSanPham().getTenSanPham());    // ✅ lazy load an toàn trong @Transactional
        dto.setMauSac(ctsp.getIdMauSac() != null
                ? ctsp.getIdMauSac().getTenMauSac() : "");
        dto.setTrongLuong(ctsp.getIdTrongLuong() != null
                ? ctsp.getIdTrongLuong().getTenTrongLuong() : "");
        dto.setGia(ctsp.getGiaBan());
        dto.setSoLuongTon(ctsp.getSoLuong());

        // Lấy ảnh chính
        if (ctsp.getHinhAnhs() != null) {
            ctsp.getHinhAnhs().stream()
                    .filter(h -> Boolean.TRUE.equals(h.getLaAnhChinh()))
                    .findFirst()
                    .ifPresent(h -> dto.setAnh("http://localhost:8080" + h.getDuongDanAnh()));
        }

        return dto;
    }
}