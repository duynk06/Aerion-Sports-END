package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.ThongTinDatHangOnlineRequest;
import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import com.example.AerionSports_BE.dto.response.HoaDonResponse;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChiTietSanPhamOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChuBanHangOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangDanhSachSanPhamOnline;
import com.example.AerionSports_BE.dto.view.DuLieuGioHangOnline;
import com.example.AerionSports_BE.dto.view.MucGioHangOnlineSession;
import com.example.AerionSports_BE.dto.view.MucGioHangOnlineView;
import com.example.AerionSports_BE.dto.view.SanPhamBanHangOnlineView;
import com.example.AerionSports_BE.entity.DiaChiKhachHang;
import com.example.AerionSports_BE.entity.HoaDon;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.LichSuHoaDon;
import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineChiTietHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineDiaChiKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineNhanVienRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineTaiKhoanRepository;
import com.example.AerionSports_BE.service.BanHangOnlineService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class BanHangOnlineController {

    private static final String GIO_HANG_ONLINE_SESSION_KEY = "gioHangOnline";
    private static final String PHIEU_GIAM_GIA_ONLINE_SESSION_KEY = "phieuGiamGiaOnlineId";
    private static final String THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY = "thongTinDatHangOnline";
    private static final String KHACH_HANG_ONLINE_ID_SESSION_KEY = "khachHangOnlineId";
    private static final String KHACH_HANG_ONLINE_TEN_SESSION_KEY = "khachHangOnlineTen";
    private static final String KHACH_HANG_ONLINE_EMAIL_SESSION_KEY = "khachHangOnlineEmail";
    private static final BigDecimal PHI_SHIP_NOI_TINH = BigDecimal.valueOf(22_000);
    private static final BigDecimal PHI_SHIP_NOI_MIEN = BigDecimal.valueOf(30_000);
    private static final BigDecimal PHI_SHIP_LIEN_MIEN = BigDecimal.valueOf(32_000);
    private static final String TINH_THANH_CUA_HANG = "ha noi";
    private static final List<String> CAC_TINH_MIEN_BAC = List.of(
            "ha noi",
            "hai phong",
            "quang ninh",
            "bac giang",
            "bac ninh",
            "hai duong",
            "hung yen",
            "vinh phuc",
            "phu tho",
            "thai nguyen",
            "bac kan",
            "cao bang",
            "lang son",
            "tuyen quang",
            "ha giang",
            "lao cai",
            "yen bai",
            "dien bien",
            "lai chau",
            "son la",
            "hoa binh",
            "ha nam",
            "nam dinh",
            "thai binh",
            "ninh binh"
    );
    private static final Map<Integer, List<MucGioHangOnlineSession>> GIO_HANG_ONLINE_THEO_KHACH = new ConcurrentHashMap<>();
    private static final Pattern EMAIL_HOP_LE = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern SDT_HOP_LE = Pattern.compile("^0\\d{9}$");

    /*
     * Controller nay chi lo phan "noi request voi giao dien":
     * - Browser goi vao URL nao thi controller nhan URL do.
     * - Controller khong tu viet logic loc/sap xep san pham.
     * - No chi goi service lay du lieu da duoc xu ly san.
     * - Sau do nhat du lieu vao Model de file HTML Thymeleaf render.
     */
    private final BanHangOnlineService banHangOnlineService;
    private final BanHangOnlinePhieuGiamGiaRepository phieuGiamGiaRepository;
    private final BanHangOnlinePhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final BanHangOnlineHoaDonRepository hoaDonRepository;
    private final BanHangOnlineChiTietHoaDonRepository chiTietHoaDonRepository;
    private final BanHangOnlineLichSuHoaDonRepository lichSuHoaDonRepository;
    private final BanHangOnlineKhachHangRepository khachHangRepository;
    private final BanHangOnlineDiaChiKhachHangRepository diaChiKhachHangRepository;
    private final BanHangOnlineNhanVienRepository nhanVienRepository;
    private final BanHangOnlineTaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping({"/", "/cua-hang"})
    public String trangChu(Model model) {
        DuLieuTrangChuBanHangOnline duLieuTrangChu = banHangOnlineService.layDuLieuTrangChu();

        model.addAttribute("pageTitle", "Aerion Sports | Trang chu");
        model.addAttribute("activePage", "home");
        model.addAttribute("featuredProducts", duLieuTrangChu.getFeaturedProducts());
        model.addAttribute("heroProduct", duLieuTrangChu.getHeroProduct());
        model.addAttribute("brandCount", duLieuTrangChu.getBrandCount());
        model.addAttribute("productCount", duLieuTrangChu.getProductCount());
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/trang-chu";
    }

    @GetMapping("/cua-hang/san-pham")
    public String danhSachSanPham(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "weight", required = false) String weight,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        DuLieuTrangDanhSachSanPhamOnline duLieuDanhSach = banHangOnlineService.layDuLieuDanhSachSanPham(
                keyword, brand, weight, price, sort, page
        );

        model.addAttribute("pageTitle", "Aerion Sports | San pham");
        model.addAttribute("activePage", "products");
        model.addAttribute("featuredProducts", duLieuDanhSach.getProducts());
        model.addAttribute("brandOptions", duLieuDanhSach.getBrandOptions());
        model.addAttribute("weightOptions", duLieuDanhSach.getWeightOptions());
        model.addAttribute("selectedKeyword", duLieuDanhSach.getSelectedKeyword());
        model.addAttribute("selectedBrand", duLieuDanhSach.getSelectedBrand());
        model.addAttribute("selectedWeight", duLieuDanhSach.getSelectedWeight());
        model.addAttribute("selectedPrice", duLieuDanhSach.getSelectedPrice());
        model.addAttribute("selectedSort", duLieuDanhSach.getSelectedSort());
        model.addAttribute("currentPage", duLieuDanhSach.getCurrentPage());
        model.addAttribute("totalPages", duLieuDanhSach.getTotalPages());
        model.addAttribute("totalItems", duLieuDanhSach.getTotalItems());
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/danh-sach-san-pham";
    }

    @GetMapping("/cua-hang/san-pham/{id}")
    public String chiTietSanPham(
            @PathVariable Integer id,
            @RequestParam(value = "variantId", required = false) Integer variantId,
            Model model
    ) {
        DuLieuTrangChiTietSanPhamOnline duLieuChiTiet = banHangOnlineService.layDuLieuChiTietSanPham(id, variantId);

        model.addAttribute("pageTitle", duLieuChiTiet.getProduct().getTenSanPham() + " | Aerion Sports");
        model.addAttribute("product", duLieuChiTiet.getProduct());
        model.addAttribute("variants", duLieuChiTiet.getVariants());
        model.addAttribute("selectedVariant", duLieuChiTiet.getSelectedVariant());
        model.addAttribute("relatedProducts", duLieuChiTiet.getRelatedProducts());
        model.addAttribute("activePage", "products");
        model.addAttribute("soLuongMacDinh", 1);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/chi-tiet-san-pham";
    }

    @GetMapping("/cua-hang/gio-hang")
    public String hienThiGioHang(Model model, HttpSession session) {
        DuLieuGioHangOnline duLieuGioHang = taoDuLieuGioHang(session);

        model.addAttribute("pageTitle", "Aerion Sports | Gio hang");
        model.addAttribute("activePage", "gio-hang");
        model.addAttribute("duLieuGioHang", duLieuGioHang);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/gio-hang";
    }

    @PostMapping("/cua-hang/gio-hang/them")
    public String themVaoGioHang(
            @RequestParam Integer productId,
            @RequestParam Integer variantId,
            @RequestParam Integer soLuong,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SanPhamBanHangOnlineView bienThe = banHangOnlineService.layBienTheSanPham(productId, variantId);
        if (bienThe == null) {
            redirectAttributes.addFlashAttribute("cartError", "Không tìm thấy biến thể sản phẩm để thêm vào giỏ.");
            return "redirect:/cua-hang/san-pham/" + productId;
        }

        int tonKho = bienThe.getStock() == null ? 0 : bienThe.getStock();
        if (tonKho <= 0) {
            redirectAttributes.addFlashAttribute("cartError", "Biến thể này đã hết hàng.");
            return "redirect:/cua-hang/san-pham/" + productId + "?variantId=" + variantId;
        }

        int soLuongHopLe = Math.max(1, soLuong == null ? 1 : soLuong);
        List<MucGioHangOnlineSession> gioHang = layGioHangTuSession(session);

        MucGioHangOnlineSession mucDaCo = null;
        for (MucGioHangOnlineSession item : gioHang) {
            if (Objects.equals(item.getProductId(), productId) && Objects.equals(item.getVariantId(), variantId)) {
                mucDaCo = item;
                break;
            }
        }

        if (mucDaCo == null) {
            gioHang.add(new MucGioHangOnlineSession(productId, variantId, Math.min(soLuongHopLe, tonKho)));
        } else {
            mucDaCo.setSoLuong(Math.min(mucDaCo.getSoLuong() + soLuongHopLe, tonKho));
        }

        luuGioHangVaoSession(session, gioHang);
        redirectAttributes.addFlashAttribute("cartSuccess", "Đã thêm sản phẩm vào giỏ hàng.");
        return "redirect:/cua-hang/san-pham/" + productId + "?variantId=" + variantId;
    }

    @PostMapping("/cua-hang/mua-ngay")
    public String muaNgay(
            @RequestParam Integer productId,
            @RequestParam Integer variantId,
            @RequestParam Integer soLuong,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        SanPhamBanHangOnlineView bienThe = banHangOnlineService.layBienTheSanPham(productId, variantId);
        if (bienThe == null) {
            redirectAttributes.addFlashAttribute("cartError", "Khong tim thay bien the san pham de mua ngay.");
            return "redirect:/cua-hang/san-pham/" + productId;
        }

        int tonKho = bienThe.getStock() == null ? 0 : bienThe.getStock();
        if (tonKho <= 0) {
            redirectAttributes.addFlashAttribute("cartError", "Bien the nay da het hang.");
            return "redirect:/cua-hang/san-pham/" + productId + "?variantId=" + variantId;
        }

        int soLuongHopLe = Math.max(1, soLuong == null ? 1 : soLuong);
        List<MucGioHangOnlineSession> gioHang = layGioHangTuSession(session);

        MucGioHangOnlineSession mucDaCo = null;
        for (MucGioHangOnlineSession item : gioHang) {
            if (Objects.equals(item.getProductId(), productId) && Objects.equals(item.getVariantId(), variantId)) {
                mucDaCo = item;
                break;
            }
        }

        if (mucDaCo == null) {
            gioHang.add(new MucGioHangOnlineSession(productId, variantId, Math.min(soLuongHopLe, tonKho)));
        } else {
            mucDaCo.setSoLuong(Math.min(mucDaCo.getSoLuong() + soLuongHopLe, tonKho));
        }

        luuGioHangVaoSession(session, gioHang);
        return "redirect:/cua-hang/checkout";
    }

    @PostMapping("/cua-hang/gio-hang/cap-nhat")
    public String capNhatSoLuongGioHang(
            @RequestParam Integer productId,
            @RequestParam Integer variantId,
            @RequestParam Integer soLuong,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        List<MucGioHangOnlineSession> gioHang = layGioHangTuSession(session);
        for (Iterator<MucGioHangOnlineSession> iterator = gioHang.iterator(); iterator.hasNext(); ) {
            MucGioHangOnlineSession item = iterator.next();
            if (Objects.equals(item.getProductId(), productId) && Objects.equals(item.getVariantId(), variantId)) {
                if (soLuong <= 0) {
                    iterator.remove();
                } else {
                    SanPhamBanHangOnlineView bienThe = banHangOnlineService.layBienTheSanPham(productId, variantId);
                    int tonKho = bienThe == null || bienThe.getStock() == null ? 0 : bienThe.getStock();
                    item.setSoLuong(Math.max(1, Math.min(soLuong, tonKho)));
                }
                break;
            }
        }

        luuGioHangVaoSession(session, gioHang);
        redirectAttributes.addFlashAttribute("cartSuccess", "Đã cập nhật giỏ hàng.");
        return "redirect:/cua-hang/gio-hang";
    }

    @PostMapping("/cua-hang/gio-hang/xoa")
    public String xoaKhoiGioHang(
            @RequestParam Integer productId,
            @RequestParam Integer variantId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        List<MucGioHangOnlineSession> gioHang = layGioHangTuSession(session);
        gioHang.removeIf(item -> Objects.equals(item.getProductId(), productId) && Objects.equals(item.getVariantId(), variantId));
        luuGioHangVaoSession(session, gioHang);
        redirectAttributes.addFlashAttribute("cartSuccess", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cua-hang/gio-hang";
    }

    @GetMapping("/cua-hang/checkout")
    public String hienThiThanhToan(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        DuLieuGioHangOnline duLieuGioHang = taoDuLieuGioHang(session);
        if (duLieuGioHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartError", "Giỏ hàng đang trống, vui lòng thêm sản phẩm trước khi thanh toán.");
            return "redirect:/cua-hang/gio-hang";
        }

        ThongTinDatHangOnlineRequest thongTinDatHang = layThongTinDatHangChoThanhToan(session);
        BigDecimal phiVanChuyen = tinhPhiVanChuyenOnline(thongTinDatHang);
        PhieuGiamGia phieuDangAp = layPhieuDangApDung(session, duLieuGioHang, phiVanChuyen);
        BigDecimal tienGiamVoucher = tinhTienGiamVoucher(phieuDangAp, duLieuGioHang, phiVanChuyen);
        BigDecimal tongThanhToan = duLieuGioHang.getTongCong().add(phiVanChuyen).subtract(tienGiamVoucher).max(BigDecimal.ZERO);

        model.addAttribute("pageTitle", "Aerion Sports | Thanh toan");
        model.addAttribute("activePage", "checkout");
        model.addAttribute("duLieuGioHang", duLieuGioHang);
        model.addAttribute("phiVanChuyen", phiVanChuyen);
        model.addAttribute("phieuDangAp", phieuDangAp);
        model.addAttribute("danhSachPhieuCoTheDung", layDanhSachPhieuCoTheDung(duLieuGioHang, phiVanChuyen, session));
        model.addAttribute("tienGiamVoucher", tienGiamVoucher);
        model.addAttribute("tongThanhToan", tongThanhToan);
        model.addAttribute("thongTinDatHang", thongTinDatHang);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/thanh-toan";
    }

    @PostMapping("/cua-hang/checkout/ap-dung-phieu")
    public String apDungPhieuGiamGia(
            @RequestParam String maPhieuGiamGia,
            ThongTinDatHangOnlineRequest thongTinDatHang,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        luuThongTinDatHangVaoSession(session, thongTinDatHang);
        DuLieuGioHangOnline duLieuGioHang = taoDuLieuGioHang(session);
        if (duLieuGioHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartError", "Giỏ hàng đang trống, không thể áp dụng mã giảm giá.");
            return "redirect:/cua-hang/gio-hang";
        }

        PhieuGiamGia phieu = timPhieuHopLeTheoMa(maPhieuGiamGia, session);
        if (phieu == null) {
            redirectAttributes.addFlashAttribute("checkoutError", "Mã giảm giá không tồn tại hoặc đã hết hiệu lực.");
            return "redirect:/cua-hang/checkout";
        }

        BigDecimal giaTriToiThieu = phieu.getGiaTriDonToiThieu() == null ? BigDecimal.ZERO : phieu.getGiaTriDonToiThieu();
        if (duLieuGioHang.getTongCong().compareTo(giaTriToiThieu) < 0) {
            redirectAttributes.addFlashAttribute("checkoutError", "Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã này.");
            return "redirect:/cua-hang/checkout";
        }

        session.setAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY, phieu.getId());
        redirectAttributes.addFlashAttribute("checkoutSuccess", "Đã áp dụng mã giảm giá " + phieu.getMaPhieuGiamGia() + ".");
        return "redirect:/cua-hang/checkout";
    }

    @PostMapping("/cua-hang/checkout/xoa-phieu")
    public String xoaPhieuGiamGia(
            ThongTinDatHangOnlineRequest thongTinDatHang,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        luuThongTinDatHangVaoSession(session, thongTinDatHang);
        session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
        redirectAttributes.addFlashAttribute("checkoutSuccess", "Đã gỡ mã giảm giá khỏi đơn hàng.");
        return "redirect:/cua-hang/checkout";
    }

    @PostMapping("/cua-hang/checkout/dat-hang")
    public String datHangOnline(
            ThongTinDatHangOnlineRequest thongTinDatHang,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        luuThongTinDatHangVaoSession(session, thongTinDatHang);
        String loiThongTinDatHang = kiemTraThongTinDatHangHopLe(thongTinDatHang);
        if (loiThongTinDatHang != null) {
            redirectAttributes.addFlashAttribute("checkoutError", loiThongTinDatHang);
            return "redirect:/cua-hang/checkout";
        }
        DuLieuGioHangOnline duLieuGioHang = taoDuLieuGioHang(session);
        if (duLieuGioHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartError", "Gio hang dang trong, khong the dat hang.");
            return "redirect:/cua-hang/gio-hang";
        }

        BigDecimal phiVanChuyen = tinhPhiVanChuyenOnline(thongTinDatHang);
        PhieuGiamGia phieuDangAp = layPhieuDangApDung(session, duLieuGioHang, phiVanChuyen);
        BigDecimal tienGiamVoucher = tinhTienGiamVoucher(phieuDangAp, duLieuGioHang, phiVanChuyen);
        BigDecimal tongThanhToan = duLieuGioHang.getTongCong().add(phiVanChuyen).subtract(tienGiamVoucher).max(BigDecimal.ZERO);

        try {
            String maHoaDon = banHangOnlineService.datHangOnline(
                    thongTinDatHang,
                    duLieuGioHang,
                    phieuDangAp,
                    phiVanChuyen,
                    tienGiamVoucher,
                    tongThanhToan
            );

            xoaGioHangOnlineTheoKhachDangNhap(session);
            session.removeAttribute(GIO_HANG_ONLINE_SESSION_KEY);
            session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            session.removeAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);
            redirectAttributes.addFlashAttribute("checkoutSuccess", "Đặt hàng thành công. Mã hoá đơn của bạn là " + maHoaDon + ".");
            return "redirect:/cua-hang/theo-doi-don-hang?code=" + maHoaDon;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("checkoutError", ex.getMessage());
            return "redirect:/cua-hang/checkout";
        }
    }

    @GetMapping("/cua-hang/dang-nhap")
    public String hienThiDangNhap(Model model, HttpSession session) {
        if (daDangNhapKhachHangOnline(session)) {
            return "redirect:/cua-hang";
        }
        model.addAttribute("pageTitle", "Aerion Sports | Dang nhap");
        model.addAttribute("activePage", "auth");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/dang-nhap";
    }

    @PostMapping("/cua-hang/dang-nhap")
    public String dangNhapKhachHangOnline(
            @RequestParam String email,
            @RequestParam String matKhau,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String emailDaChuanHoa = chuanHoaEmail(email);
        if (!StringUtils.hasText(emailDaChuanHoa) || !StringUtils.hasText(matKhau)) {
            redirectAttributes.addFlashAttribute("authError", "Vui long nhap email va mat khau.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }
        if (!emailHopLe(emailDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("authError", "Email khong dung dinh dang.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhapAndTrangThai(emailDaChuanHoa, 1);
        if (taiKhoanOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Tai khoan khong ton tai hoac da bi khoa.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        TaiKhoan taiKhoan = taiKhoanOpt.get();
        if (!"KHACH_HANG".equalsIgnoreCase(taiKhoan.getLoaiTaiKhoan())) {
            redirectAttributes.addFlashAttribute("authError", "Tai khoan nay khong phai tai khoan khach hang online.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        if (!passwordEncoder.matches(matKhau, taiKhoan.getMatKhauHash())) {
            redirectAttributes.addFlashAttribute("authError", "Mat khau khong chinh xac.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangRepository.findById(taiKhoan.getIdChuTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Khong tim thay thong tin khach hang."));
        luuKhachHangOnlineVaoSession(session, khachHang);
        hopNhatGioHangOnlineSauDangNhap(session, khachHang.getId());
        redirectAttributes.addFlashAttribute("authSuccess", "Dang nhap thanh cong.");
        return "redirect:/cua-hang";
    }

    @GetMapping("/cua-hang/dang-ky")
    public String hienThiDangKy(Model model, HttpSession session) {
        if (daDangNhapKhachHangOnline(session)) {
            return "redirect:/cua-hang";
        }
        model.addAttribute("pageTitle", "Aerion Sports | Tao tai khoan");
        model.addAttribute("activePage", "auth");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/dang-ky";
    }

    @PostMapping("/cua-hang/dang-ky")
    public String dangKyKhachHangOnline(
            @RequestParam String hoTen,
            @RequestParam String soDienThoai,
            @RequestParam String email,
            @RequestParam String matKhau,
            @RequestParam String xacNhanMatKhau,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String hoTenDaChuanHoa = chuanHoaText(hoTen);
        String sdtDaChuanHoa = chuanHoaSoDienThoai(soDienThoai);
        String emailDaChuanHoa = chuanHoaEmail(email);

        redirectAttributes.addFlashAttribute("registerHoTen", hoTenDaChuanHoa);
        redirectAttributes.addFlashAttribute("registerSoDienThoai", sdtDaChuanHoa);
        redirectAttributes.addFlashAttribute("registerEmail", emailDaChuanHoa);

        if (!StringUtils.hasText(hoTenDaChuanHoa)
                || !StringUtils.hasText(sdtDaChuanHoa)
                || !StringUtils.hasText(emailDaChuanHoa)
                || !StringUtils.hasText(matKhau)) {
            redirectAttributes.addFlashAttribute("authError", "Vui long nhap day du thong tin dang ky.");
            return "redirect:/cua-hang/dang-ky";
        }
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("authError", "So dien thoai phai bat dau bang 0 va du 10 chu so.");
            return "redirect:/cua-hang/dang-ky";
        }
        if (!emailHopLe(emailDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("authError", "Email khong dung dinh dang.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (matKhau.length() < 6) {
            redirectAttributes.addFlashAttribute("authError", "Mat khau phai co it nhat 6 ky tu.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (!Objects.equals(matKhau, xacNhanMatKhau)) {
            redirectAttributes.addFlashAttribute("authError", "Xac nhan mat khau khong trung khop.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (taiKhoanRepository.findByTenDangNhap(emailDaChuanHoa).isPresent()) {
            redirectAttributes.addFlashAttribute("authError", "Email nay da co tai khoan dang nhap.");
            return "redirect:/cua-hang/dang-ky";
        }

        Optional<KhachHang> theoEmail = khachHangRepository.findByEmail(emailDaChuanHoa);
        Optional<KhachHang> theoSdt = khachHangRepository.findBySdt(sdtDaChuanHoa);
        if (theoEmail.isPresent() && theoSdt.isPresent()
                && !Objects.equals(theoEmail.get().getId(), theoSdt.get().getId())) {
            redirectAttributes.addFlashAttribute("authError", "Email va so dien thoai dang thuoc ve 2 khach hang khac nhau.");
            return "redirect:/cua-hang/dang-ky";
        }

        KhachHang khachHang = theoEmail.orElseGet(() -> theoSdt.orElseGet(KhachHang::new));
        boolean laKhachHangMoi = khachHang.getId() == null;
        if (laKhachHangMoi) {
            khachHang.setMaKhachHang(taoMaKhachHangMoi());
            khachHang.setNgayTao(LocalDateTime.now());
            khachHang.setDiemTichLuy(0);
            khachHang.setTrangThai(1);
        }
        khachHang.setHoTen(hoTenDaChuanHoa);
        khachHang.setSdt(sdtDaChuanHoa);
        khachHang.setEmail(emailDaChuanHoa);
        khachHang.setNgayCapNhat(LocalDateTime.now());
        KhachHang khachHangDaLuu = khachHangRepository.save(khachHang);

        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(emailDaChuanHoa);
        taiKhoan.setMatKhauHash(passwordEncoder.encode(matKhau));
        taiKhoan.setLoaiTaiKhoan("KHACH_HANG");
        taiKhoan.setIdChuTaiKhoan(khachHangDaLuu.getId());
        taiKhoan.setNgayTao(LocalDateTime.now());
        taiKhoan.setNgayCapNhat(LocalDateTime.now());
        taiKhoan.setTrangThai(1);
        taiKhoanRepository.save(taiKhoan);

        luuKhachHangOnlineVaoSession(session, khachHangDaLuu);
        hopNhatGioHangOnlineSauDangNhap(session, khachHangDaLuu.getId());
        redirectAttributes.addFlashAttribute("authSuccess", "Dang ky tai khoan thanh cong.");
        return "redirect:/cua-hang";
    }

    @PostMapping("/cua-hang/dang-xuat")
    public String dangXuatKhachHangOnline(HttpSession session, RedirectAttributes redirectAttributes) {
        luuGioHangVaoKhoTheoKhachDangNhap(session);
        session.removeAttribute(GIO_HANG_ONLINE_SESSION_KEY);
        session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
        session.removeAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);
        session.removeAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        session.removeAttribute(KHACH_HANG_ONLINE_TEN_SESSION_KEY);
        session.removeAttribute(KHACH_HANG_ONLINE_EMAIL_SESSION_KEY);
        redirectAttributes.addFlashAttribute("authSuccess", "Da dang xuat.");
        return "redirect:/cua-hang";
    }

    @GetMapping("/cua-hang/ho-so")
    public String hienThiHoSo(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (!(khachHangId instanceof Integer id)) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de xem ho so ca nhan.");
            return "redirect:/cua-hang/dang-nhap";
        }

        Optional<KhachHang> khachHangOptional = khachHangRepository.findById(id);
        if (khachHangOptional.isEmpty()) {
            session.removeAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
            session.removeAttribute(KHACH_HANG_ONLINE_TEN_SESSION_KEY);
            session.removeAttribute(KHACH_HANG_ONLINE_EMAIL_SESSION_KEY);
            redirectAttributes.addFlashAttribute("authError", "Khong tim thay thong tin khach hang. Vui long dang nhap lai.");
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangOptional.get();
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(id);
        DiaChiKhachHang diaChiMacDinh = danhSachDiaChi.stream()
                .filter(diaChi -> Boolean.TRUE.equals(diaChi.getMacDinh()))
                .findFirst()
                .orElse(danhSachDiaChi.isEmpty() ? null : danhSachDiaChi.get(0));
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "Ho so cua toi | Aerion Sports");
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("diaChiMacDinh", diaChiMacDinh);
        model.addAttribute("danhSachDiaChi", danhSachDiaChi);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/thong-tin")
    public String capNhatThongTinCaNhanHoSo(
            @RequestParam("hoTen") String hoTen,
            @RequestParam("sdt") String sdt,
            @RequestParam(value = "gioiTinh", required = false) Integer gioiTinh,
            @RequestParam(value = "ngaySinh", required = false) String ngaySinh,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de cap nhat ho so.");
            return "redirect:/cua-hang/dang-nhap";
        }

        String hoTenDaChuanHoa = chuanHoaText(hoTen);
        String sdtDaChuanHoa = chuanHoaSoDienThoai(sdt);
        if (!StringUtils.hasText(hoTenDaChuanHoa) || !StringUtils.hasText(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("profileError", "Vui long nhap ho ten va so dien thoai.");
            return "redirect:/cua-hang/ho-so";
        }
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("profileError", "So dien thoai phai bat dau bang 0 va du 10 chu so.");
            return "redirect:/cua-hang/ho-so";
        }

        KhachHang khachHang = khachHangOptional.get();
        khachHang.setHoTen(hoTenDaChuanHoa);
        khachHang.setSdt(sdtDaChuanHoa);
        khachHang.setGioiTinh(chuanHoaGioiTinh(gioiTinh));
        khachHang.setNgaySinh(chuyenNgaySinh(ngaySinh));
        khachHang.setNgayCapNhat(LocalDateTime.now());
        KhachHang khachHangDaLuu = khachHangRepository.save(khachHang);
        luuKhachHangOnlineVaoSession(session, khachHangDaLuu);

        redirectAttributes.addFlashAttribute("profileSuccess", "Da cap nhat thong tin ca nhan.");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi")
    public String themDiaChiHoSo(
            @RequestParam("nguoiNhan") String nguoiNhan,
            @RequestParam("sdt") String sdt,
            @RequestParam("tinhThanh") String tinhThanh,
            @RequestParam(value = "quanHuyen", required = false) String quanHuyen,
            @RequestParam("phuongXa") String phuongXa,
            @RequestParam(value = "diaChiChiTiet", required = false) String diaChiChiTiet,
            @RequestParam(value = "macDinh", defaultValue = "false") boolean macDinh,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de them dia chi.");
            return "redirect:/cua-hang/dang-nhap";
        }

        if (!duLieuDiaChiHopLe(nguoiNhan, sdt, tinhThanh, phuongXa)) {
            redirectAttributes.addFlashAttribute("profileError", "Vui long nhap day du nguoi nhan, so dien thoai, tinh/thanh va phuong/xa.");
            return "redirect:/cua-hang/ho-so";
        }
        String sdtDaChuanHoa = chuanHoaSoDienThoai(sdt);
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("profileError", "So dien thoai phai bat dau bang 0 va du 10 chu so.");
            return "redirect:/cua-hang/ho-so";
        }

        KhachHang khachHang = khachHangOptional.get();
        List<DiaChiKhachHang> diaChiCu = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(khachHang.getId());
        boolean chuaCoDiaChiMacDinh = diaChiCu.stream().noneMatch(diaChi -> Boolean.TRUE.equals(diaChi.getMacDinh()));
        boolean datLamMacDinh = macDinh || diaChiCu.isEmpty() || chuaCoDiaChiMacDinh;
        if (datLamMacDinh) {
            boMacDinhDiaChiOnline(diaChiCu);
        }

        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setKhachHang(khachHang);
        diaChi.setNguoiNhan(chuanHoaText(nguoiNhan));
        diaChi.setSdt(sdtDaChuanHoa);
        diaChi.setTinhThanh(chuanHoaText(tinhThanh));
        diaChi.setPhuongXa(chuanHoaText(phuongXa));
        diaChi.setDiaChiChiTiet(gopDiaChiCuTheVaQuanHuyen(diaChiChiTiet, quanHuyen));
        diaChi.setMacDinh(datLamMacDinh);
        diaChi.setNgayTao(LocalDateTime.now());
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiKhachHangRepository.save(diaChi);

        redirectAttributes.addFlashAttribute("profileSuccess", "Da them dia chi moi.");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi/{idDiaChi}")
    public String capNhatDiaChiHoSo(
            @PathVariable Integer idDiaChi,
            @RequestParam("nguoiNhan") String nguoiNhan,
            @RequestParam("sdt") String sdt,
            @RequestParam("tinhThanh") String tinhThanh,
            @RequestParam(value = "quanHuyen", required = false) String quanHuyen,
            @RequestParam("phuongXa") String phuongXa,
            @RequestParam(value = "diaChiChiTiet", required = false) String diaChiChiTiet,
            @RequestParam(value = "macDinh", defaultValue = "false") boolean macDinh,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de sua dia chi.");
            return "redirect:/cua-hang/dang-nhap";
        }

        if (!duLieuDiaChiHopLe(nguoiNhan, sdt, tinhThanh, phuongXa)) {
            redirectAttributes.addFlashAttribute("profileError", "Vui long nhap day du nguoi nhan, so dien thoai, tinh/thanh va phuong/xa.");
            return "redirect:/cua-hang/ho-so";
        }
        String sdtDaChuanHoa = chuanHoaSoDienThoai(sdt);
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("profileError", "So dien thoai phai bat dau bang 0 va du 10 chu so.");
            return "redirect:/cua-hang/ho-so";
        }

        KhachHang khachHang = khachHangOptional.get();
        Optional<DiaChiKhachHang> diaChiOptional = diaChiKhachHangRepository.findById(idDiaChi);
        if (diaChiOptional.isEmpty()
                || diaChiOptional.get().getKhachHang() == null
                || !Objects.equals(diaChiOptional.get().getKhachHang().getId(), khachHang.getId())) {
            redirectAttributes.addFlashAttribute("profileError", "Khong tim thay dia chi can sua.");
            return "redirect:/cua-hang/ho-so";
        }

        List<DiaChiKhachHang> diaChiCu = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(khachHang.getId());
        if (macDinh) {
            boMacDinhDiaChiOnline(diaChiCu);
        }

        DiaChiKhachHang diaChi = diaChiOptional.get();
        diaChi.setNguoiNhan(chuanHoaText(nguoiNhan));
        diaChi.setSdt(sdtDaChuanHoa);
        diaChi.setTinhThanh(chuanHoaText(tinhThanh));
        diaChi.setPhuongXa(chuanHoaText(phuongXa));
        diaChi.setDiaChiChiTiet(gopDiaChiCuTheVaQuanHuyen(diaChiChiTiet, quanHuyen));
        diaChi.setMacDinh(macDinh || Boolean.TRUE.equals(diaChi.getMacDinh()));
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiKhachHangRepository.save(diaChi);

        redirectAttributes.addFlashAttribute("profileSuccess", "Da cap nhat dia chi.");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi/mac-dinh/{idDiaChi}")
    public String datDiaChiMacDinh(
            @PathVariable Integer idDiaChi,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để thiết lập địa chỉ.");
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangOptional.get();
        Optional<DiaChiKhachHang> diaChiOptional = diaChiKhachHangRepository.findById(idDiaChi);
        
        if (diaChiOptional.isPresent() && 
            diaChiOptional.get().getKhachHang() != null && 
            Objects.equals(diaChiOptional.get().getKhachHang().getId(), khachHang.getId())) {
            
            List<DiaChiKhachHang> diaChiCu = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(khachHang.getId());
            boMacDinhDiaChiOnline(diaChiCu);
            
            DiaChiKhachHang diaChi = diaChiOptional.get();
            diaChi.setMacDinh(true);
            diaChi.setNgayCapNhat(LocalDateTime.now());
            diaChiKhachHangRepository.save(diaChi);
            
            redirectAttributes.addFlashAttribute("profileSuccess", "Đã cập nhật địa chỉ mặc định.");
        }
        
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi/xoa/{idDiaChi}")
    public String xoaDiaChiHoSo(
            @PathVariable Integer idDiaChi,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de xoa dia chi.");
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangOptional.get();
        Optional<DiaChiKhachHang> diaChiOptional = diaChiKhachHangRepository.findById(idDiaChi);
        if (diaChiOptional.isEmpty()
                || diaChiOptional.get().getKhachHang() == null
                || !Objects.equals(diaChiOptional.get().getKhachHang().getId(), khachHang.getId())) {
            redirectAttributes.addFlashAttribute("profileError", "Khong tim thay dia chi can xoa.");
            return "redirect:/cua-hang/ho-so";
        }

        boolean laDiaChiMacDinh = Boolean.TRUE.equals(diaChiOptional.get().getMacDinh());
        diaChiKhachHangRepository.delete(diaChiOptional.get());

        if (laDiaChiMacDinh) {
            List<DiaChiKhachHang> diaChiConLai = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(khachHang.getId());
            if (!diaChiConLai.isEmpty()) {
                DiaChiKhachHang diaChiMoi = diaChiConLai.get(0);
                diaChiMoi.setMacDinh(true);
                diaChiMoi.setNgayCapNhat(LocalDateTime.now());
                diaChiKhachHangRepository.save(diaChiMoi);
            }
        }

        redirectAttributes.addFlashAttribute("profileSuccess", "Da xoa dia chi.");
        return "redirect:/cua-hang/ho-so";
    }

    @GetMapping("/cua-hang/don-hang-cua-toi")
    public String hienThiDonHangCuaToi(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "code", required = false) String code,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de xem don hang cua toi.");
            return "redirect:/cua-hang/dang-nhap";
        }

        String keywordDaChuanHoa = StringUtils.hasText(keyword) ? chuanHoaText(keyword) : null;
        List<DonHangCuaToiView> danhSachDonHang = hoaDonRepository
                .findDonHangOnlineTheoKhachHang(khachHangOptional.get().getId(), keywordDaChuanHoa, trangThai)
                .stream()
                .map(this::taoDonHangCuaToiView)
                .toList();

        DonHangCuaToiView donHangDangChon = chonDonHangDangXem(danhSachDonHang, code);

        model.addAttribute("pageTitle", "Aerion Sports - Don hang cua toi");
        model.addAttribute("activePage", "donHangCuaToi");
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("donHangDangChon", donHangDangChon);
        model.addAttribute("keyword", keywordDaChuanHoa);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("trangThaiOptions", layTrangThaiDonHangOptions());
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/don-hang-cua-toi";
    }

    @PostMapping("/cua-hang/don-hang-cua-toi/{maHoaDon}/huy")
    @Transactional
    public String huyDonHangCuaToi(
            @PathVariable String maHoaDon,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de huy don hang.");
            return "redirect:/cua-hang/dang-nhap";
        }

        HoaDon hoaDon = layDonHangOnlineCuaKhach(maHoaDon, khachHangOptional.get().getId())
                .orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("orderError", "Khong tim thay don hang cua ban.");
            return "redirect:/cua-hang/don-hang-cua-toi";
        }

        if (!Objects.equals(hoaDon.getTrangThai(), 0)) {
            redirectAttributes.addFlashAttribute("orderError", "Chi don hang dang cho xac nhan moi duoc huy.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        Integer trangThaiCu = hoaDon.getTrangThai();
        hoaDon.setTrangThai(6);
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat("Khach hang online");
        hoaDonRepository.save(hoaDon);

        luuLichSuDonHangOnline(hoaDon, trangThaiCu, 6, "Khach hang huy don", "Khach hang huy don khi don dang cho xac nhan.");
        redirectAttributes.addFlashAttribute("orderSuccess", "Da huy don hang " + hoaDon.getMaHoaDon() + ".");
        return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
    }

    @PostMapping("/cua-hang/don-hang-cua-toi/{maHoaDon}/cap-nhat-giao-hang")
    @Transactional
    public String capNhatGiaoHangDonHangCuaToi(
            @PathVariable String maHoaDon,
            @RequestParam String tenNguoiNhan,
            @RequestParam String sdtNguoiNhan,
            @RequestParam String diaChiNhan,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui long dang nhap de sua thong tin giao hang.");
            return "redirect:/cua-hang/dang-nhap";
        }

        HoaDon hoaDon = layDonHangOnlineCuaKhach(maHoaDon, khachHangOptional.get().getId())
                .orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("orderError", "Khong tim thay don hang cua ban.");
            return "redirect:/cua-hang/don-hang-cua-toi";
        }

        if (!Objects.equals(hoaDon.getTrangThai(), 0)) {
            redirectAttributes.addFlashAttribute("orderError", "Chi don hang dang cho xac nhan moi duoc sua thong tin giao hang.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        String sdtNguoiNhanDaChuanHoa = chuanHoaSoDienThoai(sdtNguoiNhan);
        if (!StringUtils.hasText(tenNguoiNhan) || !StringUtils.hasText(sdtNguoiNhanDaChuanHoa) || !StringUtils.hasText(diaChiNhan)) {
            redirectAttributes.addFlashAttribute("orderError", "Vui long nhap day du ten nguoi nhan, so dien thoai va dia chi.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }
        if (!soDienThoaiHopLe(sdtNguoiNhanDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("orderError", "So dien thoai phai bat dau bang 0 va du 10 chu so.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        hoaDon.setTenNguoiNhan(chuanHoaText(tenNguoiNhan));
        hoaDon.setSdtNguoiNhan(sdtNguoiNhanDaChuanHoa);
        hoaDon.setDiaChiNhan(chuanHoaText(diaChiNhan));
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat("Khach hang online");
        hoaDonRepository.save(hoaDon);

        luuLichSuDonHangOnline(hoaDon, 0, 0, "Khach hang sua thong tin giao hang", "Khach hang cap nhat ten, so dien thoai hoac dia chi nhan hang.");
        redirectAttributes.addFlashAttribute("orderSuccess", "Da cap nhat thong tin giao hang.");
        return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
    }

    @GetMapping("/cua-hang/theo-doi-don-hang")
    public String hienThiTheoDoiDonHang(
            @RequestParam(value = "code", required = false) String code,
            Model model
    ) {
        model.addAttribute("pageTitle", "Aerion Sports | Theo dõi đơn hàng");
        model.addAttribute("activePage", "tracking");
        if (code != null && !code.trim().isEmpty()) {
            Optional<HoaDon> hoaDonOptional = hoaDonRepository.findByMaHoaDonWithThongTin(code.trim());
            if (hoaDonOptional.isPresent()) {
                HoaDon hoaDon = chuanHoaTrangThaiDonOnlineDatMoi(hoaDonOptional.get());
                HoaDonResponse trackingOrder = new HoaDonResponse(hoaDon);
                List<ChiTietHoaDonResponse> trackingItems = chiTietHoaDonRepository.getChiTietHoaDon(hoaDon.getId());

                model.addAttribute("trackingOrder", trackingOrder);
                model.addAttribute("trackingItems", trackingItems);
                model.addAttribute("trackingStep", tinhBuocTheoDoi(trackingOrder.getTrangThai()));
                model.addAttribute("trackingSuccess", "Tra cứu đơn hàng thành công.");
            } else {
                model.addAttribute("error", "Không tìm thấy đơn hàng với mã " + code.trim() + ".");
            }
        }
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/theo-doi-don-hang";
    }

    private DuLieuGioHangOnline taoDuLieuGioHang(HttpSession session) {
        List<MucGioHangOnlineSession> gioHang = layGioHangTuSession(session);
        List<MucGioHangOnlineView> items = new ArrayList<>();
        BigDecimal tamTinh = BigDecimal.ZERO;
        BigDecimal tongGiamGia = BigDecimal.ZERO;
        int tongSoLuong = 0;

        for (MucGioHangOnlineSession item : gioHang) {
            SanPhamBanHangOnlineView bienThe = banHangOnlineService.layBienTheSanPham(item.getProductId(), item.getVariantId());
            if (bienThe == null) {
                continue;
            }

            int tonKho = bienThe.getStock() == null ? 0 : bienThe.getStock();
            int soLuongHopLe = Math.max(1, Math.min(item.getSoLuong(), tonKho > 0 ? tonKho : item.getSoLuong()));
            item.setSoLuong(soLuongHopLe);

            BigDecimal donGia = bienThe.getPrice() == null ? BigDecimal.ZERO : bienThe.getPrice();
            BigDecimal donGiaGoc = bienThe.getOriginalPrice() == null ? donGia : bienThe.getOriginalPrice();
            BigDecimal thanhTien = donGia.multiply(BigDecimal.valueOf(soLuongHopLe));
            BigDecimal tienGiam = donGiaGoc.subtract(donGia).max(BigDecimal.ZERO).multiply(BigDecimal.valueOf(soLuongHopLe));

            items.add(new MucGioHangOnlineView(
                    bienThe.getProductId(),
                    bienThe.getVariantId(),
                    bienThe.getName(),
                    bienThe.getBrand(),
                    bienThe.getColor(),
                    bienThe.getWeight(),
                    bienThe.getImageUrl(),
                    donGia,
                    donGiaGoc,
                    soLuongHopLe,
                    tonKho,
                    thanhTien
            ));

            tongSoLuong += soLuongHopLe;
            tamTinh = tamTinh.add(thanhTien);
            tongGiamGia = tongGiamGia.add(tienGiam);
        }

        luuGioHangVaoSession(session, gioHang);
        return new DuLieuGioHangOnline(items, tongSoLuong, tamTinh, tongGiamGia, tamTinh);
    }

    @SuppressWarnings("unchecked")
    private List<MucGioHangOnlineSession> layGioHangTuSession(HttpSession session) {
        Object value = session.getAttribute(GIO_HANG_ONLINE_SESSION_KEY);
        if (value instanceof List<?> list) {
            return (List<MucGioHangOnlineSession>) list;
        }

        List<MucGioHangOnlineSession> gioHangMoi = layIdKhachHangOnlineTuSession(session)
                .map(idKhachHang -> saoChepGioHang(GIO_HANG_ONLINE_THEO_KHACH.get(idKhachHang)))
                .orElseGet(ArrayList::new);
        session.setAttribute(GIO_HANG_ONLINE_SESSION_KEY, gioHangMoi);
        return gioHangMoi;
    }

    private void luuGioHangVaoSession(HttpSession session, List<MucGioHangOnlineSession> gioHang) {
        session.setAttribute(GIO_HANG_ONLINE_SESSION_KEY, gioHang);
        layIdKhachHangOnlineTuSession(session).ifPresent(idKhachHang ->
                GIO_HANG_ONLINE_THEO_KHACH.put(idKhachHang, saoChepGioHang(gioHang)));
    }

    private Optional<Integer> layIdKhachHangOnlineTuSession(HttpSession session) {
        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (khachHangId instanceof Integer id) {
            return Optional.of(id);
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private List<MucGioHangOnlineSession> layBanSaoGioHangTrongSession(HttpSession session) {
        Object value = session.getAttribute(GIO_HANG_ONLINE_SESSION_KEY);
        if (value instanceof List<?> list) {
            return saoChepGioHang((List<MucGioHangOnlineSession>) list);
        }
        return new ArrayList<>();
    }

    private List<MucGioHangOnlineSession> saoChepGioHang(List<MucGioHangOnlineSession> gioHang) {
        List<MucGioHangOnlineSession> banSao = new ArrayList<>();
        if (gioHang == null) {
            return banSao;
        }
        for (MucGioHangOnlineSession item : gioHang) {
            if (item == null) {
                continue;
            }
            banSao.add(new MucGioHangOnlineSession(item.getProductId(), item.getVariantId(), item.getSoLuong()));
        }
        return banSao;
    }

    private void hopNhatGioHangOnlineSauDangNhap(HttpSession session, Integer idKhachHang) {
        if (idKhachHang == null) {
            return;
        }

        List<MucGioHangOnlineSession> gioHangDaLuu = saoChepGioHang(GIO_HANG_ONLINE_THEO_KHACH.get(idKhachHang));
        List<MucGioHangOnlineSession> gioHangTrongSession = layBanSaoGioHangTrongSession(session);
        List<MucGioHangOnlineSession> gioHangHopNhat = hopNhatGioHang(gioHangDaLuu, gioHangTrongSession);

        session.setAttribute(GIO_HANG_ONLINE_SESSION_KEY, gioHangHopNhat);
        GIO_HANG_ONLINE_THEO_KHACH.put(idKhachHang, saoChepGioHang(gioHangHopNhat));
    }

    private List<MucGioHangOnlineSession> hopNhatGioHang(
            List<MucGioHangOnlineSession> gioHangGoc,
            List<MucGioHangOnlineSession> gioHangCanThem
    ) {
        List<MucGioHangOnlineSession> ketQua = saoChepGioHang(gioHangGoc);
        for (MucGioHangOnlineSession itemCanThem : saoChepGioHang(gioHangCanThem)) {
            MucGioHangOnlineSession mucDaCo = ketQua.stream()
                    .filter(item -> Objects.equals(item.getProductId(), itemCanThem.getProductId())
                            && Objects.equals(item.getVariantId(), itemCanThem.getVariantId()))
                    .findFirst()
                    .orElse(null);
            if (mucDaCo == null) {
                ketQua.add(itemCanThem);
            } else {
                mucDaCo.setSoLuong(Math.max(1, mucDaCo.getSoLuong()) + Math.max(1, itemCanThem.getSoLuong()));
            }
        }
        return ketQua;
    }

    private void luuGioHangVaoKhoTheoKhachDangNhap(HttpSession session) {
        layIdKhachHangOnlineTuSession(session).ifPresent(idKhachHang ->
                GIO_HANG_ONLINE_THEO_KHACH.put(idKhachHang, layBanSaoGioHangTrongSession(session)));
    }

    private void xoaGioHangOnlineTheoKhachDangNhap(HttpSession session) {
        layIdKhachHangOnlineTuSession(session).ifPresent(GIO_HANG_ONLINE_THEO_KHACH::remove);
    }

    private DonHangCuaToiView taoDonHangCuaToiView(HoaDon hoaDon) {
        HoaDon hoaDonDaChuanHoa = chuanHoaTrangThaiDonOnlineDatMoi(hoaDon);
        HoaDonResponse response = new HoaDonResponse(hoaDonDaChuanHoa);
        chuanHoaTienHoaDonResponse(response);
        List<ChiTietHoaDonResponse> chiTiet = chiTietHoaDonRepository.getChiTietHoaDon(hoaDonDaChuanHoa.getId());
        int tongSanPham = chiTiet.stream()
                .map(ChiTietHoaDonResponse::getSoLuong)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum();
        return new DonHangCuaToiView(response, chiTiet, tongSanPham, cssTrangThaiDonHang(response.getTrangThai()));
    }

    private void chuanHoaTienHoaDonResponse(HoaDonResponse response) {
        if (response.getTongTienHang() == null) {
            response.setTongTienHang(BigDecimal.ZERO);
        }
        if (response.getTienGiam() == null) {
            response.setTienGiam(BigDecimal.ZERO);
        }
        if (response.getTienVanChuyen() == null) {
            response.setTienVanChuyen(BigDecimal.ZERO);
        }
        if (response.getTongTienThanhToan() == null) {
            response.setTongTienThanhToan(BigDecimal.ZERO);
        }
    }

    private DonHangCuaToiView chonDonHangDangXem(List<DonHangCuaToiView> danhSachDonHang, String code) {
        if (danhSachDonHang == null || danhSachDonHang.isEmpty()) {
            return null;
        }
        if (StringUtils.hasText(code)) {
            String maCanChon = code.trim();
            return danhSachDonHang.stream()
                    .filter(item -> Objects.equals(item.hoaDon().getMaHoaDon(), maCanChon))
                    .findFirst()
                    .orElse(danhSachDonHang.get(0));
        }
        return danhSachDonHang.get(0);
    }

    private Optional<HoaDon> layDonHangOnlineCuaKhach(String maHoaDon, Integer idKhachHang) {
        if (!StringUtils.hasText(maHoaDon) || idKhachHang == null) {
            return Optional.empty();
        }
        return hoaDonRepository.findByMaHoaDonWithThongTin(maHoaDon.trim())
                .filter(hoaDon -> Objects.equals(hoaDon.getLoaiHoaDon(), 1))
                .filter(hoaDon -> hoaDon.getKhachHang() != null)
                .filter(hoaDon -> Objects.equals(hoaDon.getKhachHang().getId(), idKhachHang));
    }

    private void luuLichSuDonHangOnline(
            HoaDon hoaDon,
            Integer trangThaiCu,
            Integer trangThaiMoi,
            String hanhDong,
            String ghiChu
    ) {
        LichSuHoaDon lichSuHoaDon = new LichSuHoaDon();
        lichSuHoaDon.setHoaDon(hoaDon);
        NhanVien nhanVienMacDinh = nhanVienRepository.findById(1)
                .orElseThrow(() -> new RuntimeException("Khong tim thay nhan vien mac dinh de luu lich su hoa don."));
        lichSuHoaDon.setNhanVien(nhanVienMacDinh);
        lichSuHoaDon.setTrangThaiCu(trangThaiCu);
        lichSuHoaDon.setTrangThaiMoi(trangThaiMoi);
        lichSuHoaDon.setHanhDong(hanhDong);
        lichSuHoaDon.setGhiChu(ghiChu);
        lichSuHoaDon.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSuHoaDon);
    }

    private List<TrangThaiDonHangOption> layTrangThaiDonHangOptions() {
        return List.of(
                new TrangThaiDonHangOption(null, "Tất cả"),
                new TrangThaiDonHangOption(0, "Chờ xác nhận"),
                new TrangThaiDonHangOption(1, "Đã xác nhận"),
                new TrangThaiDonHangOption(2, "Chờ giao hàng"),
                new TrangThaiDonHangOption(3, "Đang giao hàng"),
                new TrangThaiDonHangOption(4, "Đã giao hàng"),
                new TrangThaiDonHangOption(5, "Đã hoàn thành"),
                new TrangThaiDonHangOption(6, "Đã hủy"),
                new TrangThaiDonHangOption(7, "Yêu cầu hủy")
        );
    }

    private String cssTrangThaiDonHang(Integer trangThai) {
        if (trangThai == null) {
            return "";
        }
        return switch (trangThai) {
            case 0 -> "status-cho-xac-nhan";
            case 1 -> "status-da-xac-nhan";
            case 2 -> "status-cho-giao-hang";
            case 3 -> "status-dang-giao-hang";
            case 4 -> "status-da-giao-hang";
            case 5 -> "status-da-hoan-thanh";
            case 6 -> "status-da-huy";
            case 7 -> "status-yeu-cau-huy";
            default -> "";
        };
    }

    private boolean daDangNhapKhachHangOnline(HttpSession session) {
        return session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY) instanceof Integer;
    }

    private Optional<KhachHang> layKhachHangOnlineDangNhap(HttpSession session) {
        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (!(khachHangId instanceof Integer id)) {
            return Optional.empty();
        }
        return khachHangRepository.findById(id);
    }

    private void luuKhachHangOnlineVaoSession(HttpSession session, KhachHang khachHang) {
        session.setAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY, khachHang.getId());
        session.setAttribute(KHACH_HANG_ONLINE_TEN_SESSION_KEY, khachHang.getHoTen());
        session.setAttribute(KHACH_HANG_ONLINE_EMAIL_SESSION_KEY, khachHang.getEmail());
    }

    private String taoMaKhachHangMoi() {
        Optional<KhachHang> khachHangCuoi = khachHangRepository.findFirstByOrderByIdDesc();
        int nextId = khachHangCuoi.map(khachHang -> khachHang.getId() + 1).orElse(1);
        return String.format("KH%03d", nextId);
    }

    private String chuanHoaEmail(String value) {
        String text = chuanHoaText(value);
        return StringUtils.hasText(text) ? text.toLowerCase() : text;
    }

    private String chuanHoaSoDienThoai(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    private boolean soDienThoaiHopLe(String value) {
        return StringUtils.hasText(value) && SDT_HOP_LE.matcher(value).matches();
    }

    private boolean emailHopLe(String value) {
        return StringUtils.hasText(value) && EMAIL_HOP_LE.matcher(value).matches();
    }

    private String chuanHoaText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private String gopDiaChiCuTheVaQuanHuyen(String diaChiChiTiet, String quanHuyen) {
        List<String> phanDiaChi = new ArrayList<>();
        if (StringUtils.hasText(diaChiChiTiet)) {
            phanDiaChi.add(chuanHoaText(diaChiChiTiet));
        }
        if (StringUtils.hasText(quanHuyen)) {
            phanDiaChi.add(chuanHoaText(quanHuyen));
        }
        return String.join(", ", phanDiaChi);
    }

    private LocalDate chuyenNgaySinh(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ignored) {
            return null;
        }
    }

    private Integer chuanHoaGioiTinh(Integer gioiTinh) {
        if (gioiTinh == null) {
            return null;
        }
        return gioiTinh == 0 || gioiTinh == 1 ? gioiTinh : null;
    }

    private boolean duLieuDiaChiHopLe(String nguoiNhan, String sdt, String tinhThanh, String phuongXa) {
        return StringUtils.hasText(chuanHoaText(nguoiNhan))
                && StringUtils.hasText(chuanHoaSoDienThoai(sdt))
                && StringUtils.hasText(chuanHoaText(tinhThanh))
                && StringUtils.hasText(chuanHoaText(phuongXa));
    }

    private String kiemTraThongTinDatHangHopLe(ThongTinDatHangOnlineRequest thongTinDatHang) {
        if (thongTinDatHang == null) {
            return "Vui long nhap thong tin giao hang.";
        }
        String sdtDaChuanHoa = chuanHoaSoDienThoai(thongTinDatHang.getSoDienThoai());
        String emailDaChuanHoa = chuanHoaEmail(thongTinDatHang.getEmail());
        thongTinDatHang.setSoDienThoai(sdtDaChuanHoa);
        thongTinDatHang.setEmail(emailDaChuanHoa);

        if (!StringUtils.hasText(chuanHoaText(thongTinDatHang.getHoTen()))
                || !StringUtils.hasText(sdtDaChuanHoa)
                || !StringUtils.hasText(emailDaChuanHoa)
                || !StringUtils.hasText(chuanHoaText(thongTinDatHang.getTinhThanh()))
                || !StringUtils.hasText(chuanHoaText(thongTinDatHang.getQuanHuyen()))
                || !StringUtils.hasText(chuanHoaText(thongTinDatHang.getPhuongXa()))) {
            return "Vui long nhap day du thong tin giao hang.";
        }
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            return "So dien thoai phai bat dau bang 0 va du 10 chu so.";
        }
        if (!emailHopLe(emailDaChuanHoa)) {
            return "Email khong dung dinh dang.";
        }
        return null;
    }

    private void boMacDinhDiaChiOnline(List<DiaChiKhachHang> danhSachDiaChi) {
        danhSachDiaChi.forEach(diaChi -> {
            diaChi.setMacDinh(false);
            diaChi.setNgayCapNhat(LocalDateTime.now());
        });
        diaChiKhachHangRepository.saveAll(danhSachDiaChi);
    }

    private ThongTinDatHangOnlineRequest layThongTinDatHangTuSession(HttpSession session) {
        Object value = session.getAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);
        if (value instanceof ThongTinDatHangOnlineRequest thongTinDatHang) {
            return thongTinDatHang;
        }
        return new ThongTinDatHangOnlineRequest();
    }

    private ThongTinDatHangOnlineRequest layThongTinDatHangChoThanhToan(HttpSession session) {
        ThongTinDatHangOnlineRequest thongTinDatHang = layThongTinDatHangTuSession(session);

        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (!(khachHangId instanceof Integer id)) {
            return thongTinDatHang;
        }

        khachHangRepository.findById(id).ifPresent(khachHang -> {
            if (!StringUtils.hasText(thongTinDatHang.getHoTen())) {
                thongTinDatHang.setHoTen(khachHang.getHoTen());
            }
            if (!StringUtils.hasText(thongTinDatHang.getSoDienThoai())) {
                thongTinDatHang.setSoDienThoai(khachHang.getSdt());
            }
            if (!StringUtils.hasText(thongTinDatHang.getEmail())) {
                thongTinDatHang.setEmail(khachHang.getEmail());
            }
            dienDiaChiMacDinhVaoThongTinDatHang(khachHang.getId(), thongTinDatHang);
        });
        return thongTinDatHang;
    }

    private void luuThongTinDatHangVaoSession(HttpSession session, ThongTinDatHangOnlineRequest thongTinDatHang) {
        if (thongTinDatHang == null) {
            session.removeAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);
            return;
        }

        ThongTinDatHangOnlineRequest duLieuNhap = new ThongTinDatHangOnlineRequest(
                chuanHoaText(thongTinDatHang.getHoTen()),
                chuanHoaSoDienThoai(thongTinDatHang.getSoDienThoai()),
                chuanHoaEmail(thongTinDatHang.getEmail()),
                chuanHoaText(thongTinDatHang.getTinhThanh()),
                chuanHoaText(thongTinDatHang.getQuanHuyen()),
                chuanHoaText(thongTinDatHang.getPhuongXa()),
                chuanHoaText(thongTinDatHang.getDiaChiChiTiet()),
                chuanHoaText(thongTinDatHang.getGhiChu()),
                chuanHoaText(thongTinDatHang.getPhuongThucThanhToan())
        );
        session.setAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY, duLieuNhap);
    }

    private BigDecimal tinhPhiVanChuyenOnline(ThongTinDatHangOnlineRequest thongTinDatHang) {
        String tinhThanhNhan = thongTinDatHang == null ? "" : chuanHoaTenTinhThanh(thongTinDatHang.getTinhThanh());
        if (!StringUtils.hasText(tinhThanhNhan) || TINH_THANH_CUA_HANG.equals(tinhThanhNhan)) {
            return PHI_SHIP_NOI_TINH;
        }
        if (CAC_TINH_MIEN_BAC.contains(tinhThanhNhan)) {
            return PHI_SHIP_NOI_MIEN;
        }
        return PHI_SHIP_LIEN_MIEN;
    }

    private String chuanHoaTenTinhThanh(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String khongDau = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
        return khongDau
                .replace("thanh pho", "")
                .replace("tinh", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

    private void dienDiaChiMacDinhVaoThongTinDatHang(Integer idKhachHang, ThongTinDatHangOnlineRequest thongTinDatHang) {
        if (idKhachHang == null || thongTinDatHang == null) {
            return;
        }

        DiaChiKhachHang diaChiMacDinh = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(idKhachHang)
                .stream()
                .filter(diaChi -> Boolean.TRUE.equals(diaChi.getMacDinh()))
                .findFirst()
                .orElse(null);

        if (diaChiMacDinh == null) {
            return;
        }

        DiaChiCheckout diaChiCheckout = tachDiaChiCheckout(diaChiMacDinh);
        if (!StringUtils.hasText(thongTinDatHang.getTinhThanh())) {
            thongTinDatHang.setTinhThanh(diaChiCheckout.tinhThanh());
        }
        if (!StringUtils.hasText(thongTinDatHang.getQuanHuyen())) {
            thongTinDatHang.setQuanHuyen(diaChiCheckout.quanHuyen());
        }
        if (!StringUtils.hasText(thongTinDatHang.getPhuongXa())) {
            thongTinDatHang.setPhuongXa(diaChiCheckout.phuongXa());
        }
        if (!StringUtils.hasText(thongTinDatHang.getDiaChiChiTiet())) {
            thongTinDatHang.setDiaChiChiTiet(diaChiCheckout.diaChiChiTiet());
        }
    }

    private DiaChiCheckout tachDiaChiCheckout(DiaChiKhachHang diaChi) {
        String tinhThanh = chuanHoaText(diaChi.getTinhThanh());
        String phuongXa = "";
        String quanHuyen = "";
        String diaChiChiTiet = chuanHoaText(diaChi.getDiaChiChiTiet());

        List<String> phanPhuongXa = tachPhanDiaChi(diaChi.getPhuongXa());
        if (!phanPhuongXa.isEmpty()) {
            phuongXa = phanPhuongXa.get(0);
            if (phanPhuongXa.size() > 1) {
                quanHuyen = phanPhuongXa.get(phanPhuongXa.size() - 1);
            }
        }

        List<String> phanChiTiet = tachPhanDiaChi(diaChiChiTiet);
        if (!StringUtils.hasText(quanHuyen) && !phanChiTiet.isEmpty() && laTenQuanHuyen(phanChiTiet.get(phanChiTiet.size() - 1))) {
            quanHuyen = phanChiTiet.get(phanChiTiet.size() - 1);
            diaChiChiTiet = String.join(", ", phanChiTiet.subList(0, phanChiTiet.size() - 1));
        }

        return new DiaChiCheckout(tinhThanh, quanHuyen, phuongXa, diaChiChiTiet);
    }

    private List<String> tachPhanDiaChi(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(this::chuanHoaText)
                .filter(StringUtils::hasText)
                .toList();
    }

    private boolean laTenQuanHuyen(String value) {
        String text = chuanHoaText(value).toLowerCase();
        return text.startsWith("quan ")
                || text.startsWith("quận ")
                || text.startsWith("huyen ")
                || text.startsWith("huyện ")
                || text.startsWith("thanh pho ")
                || text.startsWith("thành phố ")
                || text.startsWith("thi xa ")
                || text.startsWith("thị xã ");
    }

    private record DiaChiCheckout(String tinhThanh, String quanHuyen, String phuongXa, String diaChiChiTiet) {
    }

    private record DonHangCuaToiView(
            HoaDonResponse hoaDon,
            List<ChiTietHoaDonResponse> chiTiet,
            int tongSanPham,
            String statusClass
    ) {
    }

    private record TrangThaiDonHangOption(Integer value, String label) {
    }

    private PhieuGiamGia layPhieuDangApDung(HttpSession session, DuLieuGioHangOnline duLieuGioHang, BigDecimal phiVanChuyen) {
        Object value = session.getAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
        if (!(value instanceof Integer phieuId)) {
            return null;
        }

        PhieuGiamGia phieu = phieuGiamGiaRepository.findById(phieuId).orElse(null);
        if (phieu == null) {
            session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            return null;
        }

        PhieuGiamGia phieuHopLe = timPhieuHopLeTheoMa(phieu.getMaPhieuGiamGia(), session);
        if (phieuHopLe == null) {
            session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            return null;
        }

        BigDecimal giaTriToiThieu = phieuHopLe.getGiaTriDonToiThieu() == null ? BigDecimal.ZERO : phieuHopLe.getGiaTriDonToiThieu();
        if (duLieuGioHang.getTongCong().compareTo(giaTriToiThieu) < 0) {
            session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            return null;
        }

        if (tinhTienGiamVoucher(phieuHopLe, duLieuGioHang, phiVanChuyen).compareTo(BigDecimal.ZERO) <= 0) {
            session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            return null;
        }

        return phieuHopLe;
    }

    private BigDecimal tinhTienGiamVoucher(PhieuGiamGia phieuGiamGia, DuLieuGioHangOnline duLieuGioHang, BigDecimal phiVanChuyen) {
        if (phieuGiamGia == null || duLieuGioHang == null) {
            return BigDecimal.ZERO;
        }
        return tinhTienGiamChoDonHang(phieuGiamGia, duLieuGioHang.getTongCong(), phiVanChuyen);
    }

    private PhieuGiamGia timPhieuHopLeTheoMa(String maPhieuGiamGia, HttpSession session) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.trim().isEmpty()) {
            return null;
        }

        return phieuGiamGiaRepository.findByMaPhieuGiamGia(maPhieuGiamGia.trim())
                .filter(this::laPhieuDangHopLe)
                .filter(phieu -> khachHangOnlineDuocDungPhieu(phieu, session))
                .orElse(null);
    }

    private List<PhieuGiamGia> layDanhSachPhieuCoTheDung(DuLieuGioHangOnline duLieuGioHang, BigDecimal phiVanChuyen, HttpSession session) {
        if (duLieuGioHang == null || duLieuGioHang.isEmpty()) {
            return List.of();
        }

        BigDecimal tongTienHang = duLieuGioHang.getTongCong() == null ? BigDecimal.ZERO : duLieuGioHang.getTongCong();

        return phieuGiamGiaRepository.findPhieuConHieuLuc(LocalDateTime.now()).stream()
                .filter(this::laPhieuDangHopLe)
                .filter(phieu -> khachHangOnlineDuocDungPhieu(phieu, session))
                .filter(phieu -> {
                    BigDecimal giaTriToiThieu = phieu.getGiaTriDonToiThieu() == null
                            ? BigDecimal.ZERO
                            : phieu.getGiaTriDonToiThieu();
                    return tongTienHang.compareTo(giaTriToiThieu) >= 0;
                })
                .filter(phieu -> tinhTienGiamChoDonHang(phieu, tongTienHang, phiVanChuyen).compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    private boolean khachHangOnlineDuocDungPhieu(PhieuGiamGia phieuGiamGia, HttpSession session) {
        if (phieuGiamGia == null || phieuGiamGia.getId() == null) {
            return false;
        }

        boolean laPhieuGanRiengChoKhach = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGia_Id(phieuGiamGia.getId());
        if (!laPhieuGanRiengChoKhach) {
            return true;
        }

        return layIdKhachHangOnlineTuSession(session)
                .flatMap(idKhachHang -> phieuGiamGiaKhachHangRepository.findChuaSuDung(phieuGiamGia.getId(), idKhachHang))
                .isPresent();
    }

    private BigDecimal tinhTienGiamChoDonHang(PhieuGiamGia phieuGiamGia, BigDecimal tongTienHang, BigDecimal phiVanChuyen) {
        if (phieuGiamGia == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal tienHang = tongTienHang == null ? BigDecimal.ZERO : tongTienHang;
        BigDecimal tienShip = phiVanChuyen == null ? BigDecimal.ZERO : phiVanChuyen;
        String loai = phieuGiamGia.getLoaiPhieuGiamGia();

        boolean laPhanTram = "PHAN_TRAM".equalsIgnoreCase(loai)
                || (loai != null && loai.contains("%"));

        if (laPhanTram) {
            BigDecimal giam = tienHang
                    .multiply(phieuGiamGia.getGiaTriGiam())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

            if (phieuGiamGia.getGiaTriGiamToiDa() != null
                    && giam.compareTo(phieuGiamGia.getGiaTriGiamToiDa()) > 0) {
                giam = phieuGiamGia.getGiaTriGiamToiDa();
            }
            return giam.max(BigDecimal.ZERO);
        }

        if ("VAN_CHUYEN".equalsIgnoreCase(loai)) {
            return phieuGiamGia.getGiaTriGiam() == null
                    ? BigDecimal.ZERO
                    : phieuGiamGia.getGiaTriGiam().min(tienShip).max(BigDecimal.ZERO);
        }

        BigDecimal giam = phieuGiamGia.getGiaTriGiam() == null ? BigDecimal.ZERO : phieuGiamGia.getGiaTriGiam();
        if (phieuGiamGia.getGiaTriGiamToiDa() != null
                && giam.compareTo(phieuGiamGia.getGiaTriGiamToiDa()) > 0) {
            giam = phieuGiamGia.getGiaTriGiamToiDa();
        }
        return giam.max(BigDecimal.ZERO);
    }

    private boolean laPhieuDangHopLe(PhieuGiamGia phieuGiamGia) {
        if (phieuGiamGia == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        if (phieuGiamGia.getTrangThai() == null || phieuGiamGia.getTrangThai() != 1) {
            return false;
        }
        if (phieuGiamGia.getNgayBatDau() != null && phieuGiamGia.getNgayBatDau().isAfter(now)) {
            return false;
        }
        if (phieuGiamGia.getNgayKetThuc() != null && phieuGiamGia.getNgayKetThuc().isBefore(now)) {
            return false;
        }
        if (phieuGiamGia.getSoLuong() != null
                && (phieuGiamGia.getSoLuongDaSuDung() == null ? 0 : phieuGiamGia.getSoLuongDaSuDung()) >= phieuGiamGia.getSoLuong()) {
            return false;
        }
        return true;
    }

    private int tinhBuocTheoDoi(Integer trangThai) {
        if (trangThai == null) {
            return 1;
        }
        return switch (trangThai) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 4;
            case 4 -> 5;
            case 5 -> 6;
            default -> 1;
        };
    }

    private HoaDon chuanHoaTrangThaiDonOnlineDatMoi(HoaDon hoaDon) {
        if (hoaDon == null) {
            return null;
        }

        // Sua tuong thich cho cac don online da tao truoc luc fix bug:
        // - la don online
        // - dang bi luu nham trang thai "Da xac nhan"
        // - lich su moi chi co 1 dong "Dat hang online"
        if (!Objects.equals(hoaDon.getLoaiHoaDon(), 1) || !Objects.equals(hoaDon.getTrangThai(), 1)) {
            return hoaDon;
        }

        List<LichSuHoaDon> lichSuHoaDons = lichSuHoaDonRepository.findByHoaDon_IdOrderByThoiGianHanhDongAsc(hoaDon.getId());
        if (lichSuHoaDons.size() != 1) {
            return hoaDon;
        }

        LichSuHoaDon lichSuDauTien = lichSuHoaDons.get(0);
        if (!"Dat hang online".equalsIgnoreCase(lichSuDauTien.getHanhDong())) {
            return hoaDon;
        }

        hoaDon.setTrangThai(0);
        hoaDonRepository.save(hoaDon);

        lichSuDauTien.setTrangThaiCu(0);
        lichSuDauTien.setTrangThaiMoi(0);
        lichSuHoaDonRepository.save(lichSuDauTien);
        return hoaDon;
    }

    @GetMapping("/cua-hang/gioi-thieu")
    public String gioiThieu(Model model) {
        model.addAttribute("pageTitle", "Aerion Sports | Giới thiệu");
        model.addAttribute("activePage", "gioi-thieu");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/gioi-thieu";
    }

    @GetMapping("/cua-hang/lien-he")
    public String lienHe(Model model) {
        model.addAttribute("pageTitle", "Aerion Sports | Liên hệ");
        model.addAttribute("activePage", "lien-he");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/lien-he";
    }
}
