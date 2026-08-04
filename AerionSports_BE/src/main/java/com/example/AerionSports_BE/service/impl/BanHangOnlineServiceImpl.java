package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.request.ThongTinDatHangOnlineRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.dto.view.DuLieuGioHangOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChiTietSanPhamOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChuBanHangOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangDanhSachSanPhamOnline;
import com.example.AerionSports_BE.dto.view.MucGioHangOnlineView;
import com.example.AerionSports_BE.dto.view.SanPhamBanHangOnlineView;
import com.example.AerionSports_BE.dto.view.TuyChonBoLocBanHangOnline;
import com.example.AerionSports_BE.entity.ChiTietHoaDon;
import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import com.example.AerionSports_BE.entity.HoaDon;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.LichSuHoaDon;
import com.example.AerionSports_BE.entity.LichSuThanhToan;
import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineChiTietHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineChiTietSanPhamRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineDiaChiKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuThanhToanRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineNhanVienRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaRepository;
import com.example.AerionSports_BE.service.BanHangOnlineService;
import com.example.AerionSports_BE.service.ChiTietSanPhamService;
import com.example.AerionSports_BE.service.EmailService;
import com.example.AerionSports_BE.service.SanPhamService;
import com.example.AerionSports_BE.service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BanHangOnlineServiceImpl implements BanHangOnlineService {

    private static final int PAGE_SIZE = 12;
    private static final int TRANG_THAI_CHO_THANH_TOAN_VNPAY = 8;
    private static final String TT_CHO_THANH_TOAN = "Chờ thanh toán";
    private static final String TT_CHUA_THANH_TOAN = "Chưa thanh toán";

    private final ChiTietSanPhamService chiTietSanPhamService;
    private final SanPhamService sanPhamService;
    private final ThuongHieuService thuongHieuService;
    private final BanHangOnlineKhachHangRepository khachHangRepository;
    private final BanHangOnlineDiaChiKhachHangRepository diaChiKhachHangRepository;
    private final BanHangOnlineHoaDonRepository hoaDonRepository;
    private final BanHangOnlineChiTietHoaDonRepository chiTietHoaDonRepository;
    private final BanHangOnlineChiTietSanPhamRepository chiTietSanPhamRepository;
    private final BanHangOnlineLichSuHoaDonRepository lichSuHoaDonRepository;
    private final BanHangOnlineLichSuThanhToanRepository lichSuThanhToanRepository;
    private final BanHangOnlineNhanVienRepository nhanVienRepository;
    private final BanHangOnlinePhieuGiamGiaRepository phieuGiamGiaRepository;
    private final BanHangOnlinePhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final EmailService emailService;

    @Override
    public DuLieuTrangChuBanHangOnline layDuLieuTrangChu() {
        List<SanPhamBanHangOnlineView> tatCaSanPham = layDanhSachSanPhamDangBan();
        List<SanPhamBanHangOnlineView> sanPhamNoiBat = tatCaSanPham.stream()
                .limit(8)
                .toList();

        return new DuLieuTrangChuBanHangOnline(
                sanPhamNoiBat,
                sanPhamNoiBat.isEmpty() ? null : sanPhamNoiBat.get(0),
                thuongHieuService.getAll().size(),
                tatCaSanPham.size()
        );
    }

    @Override
    public DuLieuTrangDanhSachSanPhamOnline layDuLieuDanhSachSanPham(
            String keyword,
            String category,
            String brand,
            String color,
            String weight,
            String origin,
            String stiffness,
            String balancePoint,
            String gripSize,
            String shaftMaterial,
            String frameMaterial,
            Long minPrice,
            Long maxPrice,
            String status,
            String price,
            String sort,
            int page
    ) {
        List<SanPhamBanHangOnlineView> tatCaSanPham = layDanhSachSanPhamDangBan();
        List<SanPhamBanHangOnlineView> sanPhamSauLoc = new ArrayList<>(tatCaSanPham);

        String tuKhoaDaChuanHoa = chuanHoaChuoi(keyword);
        String loaiSanPhamDaChuanHoa = chuanHoaChuoi(category);
        String thuongHieuDaChuanHoa = chuanHoaChuoi(brand);
        String mauSacDaChuanHoa = chuanHoaChuoi(color);
        String trongLuongDaChuanHoa = chuanHoaChuoi(weight);
        String xuatXuDaChuanHoa = chuanHoaChuoi(origin);
        String doCungDaChuanHoa = chuanHoaChuoi(stiffness);
        String diemCanBangDaChuanHoa = chuanHoaChuoi(balancePoint);
        String chuViCanDaChuanHoa = chuanHoaChuoi(gripSize);
        String chatLieuThanDaChuanHoa = chuanHoaChuoi(shaftMaterial);
        String chatLieuKhungDaChuanHoa = chuanHoaChuoi(frameMaterial);
        String tinhTrangDaChuanHoa = chuanHoaChuoi(status);
        String khoangGiaDaChuanHoa = chuanHoaChuoi(price);

        if (StringUtils.hasText(tuKhoaDaChuanHoa)) {
            String tuKhoaChuThuong = tuKhoaDaChuanHoa.toLowerCase();
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> chuaTuKhoaKhongPhanBietHoaThuong(item.getName(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getProductCode(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getCategory(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getBrand(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getColor(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getWeight(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getOrigin(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getStiffness(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getBalancePoint(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getGripSize(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getShaftMaterial(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getFrameMaterial(), tuKhoaChuThuong))
                    .toList();
        }

        if (StringUtils.hasText(loaiSanPhamDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> loaiSanPhamDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getCategory())))
                    .toList();
        }

        if (StringUtils.hasText(thuongHieuDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> thuongHieuDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getBrand())))
                    .toList();
        }

        if (StringUtils.hasText(mauSacDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> mauSacDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getColor())))
                    .toList();
        }

        if (StringUtils.hasText(trongLuongDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> trongLuongDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getWeight())))
                    .toList();
        }

        if (StringUtils.hasText(xuatXuDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> xuatXuDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getOrigin())))
                    .toList();
        }

        if (StringUtils.hasText(doCungDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> doCungDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getStiffness())))
                    .toList();
        }

        if (StringUtils.hasText(diemCanBangDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> diemCanBangDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getBalancePoint())))
                    .toList();
        }

        if (StringUtils.hasText(chuViCanDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> chuViCanDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getGripSize())))
                    .toList();
        }

        if (StringUtils.hasText(chatLieuThanDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> chatLieuThanDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getShaftMaterial())))
                    .toList();
        }

        if (StringUtils.hasText(chatLieuKhungDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> chatLieuKhungDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getFrameMaterial())))
                    .toList();
        }

        if (minPrice != null) {
            BigDecimal minVal = BigDecimal.valueOf(minPrice);
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> item.getPrice() != null && item.getPrice().compareTo(minVal) >= 0)
                    .toList();
        }

        if (maxPrice != null) {
            BigDecimal maxVal = BigDecimal.valueOf(maxPrice);
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> item.getPrice() != null && item.getPrice().compareTo(maxVal) <= 0)
                    .toList();
        }

        if (StringUtils.hasText(tinhTrangDaChuanHoa)) {
            if ("con-hang".equalsIgnoreCase(tinhTrangDaChuanHoa) || "in-stock".equalsIgnoreCase(tinhTrangDaChuanHoa)) {
                sanPhamSauLoc = sanPhamSauLoc.stream()
                        .filter(item -> item.getStock() != null && item.getStock() > 0)
                        .toList();
            } else if ("het-hang".equalsIgnoreCase(tinhTrangDaChuanHoa) || "out-of-stock".equalsIgnoreCase(tinhTrangDaChuanHoa)) {
                sanPhamSauLoc = sanPhamSauLoc.stream()
                        .filter(item -> item.getStock() == null || item.getStock() <= 0)
                        .toList();
            }
        }

        if (StringUtils.hasText(khoangGiaDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> khopKhoangGia(item.getPrice(), khoangGiaDaChuanHoa))
                    .toList();
        }

        sanPhamSauLoc = sapXepDanhSachSanPham(sanPhamSauLoc, sort);

        int trangAnToan = Math.max(page, 0);
        int tongSoTrang = Math.max(1, (int) Math.ceil(sanPhamSauLoc.size() / (double) PAGE_SIZE));
        if (trangAnToan >= tongSoTrang) {
            trangAnToan = tongSoTrang - 1;
        }

        int viTriBatDau = Math.min(trangAnToan * PAGE_SIZE, sanPhamSauLoc.size());
        int viTriKetThuc = Math.min(viTriBatDau + PAGE_SIZE, sanPhamSauLoc.size());
        List<SanPhamBanHangOnlineView> sanPhamTrangHienTai = sanPhamSauLoc.subList(viTriBatDau, viTriKetThuc);

        return new DuLieuTrangDanhSachSanPhamOnline(
                sanPhamTrangHienTai,
                taoTuyChonThuongHieu(tatCaSanPham),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getCategory),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getColor),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getWeight),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getOrigin),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getStiffness),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getBalancePoint),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getGripSize),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getShaftMaterial),
                taoTuyChonTruong(tatCaSanPham, SanPhamBanHangOnlineView::getFrameMaterial),
                tuKhoaDaChuanHoa,
                loaiSanPhamDaChuanHoa,
                thuongHieuDaChuanHoa,
                mauSacDaChuanHoa,
                trongLuongDaChuanHoa,
                xuatXuDaChuanHoa,
                doCungDaChuanHoa,
                diemCanBangDaChuanHoa,
                chuViCanDaChuanHoa,
                chatLieuThanDaChuanHoa,
                chatLieuKhungDaChuanHoa,
                minPrice,
                maxPrice,
                tinhTrangDaChuanHoa,
                khoangGiaDaChuanHoa,
                sort,
                trangAnToan,
                tongSoTrang,
                sanPhamSauLoc.size()
        );
    }

    @Override
    public DuLieuTrangChiTietSanPhamOnline layDuLieuChiTietSanPham(Integer productId, Integer variantId) {
        SanPhamResponse sanPhamCha = sanPhamService.findById(productId);
        List<SanPhamBanHangOnlineView> danhSachBienThe = chuyenDanhSachBienThe(sanPhamCha);
        SanPhamBanHangOnlineView bienTheDangChon = chonBienThe(danhSachBienThe, variantId);

        List<SanPhamBanHangOnlineView> sanPhamLienQuan = layDanhSachSanPhamDangBan().stream()
                .filter(item -> !Objects.equals(item.getProductId(), productId))
                .filter(item -> Objects.equals(chuanHoaChuoi(item.getBrand()), chuanHoaChuoi(sanPhamCha.getTenThuongHieu())))
                .limit(4)
                .toList();

        return new DuLieuTrangChiTietSanPhamOnline(
                sanPhamCha,
                danhSachBienThe,
                bienTheDangChon,
                sanPhamLienQuan
        );
    }

    @Override
    public SanPhamBanHangOnlineView layBienTheSanPham(Integer productId, Integer variantId) {
        if (productId == null) {
            return null;
        }

        SanPhamResponse sanPhamCha = sanPhamService.findById(productId);
        List<SanPhamBanHangOnlineView> danhSachBienThe = chuyenDanhSachBienThe(sanPhamCha);
        return chonBienThe(danhSachBienThe, variantId);
    }

    @Override
    @Transactional
    public String datHangOnline(
            ThongTinDatHangOnlineRequest thongTinDatHang,
            DuLieuGioHangOnline duLieuGioHang,
            PhieuGiamGia phieuDangAp,
            BigDecimal phiVanChuyen,
            BigDecimal tienGiamVoucher,
            BigDecimal tongThanhToan
    ) {
        kiemTraThongTinDatHang(thongTinDatHang, duLieuGioHang);
        kiemTraTonKhoDatHang(duLieuGioHang);

        KhachHang khachHang = taoHoacCapNhatKhachHang(thongTinDatHang);
        luuDiaChiMacDinh(khachHang, thongTinDatHang);

        BigDecimal tongTienHang = duLieuGioHang.getTongCong() == null ? BigDecimal.ZERO : duLieuGioHang.getTongCong();
        BigDecimal tienShip = phiVanChuyen == null ? BigDecimal.ZERO : phiVanChuyen;
        BigDecimal tienGiam = tienGiamVoucher == null ? BigDecimal.ZERO : tienGiamVoucher;
        BigDecimal tongTien = tongThanhToan == null ? tongTienHang.add(tienShip).subtract(tienGiam) : tongThanhToan;

        boolean thanhToanVnPay = laThanhToanVnPay(thongTinDatHang.getPhuongThucThanhToan());

        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHang(khachHang);
        hoaDon.setPhieuGiamGia(phieuDangAp);
        hoaDon.setMaHoaDon("HD" + System.currentTimeMillis());
        hoaDon.setLoaiHoaDon(1);
        hoaDon.setTenNguoiNhan(chuanHoaText(thongTinDatHang.getHoTen()));
        hoaDon.setSdtNguoiNhan(chuanHoaText(thongTinDatHang.getSoDienThoai()));
        hoaDon.setDiaChiNhan(gopDiaChiDayDu(thongTinDatHang));
        hoaDon.setGhiChu(chuanHoaText(thongTinDatHang.getGhiChu()));
        hoaDon.setTongTienHang(tongTienHang);
        hoaDon.setTienGiam(tienGiam);
        hoaDon.setTienVanChuyen(tienShip);
        hoaDon.setTongTienThanhToan(tongTien.max(BigDecimal.ZERO));
        hoaDon.setNgayTao(LocalDateTime.now());
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat("Khách hàng online");
        hoaDon.setTrangThai(thanhToanVnPay ? TRANG_THAI_CHO_THANH_TOAN_VNPAY : 0);
        HoaDon hoaDonDaLuu = hoaDonRepository.save(hoaDon);

        int soThuTuChiTiet = 1;
        for (MucGioHangOnlineView item : duLieuGioHang.getItems()) {
            ChiTietSanPham bienThe = chiTietSanPhamRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay bien the san pham #" + item.getVariantId()));

            if (!Objects.equals(bienThe.getIdSanPham().getId(), item.getProductId())) {
                throw new RuntimeException("Du lieu bien the san pham khong hop le.");
            }

            int tonKho = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            if (item.getSoLuong() <= 0) {
                throw new RuntimeException("So luong san pham khong hop le.");
            }
            if (tonKho < item.getSoLuong()) {
                throw new RuntimeException("San pham " + item.getTenSanPham() + " khong du ton kho de dat hang.");
            }

            ChiTietHoaDon chiTietHoaDon = new ChiTietHoaDon();
            chiTietHoaDon.setHoaDon(hoaDonDaLuu);
            chiTietHoaDon.setChiTietSanPham(bienThe);
            chiTietHoaDon.setMaHoaDonChiTiet(hoaDonDaLuu.getMaHoaDon() + "-CT" + soThuTuChiTiet++);
            chiTietHoaDon.setSoLuong(item.getSoLuong());
            chiTietHoaDon.setDonGia(item.getDonGia() == null ? BigDecimal.ZERO : item.getDonGia());
            chiTietHoaDon.setThanhTien(item.getThanhTien() == null ? BigDecimal.ZERO : item.getThanhTien());
            chiTietHoaDon.setNgayTao(LocalDateTime.now());
            chiTietHoaDon.setNgayCapNhat(LocalDateTime.now());
            chiTietHoaDon.setNguoiCapNhat("Khách hàng online");
            chiTietHoaDon.setTrangThai(1);
            chiTietHoaDonRepository.save(chiTietHoaDon);

        }

        NhanVien nhanVienMacDinh = nhanVienRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien mac dinh de luu lich su hoa don."));

        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setHoaDon(hoaDonDaLuu);
        lichSuHoaDon.setNhanVien(nhanVienMacDinh);
        lichSuHoaDon.setTrangThaiCu(hoaDonDaLuu.getTrangThai());
        lichSuHoaDon.setTrangThaiMoi(hoaDonDaLuu.getTrangThai());
        lichSuHoaDon.setHanhDong("Đặt hàng online");
        lichSuHoaDon.setGhiChu("Khách hàng đặt hàng từ giao diện bán hàng online.");
        lichSuHoaDon.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSuHoaDon);

        taoLichSuThanhToan(hoaDonDaLuu, thongTinDatHang, thanhToanVnPay);
        if (!thanhToanVnPay) {
            capNhatLuotSuDungPhieuGiamGia(phieuDangAp, khachHang);
        }

        guiEmailDatHangThanhCong(khachHang, hoaDonDaLuu, duLieuGioHang, thongTinDatHang);

        return hoaDonDaLuu.getMaHoaDon();
    }

    private void kiemTraTonKhoDatHang(DuLieuGioHangOnline duLieuGioHang) {
        for (MucGioHangOnlineView item : duLieuGioHang.getItems()) {
            ChiTietSanPham bienThe = chiTietSanPhamRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Khong tim thay bien the san pham #" + item.getVariantId()));
            if (!Objects.equals(bienThe.getIdSanPham().getId(), item.getProductId())) {
                throw new RuntimeException("Du lieu bien the san pham khong hop le.");
            }
            int tonKho = bienThe.getSoLuong() == null ? 0 : bienThe.getSoLuong();
            if (item.getSoLuong() <= 0) {
                throw new RuntimeException("So luong san pham khong hop le.");
            }
            if (tonKho < item.getSoLuong()) {
                throw new RuntimeException("San pham " + item.getTenSanPham() + " khong du ton kho de dat hang.");
            }
        }
    }

    private void taoLichSuThanhToan(HoaDon hoaDon, ThongTinDatHangOnlineRequest thongTinDatHang, boolean thanhToanVnPay) {
        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setSoTien(hoaDon.getTongTienThanhToan());
        lichSuThanhToan.setPhuongThucThanhToan(thanhToanVnPay ? "VNPay" : "Thanh toán khi nhận hàng");
        lichSuThanhToan.setTrangThaiThanhToan(thanhToanVnPay ? TT_CHO_THANH_TOAN : TT_CHUA_THANH_TOAN);
        lichSuThanhToan.setNgayThanhToan(LocalDateTime.now());
        lichSuThanhToan.setGhiChu(thanhToanVnPay ? "Đơn VNPay chờ khách thanh toán." : "Đơn COD chưa thanh toán.");
        lichSuThanhToanRepository.save(lichSuThanhToan);
    }

    private boolean laThanhToanVnPay(String phuongThucThanhToan) {
        String giaTri = chuanHoaText(phuongThucThanhToan);
        return "VNPAY".equalsIgnoreCase(giaTri) || "BANK".equalsIgnoreCase(giaTri);
    }

    private void capNhatLuotSuDungPhieuGiamGia(PhieuGiamGia phieuDangAp, KhachHang khachHang) {
        if (phieuDangAp == null || phieuDangAp.getId() == null) {
            return;
        }

        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(phieuDangAp.getId()).orElse(null);
        if (phieu == null) {
            return;
        }

        int daSuDung = phieu.getSoLuongDaSuDung() == null ? 0 : phieu.getSoLuongDaSuDung();
        phieu.setSoLuongDaSuDung(daSuDung + 1);
        phieu.setNgayCapNhat(LocalDateTime.now());
        phieuGiamGiaRepository.save(phieu);

        if (khachHang == null || khachHang.getId() == null) {
            return;
        }

        phieuGiamGiaKhachHangRepository.findChuaSuDung(phieu.getId(), khachHang.getId())
                .ifPresent(phieuKhachHang -> {
                    LocalDateTime now = LocalDateTime.now();
                    phieuKhachHang.setDaSuDung(true);
                    phieuKhachHang.setDaSuDungNgay(now);
                    phieuKhachHang.setNgaySuDung(now);
                    phieuGiamGiaKhachHangRepository.save(phieuKhachHang);
                });
    }

    private List<SanPhamBanHangOnlineView> layDanhSachSanPhamDangBan() {
        return chiTietSanPhamService.getAllProductsWithVariantsForCheck().stream()
                .map(this::chuyenSanPhamChaThanhView)
                .sorted((left, right) -> {
                    Integer leftId = left == null ? null : left.getProductId();
                    Integer rightId = right == null ? null : right.getProductId();

                    if (leftId == null && rightId == null) return 0;
                    if (leftId == null) return 1;
                    if (rightId == null) return -1;
                    return rightId.compareTo(leftId);
                })
                .toList();
    }

    private SanPhamBanHangOnlineView chuyenSanPhamChaThanhView(SanPhamResponse product) {
        SanPhamBanHangOnlineView bienTheDaiDien = chonBienThe(chuyenDanhSachBienThe(product), null);
        return bienTheDaiDien == null
                ? new SanPhamBanHangOnlineView(
                product.getId(),
                null,
                product.getMaSanPham(),
                product.getTenSanPham(),
                product.getTenDanhMuc(),
                product.getTenThuongHieu(),
                product.getTenXuatXu(),
                null,
                null,
                product.getTenDoCung(),
                product.getTenDiemCanBang(),
                product.getTenChuViCanVot(),
                product.getTenChatLieuThanVot(),
                product.getTenChatLieuKhungVot(),
                null,
                product.getMoTa(),
                null,
                null,
                null,
                null
        )
                : bienTheDaiDien;
    }

    private List<SanPhamBanHangOnlineView> chuyenDanhSachBienThe(SanPhamResponse product) {
        if (product == null || product.getChiTietSanPhams() == null) {
            return List.of();
        }

        return product.getChiTietSanPhams().stream()
                .filter(Objects::nonNull)
                .map(variant -> chuyenBienTheThanhView(product, variant))
                .toList();
    }

    private SanPhamBanHangOnlineView chuyenBienTheThanhView(SanPhamResponse product, ChiTietSanPhamResponse variant) {
        BigDecimal originalPrice = variant.getGiaBan();
        BigDecimal salePrice = variant.getGiaDaGiam();
        if (salePrice == null) {
            salePrice = originalPrice;
        }
        BigDecimal discountPercent = variant.getPhanTramGiam() == null ? BigDecimal.ZERO : variant.getPhanTramGiam();

        return new SanPhamBanHangOnlineView(
                product.getId(),
                variant.getId(),
                product.getMaSanPham(),
                product.getTenSanPham(),
                product.getTenDanhMuc(),
                product.getTenThuongHieu(),
                product.getTenXuatXu(),
                variant.getTenMauSac(),
                variant.getTenTrongLuong(),
                product.getTenDoCung(),
                product.getTenDiemCanBang(),
                product.getTenChuViCanVot(),
                product.getTenChatLieuThanVot(),
                product.getTenChatLieuKhungVot(),
                chuanHoaDuongDanAnh(variant.getHinhAnh()),
                product.getMoTa(),
                salePrice,
                originalPrice,
                discountPercent,
                variant.getSoLuong()
        );
    }

    private SanPhamBanHangOnlineView chonBienThe(List<SanPhamBanHangOnlineView> variants, Integer variantId) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }

        if (variantId != null) {
            for (SanPhamBanHangOnlineView variant : variants) {
                if (Objects.equals(variant.getVariantId(), variantId)) {
                    return variant;
                }
            }
        }

        return variants.stream()
                .filter(variant -> variant.getStock() != null && variant.getStock() > 0)
                .findFirst()
                .orElse(variants.get(0));
    }

    private List<TuyChonBoLocBanHangOnline> taoTuyChonThuongHieu(List<SanPhamBanHangOnlineView> products) {
        Map<String, Long> counts = products.stream()
                .filter(item -> StringUtils.hasText(item.getBrand()))
                .collect(Collectors.groupingBy(SanPhamBanHangOnlineView::getBrand, LinkedHashMap::new, Collectors.counting()));

        return counts.entrySet().stream()
                .map(entry -> new TuyChonBoLocBanHangOnline(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<String> taoTuyChonTruong(List<SanPhamBanHangOnlineView> products, Function<SanPhamBanHangOnlineView, String> extractor) {
        return products.stream()
                .map(extractor)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();
    }

    private List<SanPhamBanHangOnlineView> sapXepDanhSachSanPham(List<SanPhamBanHangOnlineView> products, String sort) {
        if (products == null || products.isEmpty()) {
            return List.of();
        }

        Comparator<SanPhamBanHangOnlineView> byPrice = Comparator.comparing(
                item -> item.getPrice() == null ? BigDecimal.ZERO : item.getPrice(),
                Comparator.nullsLast(BigDecimal::compareTo)
        );

        return switch (String.valueOf(sort)) {
            case "price-asc" -> products.stream().sorted(byPrice).toList();
            case "price-desc" -> products.stream().sorted(byPrice.reversed()).toList();
            default -> products;
        };
    }

    private boolean khopKhoangGia(BigDecimal price, String priceRange) {
        BigDecimal value = price == null ? BigDecimal.ZERO : price;
        return switch (priceRange) {
            case "under-1m" -> value.compareTo(BigDecimal.valueOf(1_000_000)) < 0;
            case "1-2m" -> value.compareTo(BigDecimal.valueOf(1_000_000)) >= 0
                    && value.compareTo(BigDecimal.valueOf(2_000_000)) <= 0;
            case "2-3m" -> value.compareTo(BigDecimal.valueOf(2_000_000)) > 0
                    && value.compareTo(BigDecimal.valueOf(3_000_000)) <= 0;
            case "over-3m" -> value.compareTo(BigDecimal.valueOf(3_000_000)) > 0;
            default -> true;
        };
    }

    private boolean chuaTuKhoaKhongPhanBietHoaThuong(String text, String keyword) {
        return StringUtils.hasText(text) && StringUtils.hasText(keyword) && text.toLowerCase().contains(keyword.toLowerCase());
    }

    private String chuanHoaChuoi(String text) {
        return StringUtils.hasText(text) ? text.trim() : "";
    }

    private String chuanHoaDuongDanAnh(String path) {
        if (!StringUtils.hasText(path)) {
            return "/images/image.png";
        }
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("/")) {
            return path;
        }
        return "/uploads/" + path;
    }

    private String chuanHoaText(String text) {
        return StringUtils.hasText(text) ? text.trim() : "";
    }

    private void kiemTraThongTinDatHang(ThongTinDatHangOnlineRequest thongTinDatHang, DuLieuGioHangOnline duLieuGioHang) {
        if (duLieuGioHang == null || duLieuGioHang.isEmpty()) {
            throw new RuntimeException("Gio hang dang trong, khong the dat hang.");
        }
        if (thongTinDatHang == null) {
            throw new RuntimeException("Thong tin dat hang khong hop le.");
        }
        if (!StringUtils.hasText(thongTinDatHang.getHoTen())) {
            throw new RuntimeException("Vui long nhap ho va ten.");
        }
        if (!StringUtils.hasText(thongTinDatHang.getSoDienThoai())) {
            throw new RuntimeException("Vui long nhap so dien thoai.");
        }
        if (!StringUtils.hasText(thongTinDatHang.getEmail())) {
            throw new RuntimeException("Vui long nhap email.");
        }
        if (!StringUtils.hasText(thongTinDatHang.getTinhThanh())
                || !StringUtils.hasText(thongTinDatHang.getQuanHuyen())
                || !StringUtils.hasText(thongTinDatHang.getPhuongXa())) {
            throw new RuntimeException("Vui long nhap day du dia chi giao hang.");
        }
    }

    private KhachHang taoHoacCapNhatKhachHang(ThongTinDatHangOnlineRequest thongTinDatHang) {
        String sdt = chuanHoaText(thongTinDatHang.getSoDienThoai());
        String email = chuanHoaText(thongTinDatHang.getEmail());

        Optional<KhachHang> theoSoDienThoai = StringUtils.hasText(sdt)
                ? khachHangRepository.findBySdt(sdt)
                : Optional.empty();
        Optional<KhachHang> theoEmail = StringUtils.hasText(email)
                ? khachHangRepository.findByEmail(email)
                : Optional.empty();

        if (theoSoDienThoai.isPresent() && theoEmail.isPresent()
                && !Objects.equals(theoSoDienThoai.get().getId(), theoEmail.get().getId())) {
            throw new RuntimeException("So dien thoai va email dang thuoc ve 2 khach hang khac nhau.");
        }

        KhachHang khachHang = theoSoDienThoai.orElseGet(() -> theoEmail.orElseGet(KhachHang::new));
        boolean laKhachHangMoi = khachHang.getId() == null;

        if (laKhachHangMoi) {
            khachHang.setMaKhachHang(taoMaKhachHangMoi());
            khachHang.setNgayTao(LocalDateTime.now());
            khachHang.setTrangThai(1);
            khachHang.setDiemTichLuy(0);
        }

        khachHang.setHoTen(chuanHoaText(thongTinDatHang.getHoTen()));
        khachHang.setSdt(sdt);
        khachHang.setEmail(email);
        khachHang.setNgayCapNhat(LocalDateTime.now());
        return khachHangRepository.save(khachHang);
    }

    private void luuDiaChiMacDinh(KhachHang khachHang, ThongTinDatHangOnlineRequest thongTinDatHang) {
        diaChiKhachHangRepository.boMacDinhTheoKhachHang(khachHang.getId());

        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setKhachHang(khachHang);
        diaChi.setNguoiNhan(chuanHoaText(thongTinDatHang.getHoTen()));
        diaChi.setSdt(chuanHoaText(thongTinDatHang.getSoDienThoai()));
        diaChi.setTinhThanh(chuanHoaText(thongTinDatHang.getTinhThanh()));
        diaChi.setPhuongXa(chuanHoaText(thongTinDatHang.getPhuongXa()));
        diaChi.setDiaChiChiTiet(gopDiaChiChiTiet(thongTinDatHang));
        diaChi.setMacDinh(true);
        diaChi.setNgayTao(LocalDateTime.now());
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiKhachHangRepository.save(diaChi);
    }

    private String taoMaKhachHangMoi() {
        int nextNumber = Optional.ofNullable(khachHangRepository.findMaxSoThuTuMaKhachHang()).orElse(0) + 1;
        String maKhachHang;
        do {
            maKhachHang = String.format("KH%03d", nextNumber++);
        } while (khachHangRepository.existsByMaKhachHang(maKhachHang));
        return maKhachHang;
    }

    private String gopDiaChiChiTiet(ThongTinDatHangOnlineRequest thongTinDatHang) {
        String diaChiChiTiet = chuanHoaText(thongTinDatHang.getDiaChiChiTiet());
        String quanHuyen = chuanHoaText(thongTinDatHang.getQuanHuyen());
        if (!StringUtils.hasText(diaChiChiTiet)) {
            return quanHuyen;
        }
        if (!StringUtils.hasText(quanHuyen)) {
            return diaChiChiTiet;
        }
        return diaChiChiTiet + ", " + quanHuyen;
    }

    private String gopDiaChiDayDu(ThongTinDatHangOnlineRequest thongTinDatHang) {
        List<String> phanDiaChi = new ArrayList<>();
        if (StringUtils.hasText(thongTinDatHang.getDiaChiChiTiet())) {
            phanDiaChi.add(chuanHoaText(thongTinDatHang.getDiaChiChiTiet()));
        }
        if (StringUtils.hasText(thongTinDatHang.getPhuongXa())) {
            phanDiaChi.add(chuanHoaText(thongTinDatHang.getPhuongXa()));
        }
        if (StringUtils.hasText(thongTinDatHang.getQuanHuyen())) {
            phanDiaChi.add(chuanHoaText(thongTinDatHang.getQuanHuyen()));
        }
        if (StringUtils.hasText(thongTinDatHang.getTinhThanh())) {
            phanDiaChi.add(chuanHoaText(thongTinDatHang.getTinhThanh()));
        }
        return String.join(", ", phanDiaChi);
    }

    private void guiEmailDatHangThanhCong(
            KhachHang khachHang,
            HoaDon hoaDon,
            DuLieuGioHangOnline duLieuGioHang,
            ThongTinDatHangOnlineRequest thongTinDatHang
    ) {
        if (khachHang == null || hoaDon == null || duLieuGioHang == null) {
            return;
        }

        String emailNhan = chuanHoaText(khachHang.getEmail());
        if (!StringUtils.hasText(emailNhan)) {
            return;
        }

        List<ChiTietEmailDTO> sanPhamEmail = duLieuGioHang.getItems().stream()
                .map(item -> new ChiTietEmailDTO(
                        item.getTenSanPham(),
                        item.getMauSac(),
                        item.getTrongLuong(),
                        item.getSoLuong(),
                        item.getDonGia(),
                        item.getThanhTien()
                ))
                .toList();

        emailService.sendOrderStatusEmail(
                emailNhan,
                chuanHoaText(khachHang.getHoTen()),
                hoaDon.getMaHoaDon(),
                laThanhToanVnPay(thongTinDatHang.getPhuongThucThanhToan()) ? "Chờ thanh toán" : "Chờ xác nhận",
                hoaDon.getDiaChiNhan(),
                hoaDon.getSdtNguoiNhan(),
                hoaDon.getTongTienHang(),
                hoaDon.getTienGiam(),
                hoaDon.getTienVanChuyen(),
                hoaDon.getTongTienThanhToan(),
                sanPhamEmail,
                hienThiPhuongThucThanhToan(thongTinDatHang.getPhuongThucThanhToan())
        );
    }

    private String hienThiPhuongThucThanhToan(String phuongThucThanhToan) {
        String giaTri = chuanHoaText(phuongThucThanhToan);
        return switch (giaTri.toUpperCase()) {
            case "BANK", "VNPAY" -> "VNPay";
            case "COD" -> "Thanh toán khi nhận hàng";
            default -> StringUtils.hasText(giaTri) ? giaTri : "Thanh toán khi nhận hàng";
        };
    }
}
