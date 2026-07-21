package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.dto.ChiTietEmailDTO;
import com.example.AerionSports_BE.dto.request.ThongTinDatHangOnlineRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChiTietSanPhamOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChuBanHangOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangDanhSachSanPhamOnline;
import com.example.AerionSports_BE.dto.view.DuLieuGioHangOnline;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BanHangOnlineServiceImpl implements BanHangOnlineService {

    // Moi trang danh sach san pham chi render toi da 12 item.
    private static final int PAGE_SIZE = 12;

    private final ChiTietSanPhamService chiTietSanPhamService;
    private final SanPhamService sanPhamService;
    private final ThuongHieuService thuongHieuService;
    // Nhom repository rieng cho ban hang online de tranh va cham voi phan dung chung.
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
        /*
         * Luong du lieu cho trang chu rat don gian:
         * - Lay danh sach san pham dang mo ban online.
         * - Cat 8 item dau lam "san pham noi bat".
         * - Lay item dau tien lam san pham hero neu giao dien can.
         */
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
            String brand,
            String weight,
            String price,
            String sort,
            int page
    ) {
        /*
         * Day la "trai tim" cua trang danh sach san pham.
         *
         * Dau vao:
         * - cac gia tri nguoi dung gui len qua query param
         *
         * Xu ly:
         * - lay du lieu that tu service san pham/chi tiet san pham
         * - loc theo keyword, thuong hieu, trong luong, gia
         * - sap xep theo lua chon
         * - cat danh sach theo trang hien tai
         *
         * Dau ra:
         * - 1 object tong hop chua danh sach san pham
         * - kem cac bo loc va trang thai dang chon de Thymeleaf render lai
         */
        List<SanPhamBanHangOnlineView> tatCaSanPham = layDanhSachSanPhamDangBan();
        List<SanPhamBanHangOnlineView> sanPhamSauLoc = new ArrayList<>(tatCaSanPham);

        String tuKhoaDaChuanHoa = chuanHoaChuoi(keyword);
        String thuongHieuDaChuanHoa = chuanHoaChuoi(brand);
        String trongLuongDaChuanHoa = chuanHoaChuoi(weight);
        String khoangGiaDaChuanHoa = chuanHoaChuoi(price);

        if (StringUtils.hasText(tuKhoaDaChuanHoa)) {
            String tuKhoaChuThuong = tuKhoaDaChuanHoa.toLowerCase();
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> chuaTuKhoaKhongPhanBietHoaThuong(item.getName(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getProductCode(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getBrand(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getColor(), tuKhoaChuThuong)
                            || chuaTuKhoaKhongPhanBietHoaThuong(item.getWeight(), tuKhoaChuThuong))
                    .toList();
        }

        if (StringUtils.hasText(thuongHieuDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> thuongHieuDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getBrand())))
                    .toList();
        }

        if (StringUtils.hasText(trongLuongDaChuanHoa)) {
            sanPhamSauLoc = sanPhamSauLoc.stream()
                    .filter(item -> trongLuongDaChuanHoa.equalsIgnoreCase(chuanHoaChuoi(item.getWeight())))
                    .toList();
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
                taoTuyChonTrongLuong(tatCaSanPham),
                tuKhoaDaChuanHoa,
                thuongHieuDaChuanHoa,
                trongLuongDaChuanHoa,
                khoangGiaDaChuanHoa,
                sort,
                trangAnToan,
                tongSoTrang,
                sanPhamSauLoc.size()
        );
    }

    @Override
    public DuLieuTrangChiTietSanPhamOnline layDuLieuChiTietSanPham(Integer productId, Integer variantId) {
        /*
         * Trang chi tiet khong chi can 1 san pham.
         * No can ca:
         * - san pham cha
         * - danh sach bien the de doi mau/trong luong
         * - bien the dang duoc chon de hien gia/anh/ton kho
         * - nhom san pham lien quan cung thuong hieu
         */
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

        KhachHang khachHang = taoHoacCapNhatKhachHang(thongTinDatHang);
        luuDiaChiMacDinh(khachHang, thongTinDatHang);

        BigDecimal tongTienHang = duLieuGioHang.getTongCong() == null ? BigDecimal.ZERO : duLieuGioHang.getTongCong();
        BigDecimal tienShip = phiVanChuyen == null ? BigDecimal.ZERO : phiVanChuyen;
        BigDecimal tienGiam = tienGiamVoucher == null ? BigDecimal.ZERO : tienGiamVoucher;
        BigDecimal tongTien = tongThanhToan == null ? tongTienHang.add(tienShip).subtract(tienGiam) : tongThanhToan;

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
        // Don online moi tao phai o trang thai "Cho xac nhan".
        hoaDon.setTrangThai(0);
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

        capNhatLuotSuDungPhieuGiamGiaOnline(phieuDangAp, khachHang);
        luuLichSuThanhToanChoDonOnline(hoaDonDaLuu, thongTinDatHang);

        NhanVien nhanVienMacDinh = nhanVienRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien mac dinh de luu lich su hoa don."));

        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setHoaDon(hoaDonDaLuu);
        lichSuHoaDon.setNhanVien(nhanVienMacDinh);
        // Vua dat xong thi don van dang cho shop xac nhan.
        lichSuHoaDon.setTrangThaiCu(0);
        lichSuHoaDon.setTrangThaiMoi(0);
        lichSuHoaDon.setHanhDong("Đặt hàng online");
        lichSuHoaDon.setGhiChu("Khách hàng đặt hàng từ giao diện bán hàng online.");
        lichSuHoaDon.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSuHoaDon);

        guiEmailDatHangThanhCong(khachHang, hoaDonDaLuu, duLieuGioHang, thongTinDatHang);

        return hoaDonDaLuu.getMaHoaDon();
    }

    private void luuLichSuThanhToanChoDonOnline(HoaDon hoaDon, ThongTinDatHangOnlineRequest thongTinDatHang) {
        if (hoaDon == null || hoaDon.getId() == null) {
            return;
        }

        LichSuThanhToan lichSuThanhToan = new LichSuThanhToan();
        lichSuThanhToan.setHoaDon(hoaDon);
        lichSuThanhToan.setSoTien(hoaDon.getTongTienThanhToan() == null ? BigDecimal.ZERO : hoaDon.getTongTienThanhToan());
        lichSuThanhToan.setPhuongThucThanhToan(
                hienThiPhuongThucThanhToan(thongTinDatHang == null ? null : thongTinDatHang.getPhuongThucThanhToan())
        );
        lichSuThanhToan.setTrangThaiThanhToan("Chưa thanh toán");
        lichSuThanhToan.setNgayThanhToan(LocalDateTime.now());
        lichSuThanhToan.setGhiChu("Đơn online mới tạo, chờ giao hàng để ghi nhận thanh toán.");
        lichSuThanhToanRepository.save(lichSuThanhToan);
    }

    private void capNhatLuotSuDungPhieuGiamGiaOnline(PhieuGiamGia phieuDangAp, KhachHang khachHang) {
        if (phieuDangAp == null || phieuDangAp.getId() == null) {
            return;
        }

        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(phieuDangAp.getId())
                .orElseThrow(() -> new RuntimeException("Khong tim thay phieu giam gia dang ap dung."));

        int soLuongDaDung = phieu.getSoLuongDaSuDung() == null ? 0 : phieu.getSoLuongDaSuDung();
        Integer soLuongToiDa = phieu.getSoLuong();
        if (soLuongToiDa != null && soLuongDaDung >= soLuongToiDa) {
            phieu.setTrangThai(0);
            phieu.setNgayCapNhat(LocalDateTime.now());
            phieuGiamGiaRepository.save(phieu);
            throw new RuntimeException("Ma giam gia da het luot su dung.");
        }

        int soLuongMoi = soLuongDaDung + 1;
        phieu.setSoLuongDaSuDung(soLuongMoi);
        if (soLuongToiDa != null && soLuongMoi >= soLuongToiDa) {
            phieu.setTrangThai(0);
        }
        phieu.setNgayCapNhat(LocalDateTime.now());
        phieuGiamGiaRepository.save(phieu);

        if (khachHang == null || khachHang.getId() == null) {
            return;
        }

        phieuGiamGiaKhachHangRepository
                .findChuaSuDung(phieu.getId(), khachHang.getId())
                .ifPresent(phieuKhachHang -> {
                    LocalDateTime now = LocalDateTime.now();
                    phieuKhachHang.setDaSuDung(true);
                    phieuKhachHang.setDaSuDungNgay(now);
                    phieuKhachHang.setNgaySuDung(now);
                    phieuGiamGiaKhachHangRepository.save(phieuKhachHang);
                });
    }

    private List<SanPhamBanHangOnlineView> layDanhSachSanPhamDangBan() {
        /*
         * Day la diem "noi du lieu that vao giao dien":
         * - Doc san pham/chi tiet san pham tu service hien co cua he thong.
         * - Chuyen du lieu nghiep vu sang DTO danh rieng cho view online.
         * - Sap item moi hon len truoc de trang listing co thu tu hop ly.
         */
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
        /*
         * Trang listing moi card chi can 1 object de render.
         * Trong khi du lieu that co the gom:
         * - 1 san pham cha
         * - nhieu bien the con
         *
         * Nen o day ta chon 1 bien the dai dien cho card.
         * Neu san pham chua co bien the thi van tao 1 object toi thieu
         * de giao dien khong bi vo.
         */
        SanPhamBanHangOnlineView bienTheDaiDien = chonBienThe(chuyenDanhSachBienThe(product), null);
        return bienTheDaiDien == null
                ? new SanPhamBanHangOnlineView(
                product.getId(),
                null,
                product.getMaSanPham(),
                product.getTenSanPham(),
                product.getTenThuongHieu(),
                product.getTenXuatXu(),
                null,
                null,
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
        // Tach toan bo bien the con cua 1 san pham cha thanh cac object de render chi tiet.
        if (product == null || product.getChiTietSanPhams() == null) {
            return List.of();
        }

        return product.getChiTietSanPhams().stream()
                .filter(Objects::nonNull)
                .map(variant -> chuyenBienTheThanhView(product, variant))
                .toList();
    }

    private SanPhamBanHangOnlineView chuyenBienTheThanhView(SanPhamResponse product, ChiTietSanPhamResponse variant) {
        /*
         * Day la buoc "be phang" du lieu:
         * - thong tin chung lay tu san pham cha
         * - thong tin mau/gia/anh/ton kho lay tu bien the
         * - sau cung dong goi thanh 1 DTO duy nhat cho template dung
         *
         * Gia hien thi uu tien gia da giam.
         * Neu chua co giam gia thi gia hien thi chinh la gia goc.
         */
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
                product.getTenThuongHieu(),
                product.getTenXuatXu(),
                variant.getTenMauSac(),
                variant.getTenTrongLuong(),
                chuanHoaDuongDanAnh(variant.getHinhAnh()),
                product.getMoTa(),
                salePrice,
                originalPrice,
                discountPercent,
                variant.getSoLuong()
        );
    }

    private SanPhamBanHangOnlineView chonBienThe(List<SanPhamBanHangOnlineView> variants, Integer variantId) {
        /*
         * Logic chon bien the:
         * - Neu URL co variantId thi tim dung bien the do.
         * - Neu khong co thi lay bien the dau tien lam mac dinh.
         *
         * Nho vay trang chi tiet luc nao cung co 1 bien the hien hanh
         * de hien gia, anh va ton kho.
         */
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

        return variants.get(0);
    }

    private List<TuyChonBoLocBanHangOnline> taoTuyChonThuongHieu(List<SanPhamBanHangOnlineView> products) {
        // Gom nhom theo thuong hieu va dem so luong de ve filter "Thuong hieu".
        Map<String, Long> counts = products.stream()
                .filter(item -> StringUtils.hasText(item.getBrand()))
                .collect(Collectors.groupingBy(SanPhamBanHangOnlineView::getBrand, LinkedHashMap::new, Collectors.counting()));

        return counts.entrySet().stream()
                .map(entry -> new TuyChonBoLocBanHangOnline(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<String> taoTuyChonTrongLuong(List<SanPhamBanHangOnlineView> products) {
        // Lay ra danh sach trong luong duy nhat de dua vao bo loc.
        return products.stream()
                .map(SanPhamBanHangOnlineView::getWeight)
                .filter(StringUtils::hasText)
                .distinct()
                .sorted()
                .toList();
    }

    private List<SanPhamBanHangOnlineView> sapXepDanhSachSanPham(List<SanPhamBanHangOnlineView> products, String sort) {
        /*
         * Quy uoc sap xep:
         * - newest: giu nguyen thu tu da sap o buoc lay du lieu
         * - price-asc: gia tang dan
         * - price-desc: gia giam dan
         */
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
        // Tra ve true neu gia san pham nam trong khoang gia ma nguoi dung vua chon.
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
        // Ho tro tim kiem mem deo hon, khong phan biet chu hoa/chu thuong.
        return StringUtils.hasText(text) && StringUtils.hasText(keyword) && text.toLowerCase().contains(keyword.toLowerCase());
    }

    private String chuanHoaChuoi(String text) {
        // Cat khoang trang du thua, neu rong thi doi ve chuoi rong cho de xu ly.
        return StringUtils.hasText(text) ? text.trim() : "";
    }

    private String chuanHoaDuongDanAnh(String path) {
        /*
         * Chuan hoa duong dan anh:
         * - rong -> fallback image
         * - http/https/duong dan bat dau bang / -> dung nguyen
         * - con lai -> coi nhu file trong uploads
         */
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
        Optional<KhachHang> lastCustomer = khachHangRepository.findFirstByOrderByIdDesc();
        int nextId = lastCustomer.map(khachHang -> khachHang.getId() + 1).orElse(1);
        return String.format("KH%03d", nextId);
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
                "Chờ xác nhận",
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
            case "BANK" -> "Chuyển khoản ngân hàng";
            case "COD" -> "Thanh toán khi nhận hàng";
            default -> StringUtils.hasText(giaTri) ? giaTri : "Thanh toán khi nhận hàng";
        };
    }
}
