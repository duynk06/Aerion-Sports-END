package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import com.example.AerionSports_BE.dto.response.LichSuHoaDonResponse;
import com.example.AerionSports_BE.dto.response.LichSuThanhToanResponse;
import com.example.AerionSports_BE.entity.*;
import com.example.AerionSports_BE.repository.*;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuThanhToanRepository;
import com.example.AerionSports_BE.dto.response.HoaDonResponse;
import com.example.AerionSports_BE.service.EmailService;
import com.example.AerionSports_BE.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    private static final int LOAI_HOA_DON_TAI_QUAY = 0;
    private static final int LOAI_HOA_DON_ONLINE = 1;
    private static final int TRANG_THAI_CHO_XAC_NHAN = 0;
    private static final int TRANG_THAI_DA_XAC_NHAN = 1;
    private static final int TRANG_THAI_DA_HUY = 6;

    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    @Autowired
    private PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;

    @Autowired
    private LichSuThanhToanRepository lichSuThanhToanRepository;

    @Autowired
    private BanHangOnlineLichSuThanhToanRepository banHangOnlineLichSuThanhToanRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonResponse> hienThi() {
        return hoaDonRepository
                .findAll()
                .stream()
                .map(HoaDonResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonResponse> search(String keyword) {
        return hoaDonRepository.search(keyword)
                .stream()
                .map(HoaDonResponse::new)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HoaDonResponse> filterHoaDon(
            String keyword,
            Integer loaiHoaDon,
            Integer trangThai,
            LocalDate tuNgay,
            LocalDate denNgay,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page, size,
                Sort.by(Sort.Direction.DESC, "ngayCapNhat")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );

        return hoaDonRepository
                .filterHoaDon(
                        keyword,
                        loaiHoaDon,
                        trangThai,
                        tuNgay,
                        denNgay,
                        pageable
                )
                .map(HoaDonResponse::new);
    }
    @Override
    @Transactional(readOnly = true)
    public List<HoaDonResponse> filterHoaDonKhongPhanTrang(
            String keyword,
            Integer loaiHoaDon,
            Integer trangThai,
            LocalDate tuNgay,
            LocalDate denNgay
    ) {
        return hoaDonRepository
                .filterHoaDonKhongPhanTrang(keyword, loaiHoaDon, trangThai, tuNgay, denNgay)
                .stream()
                .map(HoaDonResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonResponse detail(Integer id) {
        HoaDon hoaDon = hoaDonRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));

        return new HoaDonResponse(hoaDon);
    }

    @Override
    @Transactional
    public HoaDonResponse chuyenTrangThai(Integer id, Integer trangThaiMoi, String ghiChu, String username) {
        HoaDon hoaDon = hoaDonRepository.findByIdWithChiTiet(id);   // ✅ đổi sang bản fetch kèm chi tiết để dùng ngay bên dưới
        if (hoaDon == null) {
            throw new RuntimeException("Không tìm thấy hóa đơn!");
        }

        Integer trangThaiCu = hoaDon.getTrangThai();
        validateChuyenTrangThai(trangThaiCu, trangThaiMoi, hoaDon.getLoaiHoaDon());

        NhanVien nv = null;
        if (username != null) {
            nv = nhanVienRepository.findByEmail(username).orElse(null);
        }
        if (nv == null) {
            nv = nhanVienRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien!"));
        }

        if (canTruTonKhoKhiXacNhan(hoaDon, trangThaiCu, trangThaiMoi)) {
            truTonKhoHoaDon(hoaDon);
        }

        hoaDon.setTrangThai(trangThaiMoi);
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        if (laHoaDonOnline(hoaDon)) {
            hoaDon.setNhanVien(nv);
        }

        // ✅ HỦY ĐƠN: hoàn tồn kho + hoàn lại lượt dùng phiếu giảm giá (nếu có)
        if (Objects.equals(trangThaiMoi, TRANG_THAI_DA_HUY)) {
            // Hoàn tồn kho từng sản phẩm trong đơn
            if (canHoanTonKhoKhiHuy(hoaDon, trangThaiCu)) {
                hoanTonKhoHoaDon(hoaDon);
            }

            // Hoàn lại phiếu giảm giá đã áp dụng cho đơn này (nếu có)
            if (hoaDon.getPhieuGiamGia() != null) {
                PhieuGiamGia phieu = phieuGiamGiaRepository.findById(hoaDon.getPhieuGiamGia().getId())
                        .orElse(null);
                if (phieu != null) {
                    int daSuDung = phieu.getSoLuongDaSuDung() != null ? phieu.getSoLuongDaSuDung() : 0;
                    phieu.setSoLuongDaSuDung(Math.max(0, daSuDung - 1));   // ✅ trừ lại, không cho âm
                    phieu.setNgayCapNhat(LocalDateTime.now());
                    phieuGiamGiaRepository.save(phieu);

                    // Nếu phiếu này có gắn riêng cho khách hàng, đặt lại thành CHƯA sử dụng
                    if (hoaDon.getKhachHang() != null) {
                        phieuGiamGiaKhachHangRepository
                                .findDaSuDung(phieu.getId(), hoaDon.getKhachHang().getId())
                                .ifPresent(pgk -> {
                                    pgk.setDaSuDung(false);
                                    pgk.setDaSuDungNgay(null);
                                    pgk.setNgaySuDung(null);
                                    phieuGiamGiaKhachHangRepository.save(pgk);
                                });
                    }
                }
            }
        }

        hoaDonRepository.save(hoaDon);

        LichSuHoaDon lichSu = new LichSuHoaDon();
        lichSu.setHoaDon(hoaDon);
        lichSu.setNhanVien(nv);
        lichSu.setTrangThaiCu(trangThaiCu);
        lichSu.setTrangThaiMoi(trangThaiMoi);
        lichSu.setHanhDong(getTrangThaiText(trangThaiCu) + " → " + getTrangThaiText(trangThaiMoi));
        lichSu.setGhiChu(ghiChu != null ? ghiChu : "");
        lichSu.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSu);

        HoaDon hdSauKhiSave = hoaDonRepository.findByIdWithChiTiet(id);
        if (hdSauKhiSave != null
                && hdSauKhiSave.getKhachHang() != null
                && hdSauKhiSave.getKhachHang().getEmail() != null
                && !hdSauKhiSave.getKhachHang().getId().equals(999)) {

            List<ChiTietEmailDTO> spEmail = new ArrayList<>();
            if (hdSauKhiSave.getChiTietHoaDons() != null) {
                spEmail = hdSauKhiSave.getChiTietHoaDons().stream()
                        .map(ct -> new ChiTietEmailDTO(
                                ct.getChiTietSanPham().getIdSanPham().getTenSanPham(),
                                ct.getChiTietSanPham().getIdMauSac() != null
                                        ? ct.getChiTietSanPham().getIdMauSac().getTenMauSac() : "",
                                ct.getChiTietSanPham().getIdTrongLuong() != null
                                        ? ct.getChiTietSanPham().getIdTrongLuong().getTenTrongLuong() : "",
                                ct.getSoLuong(), ct.getDonGia(), ct.getThanhTien()
                        ))
                        .collect(Collectors.toList());
            }

            String tenPhuongThuc = lichSuThanhToanRepository
                    .findByHoaDon_IdOrderByNgayThanhToanDesc(id)
                    .stream().findFirst()
                    .map(LichSuThanhToan::getPhuongThucThanhToan)
                    .orElse("Chuyển khoản");

            emailService.sendOrderStatusEmail(
                    hdSauKhiSave.getKhachHang().getEmail(),
                    hdSauKhiSave.getKhachHang().getHoTen(),
                    hdSauKhiSave.getMaHoaDon(),
                    getTrangThaiText(trangThaiMoi),
                    hdSauKhiSave.getDiaChiNhan(),
                    hdSauKhiSave.getSdtNguoiNhan(),
                    hdSauKhiSave.getTongTienHang(),
                    hdSauKhiSave.getTienGiam(),
                    hdSauKhiSave.getTienVanChuyen(),
                    hdSauKhiSave.getTongTienThanhToan(),
                    spEmail, tenPhuongThuc
            );
        }

        return new HoaDonResponse(hoaDonRepository.findById(id).orElse(hoaDon));
    }

    private boolean canTruTonKhoKhiXacNhan(HoaDon hoaDon, Integer trangThaiCu, Integer trangThaiMoi) {
        return laHoaDonOnline(hoaDon)
                && Objects.equals(trangThaiCu, TRANG_THAI_CHO_XAC_NHAN)
                && Objects.equals(trangThaiMoi, TRANG_THAI_DA_XAC_NHAN);
    }

    private boolean canHoanTonKhoKhiHuy(HoaDon hoaDon, Integer trangThaiCu) {
        if (hoaDon == null) {
            return false;
        }
        if (Objects.equals(hoaDon.getLoaiHoaDon(), LOAI_HOA_DON_TAI_QUAY)) {
            return true;
        }
        return laHoaDonOnline(hoaDon)
                && trangThaiCu != null
                && trangThaiCu >= TRANG_THAI_DA_XAC_NHAN
                && trangThaiCu <= 4
                && !Objects.equals(trangThaiCu, TRANG_THAI_DA_HUY);
    }

    private void truTonKhoHoaDon(HoaDon hoaDon) {
        if (hoaDon.getChiTietHoaDons() == null || hoaDon.getChiTietHoaDons().isEmpty()) {
            throw new RuntimeException("Hoa don chua co san pham de xac nhan!");
        }

        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            ChiTietSanPham spct = ct.getChiTietSanPham();
            int soLuongDat = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
            int tonKho = spct == null || spct.getSoLuong() == null ? 0 : spct.getSoLuong();
            if (spct == null || soLuongDat <= 0 || tonKho < soLuongDat) {
                throw new RuntimeException("Khong du ton kho de xac nhan hoa don!");
            }
        }

        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            ChiTietSanPham spct = ct.getChiTietSanPham();
            spct.setSoLuong(spct.getSoLuong() - ct.getSoLuong());
            chiTietSanPhamRepository.save(spct);
        }
    }

    private void hoanTonKhoHoaDon(HoaDon hoaDon) {
        if (hoaDon.getChiTietHoaDons() == null) {
            return;
        }
        for (ChiTietHoaDon ct : hoaDon.getChiTietHoaDons()) {
            ChiTietSanPham spct = ct.getChiTietSanPham();
            if (spct != null && ct.getSoLuong() != null) {
                spct.setSoLuong((spct.getSoLuong() == null ? 0 : spct.getSoLuong()) + ct.getSoLuong());
                chiTietSanPhamRepository.save(spct);
            }
        }
    }

    private void ghiNhanThanhToanKhiDonOnlineDaGiao(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getId() == null) {
            return;
        }

        if (banHangOnlineLichSuThanhToanRepository.existsByHoaDon_IdAndTrangThaiThanhToan(
                hoaDon.getId(), "Đã thanh toán")) {
            return;
        }

        LichSuThanhToan lichSuThanhToan = banHangOnlineLichSuThanhToanRepository
                .findFirstByHoaDon_IdAndTrangThaiThanhToanOrderByNgayThanhToanDesc(
                        hoaDon.getId(), "Chưa thanh toán")
                .orElseGet(LichSuThanhToan::new);

        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setSoTien(
                hoaDon.getTongTienThanhToan() == null ? BigDecimal.ZERO : hoaDon.getTongTienThanhToan()
        );
        if (lichSuThanhToan.getPhuongThucThanhToan() == null
                || lichSuThanhToan.getPhuongThucThanhToan().isBlank()) {
            lichSuThanhToan.setPhuongThucThanhToan("Thanh toán khi nhận hàng");
        }
        lichSuThanhToan.setTrangThaiThanhToan("Đã thanh toán");
        lichSuThanhToan.setNgayThanhToan(LocalDateTime.now());
        lichSuThanhToan.setGhiChu("Đơn online đã giao hàng, tự động ghi nhận thanh toán COD.");

        banHangOnlineLichSuThanhToanRepository.save(lichSuThanhToan);
    }

    private void hoanLuotSuDungPhieuGiamGia(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getPhieuGiamGia() == null) {
            return;
        }

        PhieuGiamGia phieu = hoaDon.getPhieuGiamGia();
        int soLuongDaSuDung = phieu.getSoLuongDaSuDung() == null ? 0 : phieu.getSoLuongDaSuDung();
        if (soLuongDaSuDung > 0) {
            phieu.setSoLuongDaSuDung(soLuongDaSuDung - 1);
            phieu.setNgayCapNhat(LocalDateTime.now());
            phieuGiamGiaRepository.save(phieu);
        }

        if (hoaDon.getKhachHang() == null
                || phieu.getId() == null
                || hoaDon.getKhachHang().getId() == null) {
            return;
        }

        phieuGiamGiaKhachHangRepository.findDaSuDung(phieu.getId(), hoaDon.getKhachHang().getId())
                .ifPresent(phieuKhachHang -> {
                    phieuKhachHang.setDaSuDung(false);
                    phieuKhachHang.setDaSuDungNgay(null);
                    phieuKhachHang.setNgaySuDung(null);
                    phieuKhachHang.setTrangThai(1);
                    phieuGiamGiaKhachHangRepository.save(phieuKhachHang);
                });
    }

    private boolean laHoaDonOnline(HoaDon hoaDon) {
        return hoaDon != null && Objects.equals(hoaDon.getLoaiHoaDon(), LOAI_HOA_DON_ONLINE);
    }

    private void validateChuyenTrangThai(Integer cu, Integer moi, Integer loaiHoaDon) {
        if (cu == 6 || cu == 5) {
            throw new RuntimeException("Không thể chuyển trạng thái từ trạng thái này!");
        }
        if (loaiHoaDon == 0) {
            if (cu == 0 && (moi == 5 || moi == 6)) return;
            throw new RuntimeException("Đơn tại quầy chỉ được chuyển sang Hoàn thành hoặc Hủy!");
        }
        if (moi == 6) return;
        if (moi != cu + 1) {
            throw new RuntimeException("Chỉ được chuyển sang trạng thái tiếp theo!");
        }
    }

    private String getTrangThaiText(Integer status) {
        return switch (status) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Chờ giao hàng";
            case 3 -> "Đang giao hàng";
            case 4 -> "Đã giao hàng";
            case 5 -> "Đã hoàn thành";
            case 6 -> "Đã hủy";
            default -> "Khởi tạo";
        };
    }

    @Override
    public List<ChiTietHoaDonResponse> getChiTietHoaDon(Integer idHoaDon) {
        return chiTietHoaDonRepository.getChiTietHoaDon(idHoaDon)
                .stream()
                .map(p -> new ChiTietHoaDonResponse(
                        p.getId(),
                        p.getMaSanPham(),
                        p.getTenSanPham(),
                        p.getMauSac(),
                        p.getTrongLuong(),
                        p.getSoLuong(),
                        p.getDonGia(),
                        p.getThanhTien(),
                        p.getAnh(),
                        p.getGiaGoc(),
                        p.getTenDotGiamGia()
                ))
                .collect(Collectors.toList());
    }
    @Override
    public List<LichSuThanhToanResponse> getLichSuThanhToan(Integer idHoaDon) {
        List<LichSuThanhToan> lichSuThanhToans = lichSuThanhToanRepository
                .findByHoaDon_IdOrderByNgayThanhToanDesc(idHoaDon);

        boolean daCoDongDaThanhToan = lichSuThanhToans.stream()
                .anyMatch(item -> Objects.equals(item.getTrangThaiThanhToan(), "Đã thanh toán"));
        HoaDon hoaDon = hoaDonRepository.findById(idHoaDon).orElse(null);
        if (!daCoDongDaThanhToan
                && hoaDon != null
                && laHoaDonOnline(hoaDon)
                && hoaDon.getTrangThai() != null
                && hoaDon.getTrangThai() >= 4
                && !Objects.equals(hoaDon.getTrangThai(), 6)) {
            ghiNhanThanhToanKhiDonOnlineDaGiao(hoaDon);
            lichSuThanhToans = lichSuThanhToanRepository.findByHoaDon_IdOrderByNgayThanhToanDesc(idHoaDon);
        }

        return lichSuThanhToans
                .stream()
                .map(LichSuThanhToanResponse::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuHoaDonResponse> getLichSuHoaDon(Integer idHoaDon) {
        return lichSuHoaDonRepository.findByHoaDonIdWithNhanVien(idHoaDon)
                .stream()
                .map(LichSuHoaDonResponse::new)
                .collect(Collectors.toList());
    }
}
