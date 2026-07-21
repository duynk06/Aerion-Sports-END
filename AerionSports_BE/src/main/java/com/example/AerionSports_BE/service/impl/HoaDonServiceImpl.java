package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import com.example.AerionSports_BE.dto.response.LichSuHoaDonResponse;
import com.example.AerionSports_BE.dto.response.LichSuThanhToanResponse;
import com.example.AerionSports_BE.entity.ChiTietHoaDon;
import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.HoaDon;
import com.example.AerionSports_BE.entity.LichSuHoaDon;
import com.example.AerionSports_BE.entity.LichSuThanhToan;
import com.example.AerionSports_BE.entity.NhanVien;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Autowired
    private NhanVienRepository nhanVienRepository;
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired
    private EmailService emailService;

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
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn!"));

        Integer trangThaiCu = hoaDon.getTrangThai();
        validateChuyenTrangThai(trangThaiCu, trangThaiMoi, hoaDon.getLoaiHoaDon());

        hoaDon.setTrangThai(trangThaiMoi);
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);

        NhanVien nv = null;
        if (username != null) {
            nv = nhanVienRepository.findByEmail(username).orElse(null);
        }
        if (nv == null) {
            nv = nhanVienRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
        }

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
        if (hdSauKhiSave != null && laHoaDonOnline(hdSauKhiSave)) {
            if (Objects.equals(trangThaiCu, 0) && Objects.equals(trangThaiMoi, 1)) {
                truTonKhoKhiXacNhanDonOnline(hdSauKhiSave);
            }
            if (trangThaiCu != null && trangThaiCu >= 1 && Objects.equals(trangThaiMoi, 6)) {
                hoanTonKhoKhiHuyDonOnlineDaXacNhan(hdSauKhiSave);
            }
        }

        if (hdSauKhiSave != null
                && laHoaDonOnline(hdSauKhiSave)
                && !Objects.equals(trangThaiCu, 4)
                && Objects.equals(trangThaiMoi, 4)) {
            ghiNhanThanhToanKhiDonOnlineDaGiao(hdSauKhiSave);
        }

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
        lichSuThanhToan.setGhiChu("Đơn online đã giao hàng, tự động ghi nhận thanh toán.");
        banHangOnlineLichSuThanhToanRepository.save(lichSuThanhToan);
    }

    private boolean laHoaDonOnline(HoaDon hoaDon) {
        return hoaDon != null && Objects.equals(hoaDon.getLoaiHoaDon(), 1);
    }

    private void truTonKhoKhiXacNhanDonOnline(HoaDon hoaDon) {
        List<ChiTietHoaDon> chiTietHoaDons = layChiTietHoaDonDeCapNhatKho(hoaDon);
        for (ChiTietHoaDon chiTietHoaDon : chiTietHoaDons) {
            ChiTietSanPham bienThe = chiTietHoaDon.getChiTietSanPham();
            if (bienThe == null || bienThe.getId() == null) {
                throw new RuntimeException("Không tìm thấy biến thể sản phẩm trong hóa đơn.");
            }

            int soLuongDat = chiTietHoaDon.getSoLuong() == null ? 0 : chiTietHoaDon.getSoLuong();
            int tonKhoHienTai = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            if (soLuongDat <= 0) {
                throw new RuntimeException("Số lượng sản phẩm trong hóa đơn không hợp lệ.");
            }
            if (tonKhoHienTai < soLuongDat) {
                throw new RuntimeException("Sản phẩm " + layTenBienThe(bienThe) + " không đủ tồn kho để xác nhận đơn.");
            }

            bienThe.setSoLuong(tonKhoHienTai - soLuongDat);
            bienThe.setNgayCapNhat(Instant.now());
            chiTietSanPhamRepository.save(bienThe);
        }
    }

    private void hoanTonKhoKhiHuyDonOnlineDaXacNhan(HoaDon hoaDon) {
        List<ChiTietHoaDon> chiTietHoaDons = layChiTietHoaDonDeCapNhatKho(hoaDon);
        for (ChiTietHoaDon chiTietHoaDon : chiTietHoaDons) {
            ChiTietSanPham bienThe = chiTietHoaDon.getChiTietSanPham();
            if (bienThe == null || bienThe.getId() == null) {
                continue;
            }

            int soLuongDat = chiTietHoaDon.getSoLuong() == null ? 0 : chiTietHoaDon.getSoLuong();
            int tonKhoHienTai = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            bienThe.setSoLuong(tonKhoHienTai + Math.max(soLuongDat, 0));
            bienThe.setNgayCapNhat(Instant.now());
            chiTietSanPhamRepository.save(bienThe);
        }
    }

    private List<ChiTietHoaDon> layChiTietHoaDonDeCapNhatKho(HoaDon hoaDon) {
        if (hoaDon == null || hoaDon.getId() == null) {
            return List.of();
        }
        return chiTietHoaDonRepository.findByHoaDonIdWithDetail(hoaDon.getId());
    }

    private String layTenBienThe(ChiTietSanPham bienThe) {
        if (bienThe == null || bienThe.getIdSanPham() == null) {
            return "không xác định";
        }
        return bienThe.getIdSanPham().getTenSanPham();
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
    @Transactional(readOnly = true)
    public List<ChiTietHoaDonResponse> getChiTietHoaDon(Integer idHoaDon) {

        return chiTietHoaDonRepository.findByHoaDonIdWithDetail(idHoaDon)
                .stream()
                .map(ct -> {

                    String anh = null;

                    if (ct.getChiTietSanPham().getHinhAnhs() != null
                            && !ct.getChiTietSanPham().getHinhAnhs().isEmpty()) {

                        // Ưu tiên ảnh chính
                        anh = ct.getChiTietSanPham()
                                .getHinhAnhs()
                                .stream()
                                .filter(h -> Boolean.TRUE.equals(h.getLaAnhChinh()))
                                .findFirst()
                                .orElse(ct.getChiTietSanPham().getHinhAnhs().get(0))
                                .getDuongDanAnh();
                    }

                    return new ChiTietHoaDonResponse(
                            ct.getId(),
                            ct.getChiTietSanPham().getMaCtsp(),
                            ct.getChiTietSanPham().getIdSanPham().getTenSanPham(),
                            ct.getChiTietSanPham().getIdMauSac().getTenMauSac(),
                            ct.getChiTietSanPham().getIdTrongLuong().getTenTrongLuong(),
                            ct.getSoLuong(),
                            ct.getDonGia(),
                            ct.getThanhTien(),
                            anh,
                            ct.getChiTietSanPham().getGiaBan()   // ✅ giá gốc hiện tại
                    );
                })
                .collect(Collectors.toList());
    }
    @Override
    public List<LichSuThanhToanResponse> getLichSuThanhToan(Integer idHoaDon) {
        return lichSuThanhToanRepository.findByHoaDon_IdOrderByNgayThanhToanDesc(idHoaDon)
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
