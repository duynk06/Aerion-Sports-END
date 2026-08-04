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
import com.example.AerionSports_BE.entity.LichSuThanhToan;
import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.entity.TaiKhoan;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineChiTietHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineDiaChiKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuHoaDonRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineLichSuThanhToanRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineNhanVienRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaKhachHangRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlinePhieuGiamGiaRepository;
import com.example.AerionSports_BE.repository.banhangonline.BanHangOnlineTaiKhoanRepository;
import com.example.AerionSports_BE.service.BanHangOnlineService;
import com.example.AerionSports_BE.service.BanHangOnlineVnPayService;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
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

    private static final int TRANG_THAI_CHO_THANH_TOAN_VNPAY = 8;
    private static final int TRANG_THAI_CAN_XU_LY_ONLINE = 9;
    private static final String GIO_HANG_ONLINE_SESSION_KEY = "gioHangOnline";
    private static final String PHIEU_GIAM_GIA_ONLINE_SESSION_KEY = "phieuGiamGiaOnlineId";
    private static final String PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY = "phieuGiamGiaOnlineNhapTay";
    private static final String BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY = "boQuaTuDongApPhieuOnline";
    private static final String THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY = "thongTinDatHangOnline";
    private static final String KHACH_HANG_ONLINE_ID_SESSION_KEY = "khachHangOnlineId";
    private static final String KHACH_HANG_ONLINE_TEN_SESSION_KEY = "khachHangOnlineTen";
    private static final String KHACH_HANG_ONLINE_EMAIL_SESSION_KEY = "khachHangOnlineEmail";
    private static final BigDecimal PHI_SHIP_NOI_TINH = BigDecimal.valueOf(22_000);
    private static final BigDecimal PHI_SHIP_NOI_MIEN = BigDecimal.valueOf(30_000);
    private static final BigDecimal PHI_SHIP_LIEN_MIEN = BigDecimal.valueOf(32_000);
    private static final BigDecimal NGUONG_MIEN_PHI_SHIP = BigDecimal.valueOf(5_000_000);
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
     * Controller này là đầu vào chính của phần bán hàng online.
     * Nhiệm vụ của nó là nhận request từ trình duyệt, gọi service/repository lấy dữ liệu thật,
     * đưa dữ liệu vào Model cho Thymeleaf render, rồi điều hướng người dùng sang đúng trang.
     * Những xử lý nhỏ liên quan session, giỏ hàng, đăng nhập khách, voucher và đơn hàng online
     * được đặt ở cuối file dưới dạng hàm helper để phần endpoint phía trên dễ đọc hơn.
     */
    private final BanHangOnlineService banHangOnlineService;
    private final BanHangOnlineVnPayService banHangOnlineVnPayService;
    private final BanHangOnlinePhieuGiamGiaRepository phieuGiamGiaRepository;
    private final BanHangOnlinePhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final BanHangOnlineHoaDonRepository hoaDonRepository;
    private final BanHangOnlineChiTietHoaDonRepository chiTietHoaDonRepository;
    private final BanHangOnlineLichSuHoaDonRepository lichSuHoaDonRepository;
    private final BanHangOnlineLichSuThanhToanRepository lichSuThanhToanRepository;
    private final BanHangOnlineKhachHangRepository khachHangRepository;
    private final BanHangOnlineDiaChiKhachHangRepository diaChiKhachHangRepository;
    private final BanHangOnlineNhanVienRepository nhanVienRepository;
    private final BanHangOnlineTaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Hiển thị trang chủ bán hàng online.
     *
     * <p>Hàm này lấy dữ liệu trang chủ từ service, gồm sản phẩm nổi bật, sản phẩm hero,
     * tổng số thương hiệu và tổng số sản phẩm. Sau đó đẩy dữ liệu sang template
     * {@code ban-hang-online/trang-chu} để Thymeleaf render giao diện.</p>
     */
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

    /**
     * Hiển thị danh sách sản phẩm online có lọc, tìm kiếm, sắp xếp và phân trang.
     *
     * <p>Các tham số trên URL như từ khóa, thương hiệu, trọng lượng, khoảng giá, kiểu sắp xếp
     * và trang hiện tại được chuyển xuống service để lấy dữ liệu thật. Kết quả trả về gồm danh sách
     * sản phẩm, các option bộ lọc và trạng thái bộ lọc đang chọn để giao diện giữ lại lựa chọn của khách.</p>
     */
    @GetMapping("/cua-hang/san-pham")
    public String danhSachSanPham(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "color", required = false) String color,
            @RequestParam(value = "weight", required = false) String weight,
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "stiffness", required = false) String stiffness,
            @RequestParam(value = "balancePoint", required = false) String balancePoint,
            @RequestParam(value = "gripSize", required = false) String gripSize,
            @RequestParam(value = "shaftMaterial", required = false) String shaftMaterial,
            @RequestParam(value = "frameMaterial", required = false) String frameMaterial,
            @RequestParam(value = "minPrice", required = false) Long minPrice,
            @RequestParam(value = "maxPrice", required = false) Long maxPrice,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "price", required = false) String price,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        DuLieuTrangDanhSachSanPhamOnline duLieuDanhSach = banHangOnlineService.layDuLieuDanhSachSanPham(
                keyword, category, brand, color, weight, origin, stiffness, balancePoint, gripSize,
                shaftMaterial, frameMaterial, minPrice, maxPrice, status, price, sort, page
        );

        model.addAttribute("pageTitle", "Aerion Sports | San pham");
        model.addAttribute("activePage", "products");
        model.addAttribute("featuredProducts", duLieuDanhSach.getProducts());

        model.addAttribute("brandOptions", duLieuDanhSach.getBrandOptions());
        model.addAttribute("categoryOptions", duLieuDanhSach.getCategoryOptions());
        model.addAttribute("colorOptions", duLieuDanhSach.getColorOptions());
        model.addAttribute("weightOptions", duLieuDanhSach.getWeightOptions());
        model.addAttribute("originOptions", duLieuDanhSach.getOriginOptions());
        model.addAttribute("stiffnessOptions", duLieuDanhSach.getStiffnessOptions());
        model.addAttribute("balancePointOptions", duLieuDanhSach.getBalancePointOptions());
        model.addAttribute("gripSizeOptions", duLieuDanhSach.getGripSizeOptions());
        model.addAttribute("shaftMaterialOptions", duLieuDanhSach.getShaftMaterialOptions());
        model.addAttribute("frameMaterialOptions", duLieuDanhSach.getFrameMaterialOptions());

        model.addAttribute("selectedKeyword", duLieuDanhSach.getSelectedKeyword());
        model.addAttribute("selectedCategory", duLieuDanhSach.getSelectedCategory());
        model.addAttribute("selectedBrand", duLieuDanhSach.getSelectedBrand());
        model.addAttribute("selectedColor", duLieuDanhSach.getSelectedColor());
        model.addAttribute("selectedWeight", duLieuDanhSach.getSelectedWeight());
        model.addAttribute("selectedOrigin", duLieuDanhSach.getSelectedOrigin());
        model.addAttribute("selectedStiffness", duLieuDanhSach.getSelectedStiffness());
        model.addAttribute("selectedBalancePoint", duLieuDanhSach.getSelectedBalancePoint());
        model.addAttribute("selectedGripSize", duLieuDanhSach.getSelectedGripSize());
        model.addAttribute("selectedShaftMaterial", duLieuDanhSach.getSelectedShaftMaterial());
        model.addAttribute("selectedFrameMaterial", duLieuDanhSach.getSelectedFrameMaterial());
        model.addAttribute("selectedMinPrice", duLieuDanhSach.getSelectedMinPrice());
        model.addAttribute("selectedMaxPrice", duLieuDanhSach.getSelectedMaxPrice());
        model.addAttribute("selectedStatus", duLieuDanhSach.getSelectedStatus());
        model.addAttribute("selectedPrice", duLieuDanhSach.getSelectedPrice());
        model.addAttribute("selectedSort", duLieuDanhSach.getSelectedSort());

        model.addAttribute("currentPage", duLieuDanhSach.getCurrentPage());
        model.addAttribute("totalPages", duLieuDanhSach.getTotalPages());
        model.addAttribute("totalItems", duLieuDanhSach.getTotalItems());
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/danh-sach-san-pham";
    }

    /**
     * Hiển thị trang chi tiết sản phẩm online.
     *
     * <p>Hàm này nhận id sản phẩm cha và có thể nhận thêm id biến thể đang chọn. Service sẽ lấy sản phẩm,
     * danh sách biến thể, biến thể đang chọn và sản phẩm liên quan. Giao diện dùng dữ liệu này để hiển thị
     * ảnh, giá, màu sắc, trọng lượng, tồn kho và nút thêm giỏ/mua ngay.</p>
     */
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

    /**
     * Hiển thị giỏ hàng online hiện tại của khách.
     *
     * <p>Giỏ hàng được lấy từ session nếu khách chưa đăng nhập, hoặc từ kho giỏ hàng theo khách nếu đã đăng nhập.
     * Sau đó controller dựng dữ liệu giỏ hàng đầy đủ để template hiển thị sản phẩm, số lượng, đơn giá,
     * thành tiền và tổng tiền.</p>
     */
    @GetMapping("/cua-hang/gio-hang")
    public String hienThiGioHang(Model model, HttpSession session) {
        DuLieuGioHangOnline duLieuGioHang = taoDuLieuGioHang(session);

        model.addAttribute("pageTitle", "Aerion Sports | Gio hang");
        model.addAttribute("activePage", "gio-hang");
        model.addAttribute("duLieuGioHang", duLieuGioHang);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/gio-hang";
    }

    /**
     * Thêm sản phẩm/biến thể vào giỏ hàng online.
     *
     * <p>Hàm này kiểm tra biến thể có tồn tại không, còn hàng không, số lượng có vượt tồn kho không.
     * Nếu sản phẩm đã có trong giỏ thì cộng dồn số lượng; nếu chưa có thì thêm mới vào session.
     * Sau khi thêm thành công, giỏ hàng được lưu lại và đồng bộ theo khách đăng nhập nếu có.</p>
     */
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
        session.removeAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY);
        redirectAttributes.addFlashAttribute("cartSuccess", "Đã thêm sản phẩm vào giỏ hàng.");
        return "redirect:/cua-hang/san-pham/" + productId + "?variantId=" + variantId;
    }

    /**
     * Xử lý nút mua ngay ở trang chi tiết sản phẩm.
     *
     * <p>Khác với thêm giỏ thông thường, mua ngay sẽ tạo lại giỏ hàng chỉ gồm đúng biến thể khách vừa chọn,
     * validate tồn kho, lưu vào session rồi chuyển thẳng khách sang trang checkout.</p>
     */
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
            redirectAttributes.addFlashAttribute("cartError", "Không tìm thấy biến thể sản phẩm để mua ngay.");
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
        session.removeAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY);
        return "redirect:/cua-hang/checkout";
    }

    /**
     * Cập nhật số lượng một sản phẩm trong giỏ hàng online.
     *
     * <p>Hàm này dùng cho nút cộng/trừ hoặc nhập số lượng trong trang giỏ hàng. Controller kiểm tra item có trong giỏ,
     * số lượng không âm, không vượt tồn kho hiện tại, rồi lưu lại giỏ hàng vào session.</p>
     */
    @PostMapping("/cua-hang/gio-hang/cap-nhat")
    public String capNhatSoLuongGioHang(
            @RequestParam Integer productId,
            @RequestParam Integer variantId,
            @RequestParam Integer soLuong,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        List<MucGioHangOnlineSession> gioHang = layGioHangTuSession(session);
        boolean datSoLuongToiDa = false;
        for (Iterator<MucGioHangOnlineSession> iterator = gioHang.iterator(); iterator.hasNext(); ) {
            MucGioHangOnlineSession item = iterator.next();
            if (Objects.equals(item.getProductId(), productId) && Objects.equals(item.getVariantId(), variantId)) {
                if (soLuong <= 0) {
                    iterator.remove();
                } else {
                    SanPhamBanHangOnlineView bienThe = banHangOnlineService.layBienTheSanPham(productId, variantId);
                    int tonKho = bienThe == null || bienThe.getStock() == null ? 0 : bienThe.getStock();
                    datSoLuongToiDa = soLuong > tonKho;
                    item.setSoLuong(Math.max(1, Math.min(soLuong, tonKho)));
                }
                break;
            }
        }

        luuGioHangVaoSession(session, gioHang);
        if (datSoLuongToiDa) {
            redirectAttributes.addFlashAttribute("cartError", "Sản phẩm đạt số lượng tối đa.");
        } else {
            redirectAttributes.addFlashAttribute("cartSuccess", "Đã cập nhật giỏ hàng.");
        }
        return "redirect:/cua-hang/gio-hang";
    }

    /**
     * Xóa một sản phẩm ra khỏi giỏ hàng online.
     *
     * <p>Frontend gửi id biến thể cần xóa. Controller loại item đó khỏi giỏ hàng session,
     * đồng bộ lại giỏ theo khách đăng nhập và quay về trang giỏ hàng.</p>
     */
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

    /**
     * Hiển thị trang thông tin thanh toán/đặt hàng online.
     *
     * <p>Hàm này kiểm tra giỏ hàng có sản phẩm không, tự điền địa chỉ mặc định nếu khách đã đăng nhập,
     * tính phí vận chuyển, voucher đang áp dụng, danh sách voucher dùng được và tổng tiền cần thanh toán.
     * Nếu giỏ hàng trống thì chuyển khách về giỏ hàng kèm thông báo lỗi.</p>
     */
    @GetMapping("/cua-hang/checkout")
    public String hienThiThanhToan(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        DuLieuGioHangOnline duLieuGioHang = taoDuLieuGioHang(session);
        if (duLieuGioHang.isEmpty()) {
            redirectAttributes.addFlashAttribute("cartError", "Giỏ hàng đang trống, vui lòng thêm sản phẩm trước khi thanh toán.");
            return "redirect:/cua-hang/gio-hang";
        }

        ThongTinDatHangOnlineRequest thongTinDatHang = layThongTinDatHangChoThanhToan(session);
        BigDecimal phiVanChuyen = tinhPhiVanChuyenOnline(thongTinDatHang, duLieuGioHang.getTongCong());
        List<PhieuGiamGia> danhSachPhieuCoTheDung = layDanhSachPhieuCoTheDung(duLieuGioHang, phiVanChuyen, session);
        PhieuGiamGia phieuTotNhat = timPhieuGiamGiaTotNhat(danhSachPhieuCoTheDung, duLieuGioHang, phiVanChuyen);
        PhieuGiamGia phieuDangAp = layPhieuDangApDung(session, duLieuGioHang, phiVanChuyen);
        boolean dangDungMaNhapTay = Boolean.TRUE.equals(session.getAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY));
        boolean boQuaTuDongApPhieu = Boolean.TRUE.equals(session.getAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY));
        if (!boQuaTuDongApPhieu && (phieuDangAp == null || !dangDungMaNhapTay)) {
            phieuDangAp = phieuTotNhat;
            if (phieuDangAp != null) {
                session.setAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY, phieuDangAp.getId());
            } else {
                session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            }
            session.removeAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY);
        } else if (boQuaTuDongApPhieu && phieuDangAp == null) {
            session.removeAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY);
        }
        BigDecimal tienGiamVoucher = tinhTienGiamVoucher(phieuDangAp, duLieuGioHang, phiVanChuyen);
        BigDecimal tongThanhToan = duLieuGioHang.getTongCong().add(phiVanChuyen).subtract(tienGiamVoucher).max(BigDecimal.ZERO);

        model.addAttribute("pageTitle", "Aerion Sports | Thanh toan");
        model.addAttribute("activePage", "checkout");
        model.addAttribute("duLieuGioHang", duLieuGioHang);
        model.addAttribute("phiVanChuyen", phiVanChuyen);
        model.addAttribute("phieuDangAp", phieuDangAp);
        model.addAttribute("phieuTotNhat", phieuTotNhat);
        model.addAttribute("danhSachPhieuCoTheDung", danhSachPhieuCoTheDung);
        model.addAttribute("tienGiamVoucher", tienGiamVoucher);
        model.addAttribute("tongThanhToan", tongThanhToan);
        model.addAttribute("thongTinDatHang", thongTinDatHang);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/thanh-toan";
    }

    /**
     * Áp dụng mã giảm giá ở trang checkout.
     *
     * <p>Thông tin giao hàng hiện tại được lưu vào session trước khi áp mã để tránh bị mất dữ liệu form.
     * Sau đó controller kiểm tra mã có tồn tại, còn hiệu lực, dùng được với khách hiện tại và đủ điều kiện đơn hàng không.
     * Nếu hợp lệ thì lưu id phiếu vào session để trang checkout tính lại tiền giảm.</p>
     */
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
        session.setAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY, true);
        session.removeAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY);
        redirectAttributes.addFlashAttribute("checkoutSuccess", "Đã áp dụng mã giảm giá " + phieu.getMaPhieuGiamGia() + ".");
        return "redirect:/cua-hang/checkout";
    }

    /**
     * Gỡ mã giảm giá đang áp dụng ở trang checkout.
     *
     * <p>Hàm này xóa id voucher khỏi session nhưng vẫn giữ lại thông tin giao hàng khách đã nhập,
     * sau đó redirect về checkout để tính lại tổng tiền không có voucher.</p>
     */
    @PostMapping("/cua-hang/checkout/xoa-phieu")
    public String xoaPhieuGiamGia(
            ThongTinDatHangOnlineRequest thongTinDatHang,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        luuThongTinDatHangVaoSession(session, thongTinDatHang);
        session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
        session.removeAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY);
        session.setAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY, true);
        redirectAttributes.addFlashAttribute("checkoutSuccess", "Đã gỡ mã giảm giá khỏi đơn hàng.");
        return "redirect:/cua-hang/checkout";
    }

    /**
     * Đặt hàng online từ trang checkout.
     *
     * <p>Controller validate thông tin giao hàng, kiểm tra giỏ hàng, lấy voucher đang áp dụng,
     * tính phí vận chuyển, tiền giảm và tổng thanh toán. Sau đó gọi service tạo khách hàng/địa chỉ/hóa đơn/
     * chi tiết hóa đơn thật trong database. Đặt hàng xong sẽ xóa giỏ, xóa voucher session và chuyển sang trang theo dõi đơn.</p>
     */
    @PostMapping("/cua-hang/checkout/dat-hang")
    public String datHangOnline(
            ThongTinDatHangOnlineRequest thongTinDatHang,
            HttpSession session,
            HttpServletRequest request,
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
            redirectAttributes.addFlashAttribute("cartError", "Giỏ hàng đang trống, không thể đặt hàng.");
            return "redirect:/cua-hang/gio-hang";
        }

        BigDecimal phiVanChuyen = tinhPhiVanChuyenOnline(thongTinDatHang, duLieuGioHang.getTongCong());
        List<PhieuGiamGia> danhSachPhieuCoTheDung = layDanhSachPhieuCoTheDung(duLieuGioHang, phiVanChuyen, session);
        PhieuGiamGia phieuTotNhat = timPhieuGiamGiaTotNhat(danhSachPhieuCoTheDung, duLieuGioHang, phiVanChuyen);
        PhieuGiamGia phieuDangAp = layPhieuDangApDung(session, duLieuGioHang, phiVanChuyen);
        boolean dangDungMaNhapTay = Boolean.TRUE.equals(session.getAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY));
        boolean boQuaTuDongApPhieu = Boolean.TRUE.equals(session.getAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY));
        if (!boQuaTuDongApPhieu && (phieuDangAp == null || !dangDungMaNhapTay)) {
            phieuDangAp = phieuTotNhat;
            if (phieuDangAp != null) {
                session.setAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY, phieuDangAp.getId());
            } else {
                session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            }
            session.removeAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY);
        } else if (boQuaTuDongApPhieu && phieuDangAp == null) {
            session.removeAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY);
        }
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

            boolean thanhToanVnPay = laThanhToanVnPay(thongTinDatHang.getPhuongThucThanhToan());
            String urlThanhToanVnPay = thanhToanVnPay
                    ? banHangOnlineVnPayService.taoUrlThanhToan(maHoaDon, request)
                    : null;

            xoaGioHangOnlineTheoKhachDangNhap(session);
            session.removeAttribute(GIO_HANG_ONLINE_SESSION_KEY);
            session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
            session.removeAttribute(PHIEU_GIAM_GIA_NHAP_TAY_ONLINE_SESSION_KEY);
            session.removeAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY);
            session.removeAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);

            if (thanhToanVnPay) {
                return "redirect:" + urlThanhToanVnPay;
            }

            redirectAttributes.addFlashAttribute("checkoutSuccess", "Đặt hàng thành công. Mã hoá đơn của bạn là " + maHoaDon + ".");
            return "redirect:/cua-hang/theo-doi-don-hang?code=" + maHoaDon;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("checkoutError", ex.getMessage());
            return "redirect:/cua-hang/checkout";
        }
    }

    /**
     * Hiển thị form đăng nhập khách hàng online.
     *
     * <p>Nếu khách đã đăng nhập thì không cần vào lại form, controller chuyển luôn sang hồ sơ cá nhân.
     * Nếu chưa đăng nhập thì render template đăng nhập.</p>
     */
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

    @GetMapping("/cua-hang/quen-mat-khau")
    public String hienThiQuenMatKhau(Model model, HttpSession session) {
        if (daDangNhapKhachHangOnline(session)) {
            return "redirect:/cua-hang";
        }
        model.addAttribute("pageTitle", "Aerion Sports | Quen mat khau");
        model.addAttribute("activePage", "auth");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/quen-mat-khau";
    }

    /**
     * Xử lý đăng nhập khách hàng online.
     *
     * <p>Hàm này chuẩn hóa email, kiểm tra định dạng, tìm tài khoản loại khách hàng,
     * kiểm tra mật khẩu bằng PasswordEncoder, lấy hồ sơ khách hàng tương ứng rồi lưu thông tin đăng nhập vào session.
     * Sau đăng nhập, giỏ hàng tạm trong session sẽ được hợp nhất với giỏ hàng đã lưu theo khách.</p>
     */
    @PostMapping("/cua-hang/dang-nhap")
    public String dangNhapKhachHangOnline(
            @RequestParam String email,
            @RequestParam String matKhau,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        String emailDaChuanHoa = chuanHoaEmail(email);
        if (!StringUtils.hasText(emailDaChuanHoa) || !StringUtils.hasText(matKhau)) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng nhập email và mật khẩu.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }
        if (!emailHopLe(emailDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("authError", "Email không đúng định dạng.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        Optional<TaiKhoan> taiKhoanOpt = taiKhoanRepository.findByTenDangNhapAndTrangThai(emailDaChuanHoa, 1);
        if (taiKhoanOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Tài khoản không tồn tại hoặc đã bị khóa.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        TaiKhoan taiKhoan = taiKhoanOpt.get();
        if (!"KHACH_HANG".equalsIgnoreCase(taiKhoan.getLoaiTaiKhoan())) {
            redirectAttributes.addFlashAttribute("authError", "Tài khoản này không phải tài khoản khách hàng online.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        if (!passwordEncoder.matches(matKhau, taiKhoan.getMatKhauHash())) {
            redirectAttributes.addFlashAttribute("authError", "Mật khẩu không chính xác.");
            redirectAttributes.addFlashAttribute("loginEmail", emailDaChuanHoa);
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangRepository.findById(taiKhoan.getIdChuTaiKhoan())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng."));
        session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
        session.removeAttribute(BO_QUA_TU_DONG_AP_PHIEU_ONLINE_SESSION_KEY);
        luuKhachHangOnlineVaoSession(session, khachHang);
        hopNhatGioHangOnlineSauDangNhap(session, khachHang.getId());
        redirectAttributes.addFlashAttribute("authSuccess", "Đăng nhập thành công!");
        return "redirect:/cua-hang";
    }

    /**
     * Hiển thị form đăng ký tài khoản khách hàng online.
     *
     * <p>Nếu khách đã đăng nhập thì chuyển về hồ sơ, còn nếu chưa đăng nhập thì mở template đăng ký.</p>
     */
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

    /**
     * Xử lý đăng ký tài khoản khách hàng online.
     *
     * <p>Hàm này validate họ tên, email, số điện thoại, mật khẩu và xác nhận mật khẩu.
     * Nếu hợp lệ, controller tạo mới khách hàng, tạo tài khoản loại khách hàng, mã hóa mật khẩu,
     * lưu vào database rồi đăng nhập luôn khách vừa đăng ký vào session.</p>
     */
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
            redirectAttributes.addFlashAttribute("authError", "Vui lòng nhập đầy đủ thông tin đăng ký.");
            return "redirect:/cua-hang/dang-ky";
        }
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("authError", "Số điện thoại phải bắt đầu bằng 0 và đủ 10 chữ số.");
            return "redirect:/cua-hang/dang-ky";
        }
        if (!emailHopLe(emailDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("authError", "Email không đúng định dạng.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (matKhau.length() < 6) {
            redirectAttributes.addFlashAttribute("authError", "Mật khẩu phải có ít nhất 6 ký tự.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (!Objects.equals(matKhau, xacNhanMatKhau)) {
            redirectAttributes.addFlashAttribute("authError", "Xác nhận mật khẩu không trùng khớp.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (taiKhoanRepository.findByTenDangNhap(emailDaChuanHoa).isPresent()) {
            redirectAttributes.addFlashAttribute("authError", "Email này đã có tài khoản đăng nhập.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (khachHangRepository.findByEmail(emailDaChuanHoa).isPresent()) {
            redirectAttributes.addFlashAttribute("authError", "Email này đã thuộc về một khách hàng khác.");
            return "redirect:/cua-hang/dang-ky";
        }

        if (khachHangRepository.findBySdt(sdtDaChuanHoa).isPresent()) {
            redirectAttributes.addFlashAttribute("authError", "Số điện thoại này đã thuộc về một khách hàng khác.");
            return "redirect:/cua-hang/dang-ky";
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(taoMaKhachHangMoi());
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang.setDiemTichLuy(0);
        khachHang.setTrangThai(1);
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
        redirectAttributes.addFlashAttribute("authSuccess", "Đăng ký tài khoản thành công!");
        return "redirect:/cua-hang";
    }

    /**
     * Đăng xuất khách hàng online.
     *
     * <p>Trước khi xóa session đăng nhập, controller lưu lại giỏ hàng hiện tại theo id khách hàng
     * để khi đăng nhập lại vẫn giữ được sản phẩm đã thêm trước đó.</p>
     */
    @PostMapping("/cua-hang/dang-xuat")
    public String dangXuatKhachHangOnline(HttpSession session, RedirectAttributes redirectAttributes) {
        luuGioHangVaoKhoTheoKhachDangNhap(session);
        session.removeAttribute(GIO_HANG_ONLINE_SESSION_KEY);
        session.removeAttribute(PHIEU_GIAM_GIA_ONLINE_SESSION_KEY);
        session.removeAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);
        session.removeAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        session.removeAttribute(KHACH_HANG_ONLINE_TEN_SESSION_KEY);
        session.removeAttribute(KHACH_HANG_ONLINE_EMAIL_SESSION_KEY);
        redirectAttributes.addFlashAttribute("authSuccess", "Đã đăng xuất.");
        return "redirect:/cua-hang";
    }

    /**
     * Hiển thị hồ sơ cá nhân của khách hàng online.
     *
     * <p>Hàm này bắt buộc khách phải đăng nhập. Sau đó lấy thông tin khách hàng, danh sách địa chỉ nhận hàng,
     * địa chỉ mặc định và các thông báo cập nhật để render trang hồ sơ.</p>
     */
    @GetMapping("/cua-hang/ho-so")
    public String hienThiHoSo(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (!(khachHangId instanceof Integer id)) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để xem hồ sơ cá nhân.");
            return "redirect:/cua-hang/dang-nhap";
        }

        Optional<KhachHang> khachHangOptional = khachHangRepository.findById(id);
        if (khachHangOptional.isEmpty()) {
            session.removeAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
            session.removeAttribute(KHACH_HANG_ONLINE_TEN_SESSION_KEY);
            session.removeAttribute(KHACH_HANG_ONLINE_EMAIL_SESSION_KEY);
            redirectAttributes.addFlashAttribute("authError", "Không tìm thấy thông tin khách hàng. Vui lòng đăng nhập lại.");
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangOptional.get();
        List<DiaChiKhachHang> danhSachDiaChi = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(id);
        DiaChiKhachHang diaChiMacDinh = danhSachDiaChi.stream()
                .filter(diaChi -> Boolean.TRUE.equals(diaChi.getMacDinh()))
                .findFirst()
                .orElse(danhSachDiaChi.isEmpty() ? null : danhSachDiaChi.get(0));
        model.addAttribute("activePage", "profile");
        model.addAttribute("pageTitle", "Hồ sơ của tôi | Aerion Sports");
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("diaChiMacDinh", diaChiMacDinh);
        model.addAttribute("danhSachDiaChi", danhSachDiaChi);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/ho-so";
    }

    /**
     * Cập nhật thông tin cá nhân trong hồ sơ khách hàng.
     *
     * <p>Khách được sửa họ tên, số điện thoại, giới tính và ngày sinh. Email đang dùng làm tài khoản nên không sửa ở đây.
     * Controller validate số điện thoại/ngày sinh, cập nhật khách hàng trong database rồi refresh lại thông tin session.</p>
     */
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
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để cập nhật hồ sơ.");
            return "redirect:/cua-hang/dang-nhap";
        }

        String hoTenDaChuanHoa = chuanHoaText(hoTen);
        String sdtDaChuanHoa = chuanHoaSoDienThoai(sdt);
        if (!StringUtils.hasText(hoTenDaChuanHoa) || !StringUtils.hasText(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("profileError", "Vui lòng nhập họ tên và số điện thoại.");
            return "redirect:/cua-hang/ho-so";
        }
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            redirectAttributes.addFlashAttribute("profileError", "Số điện thoại phải bắt đầu bằng 0 và đủ 10 chữ số.");
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

        redirectAttributes.addFlashAttribute("profileSuccess", "Đã cập nhật thông tin cá nhân thành công!");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi")
    @Transactional
    public String themDiaChiHoSo(
            @RequestParam String nguoiNhan,
            @RequestParam String sdt,
            @RequestParam String tinhThanh,
            @RequestParam(required = false) String quanHuyen,
            @RequestParam String phuongXa,
            @RequestParam(required = false) String diaChiChiTiet,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để thêm địa chỉ.");
            return "redirect:/cua-hang/dang-nhap";
        }

        String loi = kiemTraDiaChiHoSo(nguoiNhan, sdt, tinhThanh, phuongXa);
        if (loi != null) {
            redirectAttributes.addFlashAttribute("profileError", loi);
            return "redirect:/cua-hang/ho-so";
        }

        KhachHang khachHang = khachHangOptional.get();
        List<DiaChiKhachHang> diaChiHienCo = diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(khachHang.getId());
        DiaChiKhachHang diaChi = new DiaChiKhachHang();
        diaChi.setKhachHang(khachHang);
        ganThongTinDiaChi(diaChi, nguoiNhan, sdt, tinhThanh, quanHuyen, phuongXa, diaChiChiTiet);
        diaChi.setMacDinh(diaChiHienCo.isEmpty());
        diaChi.setNgayTao(LocalDateTime.now());
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiKhachHangRepository.save(diaChi);

        redirectAttributes.addFlashAttribute("profileSuccess", "Đã thêm địa chỉ mới.");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi/{idDiaChi}")
    @Transactional
    public String capNhatDiaChiHoSo(
            @PathVariable Integer idDiaChi,
            @RequestParam String nguoiNhan,
            @RequestParam String sdt,
            @RequestParam String tinhThanh,
            @RequestParam(required = false) String quanHuyen,
            @RequestParam String phuongXa,
            @RequestParam(required = false) String diaChiChiTiet,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để sửa địa chỉ.");
            return "redirect:/cua-hang/dang-nhap";
        }

        Optional<DiaChiKhachHang> diaChiOptional = layDiaChiCuaKhach(idDiaChi, khachHangOptional.get().getId());
        if (diaChiOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "Không tìm thấy địa chỉ của bạn.");
            return "redirect:/cua-hang/ho-so";
        }
        String loi = kiemTraDiaChiHoSo(nguoiNhan, sdt, tinhThanh, phuongXa);
        if (loi != null) {
            redirectAttributes.addFlashAttribute("profileError", loi);
            return "redirect:/cua-hang/ho-so";
        }

        DiaChiKhachHang diaChi = diaChiOptional.get();
        ganThongTinDiaChi(diaChi, nguoiNhan, sdt, tinhThanh, quanHuyen, phuongXa, diaChiChiTiet);
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiKhachHangRepository.save(diaChi);

        redirectAttributes.addFlashAttribute("profileSuccess", "Đã cập nhật địa chỉ.");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi/mac-dinh/{idDiaChi}")
    @Transactional
    public String datDiaChiMacDinhHoSo(
            @PathVariable Integer idDiaChi,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để đổi địa chỉ mặc định.");
            return "redirect:/cua-hang/dang-nhap";
        }

        Optional<DiaChiKhachHang> diaChiOptional = layDiaChiCuaKhach(idDiaChi, khachHangOptional.get().getId());
        if (diaChiOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "Không tìm thấy địa chỉ của bạn.");
            return "redirect:/cua-hang/ho-so";
        }

        diaChiKhachHangRepository.boMacDinhTheoKhachHang(khachHangOptional.get().getId());
        DiaChiKhachHang diaChi = diaChiOptional.get();
        diaChi.setMacDinh(true);
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiKhachHangRepository.save(diaChi);

        redirectAttributes.addFlashAttribute("profileSuccess", "Đã đặt địa chỉ mặc định.");
        return "redirect:/cua-hang/ho-so";
    }

    @PostMapping("/cua-hang/ho-so/dia-chi/xoa/{idDiaChi}")
    @Transactional
    public String xoaDiaChiHoSo(
            @PathVariable Integer idDiaChi,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để xóa địa chỉ.");
            return "redirect:/cua-hang/dang-nhap";
        }

        Integer idKhachHang = khachHangOptional.get().getId();
        Optional<DiaChiKhachHang> diaChiOptional = layDiaChiCuaKhach(idDiaChi, idKhachHang);
        if (diaChiOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("profileError", "Không tìm thấy địa chỉ của bạn.");
            return "redirect:/cua-hang/ho-so";
        }

        boolean laMacDinh = Boolean.TRUE.equals(diaChiOptional.get().getMacDinh());
        diaChiKhachHangRepository.delete(diaChiOptional.get());
        if (laMacDinh) {
            diaChiKhachHangRepository.findByKhachHang_IdOrderByMacDinhDescNgayCapNhatDesc(idKhachHang).stream()
                    .findFirst()
                    .ifPresent(diaChiConLai -> {
                        diaChiConLai.setMacDinh(true);
                        diaChiConLai.setNgayCapNhat(LocalDateTime.now());
                        diaChiKhachHangRepository.save(diaChiConLai);
                    });
        }

        redirectAttributes.addFlashAttribute("profileSuccess", "Đã xóa địa chỉ.");
        return "redirect:/cua-hang/ho-so";
    }
    /**
     * Hiển thị trang đổi mật khẩu khách hàng online.
     */
    @GetMapping("/cua-hang/doi-mat-khau")
    public String hienThiTrangDoiMatKhau(Model model, HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để đổi mật khẩu.");
            return "redirect:/cua-hang/dang-nhap";
        }

        KhachHang khachHang = khachHangOptional.get();
        model.addAttribute("activePage", "doi-mat-khau");
        model.addAttribute("pageTitle", "Đổi mật khẩu | Aerion Sports");
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/doi-mat-khau";
    }

    @PostMapping({"/cua-hang/doi-mat-khau", "/cua-hang/ho-so/doi-mat-khau"})
    public String doiMatKhauKhachHangOnline(
            @RequestParam String matKhauCu,
            @RequestParam String matKhauMoi,
            @RequestParam String xacNhanMatKhau,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để đổi mật khẩu.");
            return "redirect:/cua-hang/dang-nhap";
        }

        if (!StringUtils.hasText(matKhauCu) || !StringUtils.hasText(matKhauMoi) || !StringUtils.hasText(xacNhanMatKhau)) {
            redirectAttributes.addFlashAttribute("passwordError", "Vui lòng nhập đầy đủ thông tin đổi mật khẩu.");
            return "redirect:/cua-hang/doi-mat-khau";
        }
        if (matKhauMoi.length() < 6) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return "redirect:/cua-hang/doi-mat-khau";
        }
        if (!Objects.equals(matKhauMoi, xacNhanMatKhau)) {
            redirectAttributes.addFlashAttribute("passwordError", "Xác nhận mật khẩu mới không trùng khớp.");
            return "redirect:/cua-hang/doi-mat-khau";
        }

        KhachHang khachHang = khachHangOptional.get();
        Optional<TaiKhoan> taiKhoanOptional = taiKhoanRepository.findByTenDangNhapAndTrangThai(khachHang.getEmail(), 1);
        if (taiKhoanOptional.isEmpty()
                || !"KHACH_HANG".equalsIgnoreCase(taiKhoanOptional.get().getLoaiTaiKhoan())
                || !Objects.equals(taiKhoanOptional.get().getIdChuTaiKhoan(), khachHang.getId())) {
            redirectAttributes.addFlashAttribute("passwordError", "Không tìm thấy tài khoản khách hàng.");
            return "redirect:/cua-hang/doi-mat-khau";
        }

        TaiKhoan taiKhoan = taiKhoanOptional.get();
        if (!passwordEncoder.matches(matKhauCu, taiKhoan.getMatKhauHash())) {
            redirectAttributes.addFlashAttribute("passwordError", "Mật khẩu hiện tại không chính xác.");
            return "redirect:/cua-hang/doi-mat-khau";
        }

        taiKhoan.setMatKhauHash(passwordEncoder.encode(matKhauMoi));
        taiKhoan.setNgayCapNhat(LocalDateTime.now());
        taiKhoanRepository.save(taiKhoan);

        redirectAttributes.addFlashAttribute("passwordSuccess", "Đã đổi mật khẩu thành công!");
        return "redirect:/cua-hang/doi-mat-khau";
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
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để xem đơn hàng của tôi.");
            return "redirect:/cua-hang/dang-nhap";
        }

        String keywordDaChuanHoa = StringUtils.hasText(keyword) ? keyword.trim() : null;
        List<DonHangCuaToiView> danhSachDonHang = hoaDonRepository
                .findDonHangOnlineTheoKhachHang(khachHangOptional.get().getId(), keywordDaChuanHoa, trangThai)
                .stream()
                .map(this::taoDonHangCuaToiView)
                .toList();

        model.addAttribute("pageTitle", "Aerion Sports | Đơn hàng của tôi");
        model.addAttribute("activePage", "orders");
        model.addAttribute("keyword", keywordDaChuanHoa);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("trangThaiOptions", layTrangThaiDonHangOptions());
        model.addAttribute("danhSachDonHang", danhSachDonHang);
        model.addAttribute("donHangDangChon", chonDonHangDangXem(danhSachDonHang, code));
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/don-hang-cua-toi";
    }


    @GetMapping("/cua-hang/vnpay/return")
    public String xuLyKetQuaVnPay(
            @RequestParam Map<String, String> params,
            RedirectAttributes redirectAttributes
    ) {
        try {
            String maHoaDon = banHangOnlineVnPayService.xuLyKetQuaThanhToan(params);
            redirectAttributes.addFlashAttribute("orderSuccess", "Đã nhận kết quả thanh toán VNPay cho đơn " + maHoaDon + ".");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + maHoaDon;
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("orderError", ex.getMessage());
            return "redirect:/cua-hang/don-hang-cua-toi";
        }
    }

    @GetMapping("/cua-hang/vnpay/ipn")
    @ResponseBody
    public Map<String, String> xuLyIpnVnPay(@RequestParam Map<String, String> params) {
        try {
            banHangOnlineVnPayService.xuLyKetQuaThanhToan(params);
            return Map.of("RspCode", "00", "Message", "Confirm Success");
        } catch (RuntimeException ex) {
            return Map.of("RspCode", "99", "Message", ex.getMessage());
        }
    }

    @PostMapping("/cua-hang/don-hang-cua-toi/{maHoaDon}/thanh-toan-lai")
    @Transactional
    public String thanhToanLaiVnPay(
            @PathVariable String maHoaDon,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để thanh toán lại.");
            return "redirect:/cua-hang/dang-nhap";
        }
        try {
            banHangOnlineVnPayService.lamMoiThanhToanChoDon(maHoaDon, khachHangOptional.get().getId());
            return "redirect:" + banHangOnlineVnPayService.taoUrlThanhToan(maHoaDon, request);
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("orderError", ex.getMessage());
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + maHoaDon;
        }
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
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để hủy đơn hàng.");
            return "redirect:/cua-hang/dang-nhap";
        }

        HoaDon hoaDon = layDonHangOnlineCuaKhach(maHoaDon, khachHangOptional.get().getId())
                .orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("orderError", "Không tìm thấy đơn hàng của bạn.");
            return "redirect:/cua-hang/don-hang-cua-toi";
        }

        if (!Objects.equals(hoaDon.getTrangThai(), 0) && !Objects.equals(hoaDon.getTrangThai(), TRANG_THAI_CHO_THANH_TOAN_VNPAY)) {
            redirectAttributes.addFlashAttribute("orderError", "Chỉ đơn chờ xác nhận hoặc chờ thanh toán mới được hủy.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        Integer trangThaiCu = hoaDon.getTrangThai();
        hoaDon.setTrangThai(6);
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat("Khách hàng online");
        hoaDonRepository.save(hoaDon);
        if (!Objects.equals(trangThaiCu, TRANG_THAI_CHO_THANH_TOAN_VNPAY)) {
            hoanLuotSuDungPhieuGiamGia(hoaDon);
        }

        luuLichSuDonHangOnline(hoaDon, trangThaiCu, 6, "Khách hàng hủy đơn", "Khách hàng hủy đơn khi đơn đang chờ xác nhận.");
        redirectAttributes.addFlashAttribute("orderSuccess", "Đã hủy đơn hàng " + hoaDon.getMaHoaDon() + ".");
        return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
    }

    @PostMapping("/cua-hang/don-hang-cua-toi/{maHoaDon}/cap-nhat-giao-hang")
    @Transactional
    public String capNhatGiaoHangDonHangCuaToi(
            @PathVariable String maHoaDon,
            @RequestParam String tenNguoiNhan,
            @RequestParam String sdtNguoiNhan,
            @RequestParam(required = false) String tinhThanh,
            @RequestParam(required = false) String quanHuyen,
            @RequestParam(required = false) String phuongXa,
            @RequestParam(required = false) String diaChiChiTiet,
            @RequestParam(required = false) String diaChiNhan,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Optional<KhachHang> khachHangOptional = layKhachHangOnlineDangNhap(session);
        if (khachHangOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("authError", "Vui lòng đăng nhập để thực hiện.");
            return "redirect:/cua-hang/dang-nhap";
        }

        HoaDon hoaDon = layDonHangOnlineCuaKhach(maHoaDon, khachHangOptional.get().getId())
                .orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("orderError", "Không tìm thấy đơn hàng của bạn.");
            return "redirect:/cua-hang/don-hang-cua-toi";
        }

        if (!Objects.equals(hoaDon.getTrangThai(), 0)) {
            redirectAttributes.addFlashAttribute("orderError", "Chỉ được cập nhật thông tin khi đơn hàng ở trạng thái Chờ xác nhận.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        if (!StringUtils.hasText(tenNguoiNhan)) {
            redirectAttributes.addFlashAttribute("orderError", "Tên người nhận không được để trống.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        if (!StringUtils.hasText(sdtNguoiNhan)) {
            redirectAttributes.addFlashAttribute("orderError", "Số điện thoại nhận không được để trống.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        List<String> phanDiaChi = new ArrayList<>();
        if (StringUtils.hasText(diaChiChiTiet)) phanDiaChi.add(diaChiChiTiet.trim());
        if (StringUtils.hasText(phuongXa)) phanDiaChi.add(phuongXa.trim());
        if (StringUtils.hasText(quanHuyen)) phanDiaChi.add(quanHuyen.trim());
        if (StringUtils.hasText(tinhThanh)) phanDiaChi.add(tinhThanh.trim());

        String diaChiDayDu = "";
        if (!phanDiaChi.isEmpty()) {
            diaChiDayDu = String.join(", ", phanDiaChi);
        } else if (StringUtils.hasText(diaChiNhan)) {
            diaChiDayDu = diaChiNhan.trim();
        }

        if (!StringUtils.hasText(diaChiDayDu)) {
            redirectAttributes.addFlashAttribute("orderError", "Vui lòng chọn hoặc nhập đầy đủ địa chỉ giao hàng.");
            return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
        }

        hoaDon.setTenNguoiNhan(tenNguoiNhan.trim());
        hoaDon.setSdtNguoiNhan(sdtNguoiNhan.trim());
        hoaDon.setDiaChiNhan(diaChiDayDu);
        hoaDon.setNgayCapNhat(LocalDateTime.now());
        hoaDon.setNguoiCapNhat("Khách hàng online");
        hoaDonRepository.save(hoaDon);

        luuLichSuDonHangOnline(hoaDon, 0, 0, "Cập nhật thông tin giao hàng", "Khách hàng cập nhật lại người nhận, SĐT và địa chỉ giao hàng.");
        redirectAttributes.addFlashAttribute("orderSuccess", "Đã cập nhật thông tin giao hàng cho đơn " + hoaDon.getMaHoaDon() + ".");
        return "redirect:/cua-hang/don-hang-cua-toi?code=" + hoaDon.getMaHoaDon();
    }

    /**
     * Hiển thị trang theo dõi đơn hàng theo mã hóa đơn.
     *
     * <p>Khách có thể nhập mã hóa đơn hoặc được redirect từ đặt hàng thành công. Controller tra cứu hóa đơn thật,
     * chi tiết sản phẩm thật, chuẩn hóa trạng thái đơn mới tạo nếu cần rồi đưa lên template theo dõi đơn hàng.</p>
     */
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

    /**
     * Dựng dữ liệu giỏ hàng đầy đủ để đưa lên giao diện.
     *
     * <p>Trong session chỉ lưu id sản phẩm, id biến thể và số lượng để nhẹ dữ liệu.
     * Hàm này lấy lại thông tin thật của từng biến thể từ service, tính đơn giá, giá gốc,
     * thành tiền, tiền giảm, tổng số lượng và tổng tiền. Nếu tồn kho thay đổi thì số lượng
     * trong giỏ cũng được kẹp lại để không vượt quá tồn hiện tại.</p>
     */
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

    /**
     * Lấy giỏ hàng đang lưu trong session.
     *
     * <p>Nếu session đã có giỏ thì trả về giỏ đó. Nếu chưa có mà khách đã đăng nhập,
     * hàm sẽ lấy giỏ hàng đã lưu theo id khách trong bộ nhớ tạm và gắn lại vào session.
     * Nếu khách chưa đăng nhập thì tạo giỏ rỗng.</p>
     */
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

    /**
     * Lưu giỏ hàng vào session và đồng bộ theo khách đăng nhập.
     *
     * <p>Session dùng để giữ giỏ trong phiên hiện tại. Nếu khách đã đăng nhập,
     * controller còn lưu thêm bản sao vào map theo id khách để khi đăng xuất/đăng nhập lại
     * trong lúc chạy app thì giỏ vẫn còn.</p>
     */
    private void luuGioHangVaoSession(HttpSession session, List<MucGioHangOnlineSession> gioHang) {
        session.setAttribute(GIO_HANG_ONLINE_SESSION_KEY, gioHang);
        layIdKhachHangOnlineTuSession(session).ifPresent(idKhachHang ->
                GIO_HANG_ONLINE_THEO_KHACH.put(idKhachHang, saoChepGioHang(gioHang)));
    }

    /**
     * Lấy id khách hàng online đang đăng nhập từ session.
     *
     * <p>Trả về Optional để các hàm gọi phía sau dễ xử lý trường hợp khách chưa đăng nhập
     * mà không phải tự kiểm tra null nhiều lần.</p>
     */
    private Optional<Integer> layIdKhachHangOnlineTuSession(HttpSession session) {
        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (khachHangId instanceof Integer id) {
            return Optional.of(id);
        }
        return Optional.empty();
    }

    /**
     * Lấy bản sao giỏ hàng trong session.
     *
     * <p>Hàm này dùng khi cần giữ lại giỏ hiện tại trước thao tác đăng nhập/đăng xuất.
     * Việc trả về bản sao giúp tránh sửa trực tiếp danh sách đang nằm trong session.</p>
     */
    @SuppressWarnings("unchecked")
    private List<MucGioHangOnlineSession> layBanSaoGioHangTrongSession(HttpSession session) {
        Object value = session.getAttribute(GIO_HANG_ONLINE_SESSION_KEY);
        if (value instanceof List<?> list) {
            return saoChepGioHang((List<MucGioHangOnlineSession>) list);
        }
        return new ArrayList<>();
    }

    /**
     * Tạo bản sao độc lập của danh sách giỏ hàng.
     *
     * <p>Mỗi item được clone sang object mới để tránh tình trạng một nơi sửa số lượng
     * làm thay đổi luôn dữ liệu ở nơi khác.</p>
     */
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

    /**
     * Hợp nhất giỏ hàng tạm trước đăng nhập với giỏ hàng đã lưu theo khách.
     *
     * <p>Khi khách thêm hàng lúc chưa đăng nhập rồi mới đăng nhập, hàm này sẽ không làm mất giỏ tạm.
     * Nó cộng dồn các sản phẩm giống nhau vào giỏ đã lưu theo khách và ghi kết quả lại vào session.</p>
     */
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

    /**
     * Gộp hai danh sách giỏ hàng thành một danh sách duy nhất.
     *
     * <p>Nếu hai item có cùng id biến thể thì cộng số lượng. Nếu khác biến thể thì thêm mới.
     * Hàm này chỉ xử lý dữ liệu trong bộ nhớ, chưa kiểm tra tồn kho; tồn kho được kiểm tra lại khi dựng giỏ.</p>
     */
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

    /**
     * Lưu giỏ hàng hiện tại vào kho tạm theo khách đang đăng nhập.
     *
     * <p>Hàm này thường dùng trước khi đăng xuất để giữ lại giỏ hàng của khách,
     * tránh việc thoát tài khoản là mất các sản phẩm đã thêm.</p>
     */
    private void luuGioHangVaoKhoTheoKhachDangNhap(HttpSession session) {
        layIdKhachHangOnlineTuSession(session).ifPresent(idKhachHang ->
                GIO_HANG_ONLINE_THEO_KHACH.put(idKhachHang, layBanSaoGioHangTrongSession(session)));
    }

    /**
     * Xóa giỏ hàng đã lưu theo khách đăng nhập.
     *
     * <p>Dùng sau khi đặt hàng thành công để giỏ hàng của khách sạch cả trong session
     * lẫn kho tạm theo id khách.</p>
     */
    private void xoaGioHangOnlineTheoKhachDangNhap(HttpSession session) {
        layIdKhachHangOnlineTuSession(session).ifPresent(GIO_HANG_ONLINE_THEO_KHACH::remove);
    }

    /**
     * Chuyển entity hóa đơn thành dữ liệu hiển thị cho màn "Đơn hàng của tôi".
     *
     * <p>Hàm này gom hóa đơn, danh sách chi tiết sản phẩm và bước trạng thái hiện tại
     * vào một view object để Thymeleaf render dễ hơn.</p>
     */
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
        Optional<LichSuThanhToan> thanhToanGanNhat = lichSuThanhToanRepository.findFirstByHoaDon_IdOrderByNgayThanhToanDesc(hoaDonDaChuanHoa.getId());
        String phuongThucThanhToan = thanhToanGanNhat
                .map(LichSuThanhToan::getPhuongThucThanhToan)
                .filter(StringUtils::hasText)
                .orElse("COD");
        String trangThaiThanhToan = thanhToanGanNhat
                .map(LichSuThanhToan::getTrangThaiThanhToan)
                .filter(StringUtils::hasText)
                .orElseGet(() -> daThanhToanTheoTrangThaiDon(response.getTrangThai()) ? "Đã thanh toán" : "Chưa thanh toán");
        boolean daThanhToan = laTrangThaiDaThanhToan(trangThaiThanhToan)
                || (!thanhToanGanNhat.isPresent() && daThanhToanTheoTrangThaiDon(response.getTrangThai()));
        return new DonHangCuaToiView(
                response,
                chiTiet,
                tongSanPham,
                cssTrangThaiDonHang(response.getTrangThai()),
                phuongThucThanhToan,
                trangThaiThanhToan,
                daThanhToan
        );
    }

    /**
     * Chuẩn hóa các trường tiền trong response hóa đơn.
     *
     * <p>Một số hóa đơn cũ có thể bị null tiền hàng, tiền giảm, phí ship hoặc tổng thanh toán.
     * Hàm này đưa các giá trị null về 0 để giao diện không lỗi khi format số.</p>
     */
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

    /**
     * Chọn đơn hàng đang được mở chi tiết trên màn "Đơn hàng của tôi".
     *
     * <p>Nếu URL có mã hóa đơn thì ưu tiên chọn đúng đơn đó. Nếu không có hoặc không tìm thấy,
     * hàm chọn đơn đầu tiên trong danh sách để giao diện luôn có dữ liệu hiển thị.</p>
     */
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

    /**
     * Tìm một đơn online theo mã hóa đơn và kiểm tra đơn đó thuộc đúng khách đang đăng nhập.
     *
     * <p>Đây là lớp bảo vệ để khách không thể sửa/hủy đơn của người khác chỉ bằng cách đoán mã đơn.</p>
     */
    private Optional<HoaDon> layDonHangOnlineCuaKhach(String maHoaDon, Integer idKhachHang) {
        if (!StringUtils.hasText(maHoaDon) || idKhachHang == null) {
            return Optional.empty();
        }
        return hoaDonRepository.findByMaHoaDonWithThongTin(maHoaDon.trim())
                .filter(hoaDon -> Objects.equals(hoaDon.getLoaiHoaDon(), 1))
                .filter(hoaDon -> hoaDon.getKhachHang() != null)
                .filter(hoaDon -> Objects.equals(hoaDon.getKhachHang().getId(), idKhachHang));
    }

    /**
     * Lưu lịch sử thao tác cho đơn hàng online.
     *
     * <p>Mỗi lần khách hủy đơn, sửa thông tin giao hàng hoặc hệ thống cần ghi nhận thay đổi trạng thái,
     * hàm này tạo một dòng trong bảng lịch sử hóa đơn. Nhân viên mặc định được dùng để thỏa khóa ngoại
     * vì thao tác phát sinh từ khách online chứ không phải nhân viên trực tiếp.</p>
     */
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên mặc định để lưu lịch sử hóa đơn."));
        lichSuHoaDon.setNhanVien(nhanVienMacDinh);
        lichSuHoaDon.setTrangThaiCu(trangThaiCu);
        lichSuHoaDon.setTrangThaiMoi(trangThaiMoi);
        lichSuHoaDon.setHanhDong(hanhDong);
        lichSuHoaDon.setGhiChu(ghiChu);
        lichSuHoaDon.setThoiGianHanhDong(LocalDateTime.now());
        lichSuHoaDonRepository.save(lichSuHoaDon);
    }

    /**
     * Tạo danh sách trạng thái cho bộ lọc đơn hàng của khách.
     *
     * <p>Danh sách này được render lên dropdown/tab lọc trong màn "Đơn hàng của tôi".</p>
     */
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
                new TrangThaiDonHangOption(8, "Chờ thanh toán"),
                new TrangThaiDonHangOption(9, "Cần xử lý")
        );
    }

    private boolean daThanhToanTheoTrangThaiDon(Integer trangThai) {
        return trangThai != null && trangThai >= 4 && trangThai != 6 && trangThai != 7;
    }

    private boolean laTrangThaiDaThanhToan(String trangThaiThanhToan) {
        String giaTri = chuanHoaText(trangThaiThanhToan).toLowerCase(Locale.ROOT);
        return giaTri.contains("đã thanh toán") || giaTri.contains("da thanh toan");
    }
    /**
     * Chọn class CSS tương ứng với trạng thái đơn hàng.
     *
     * <p>Template dùng class này để tô màu badge trạng thái như chờ xác nhận, đang giao,
     * đã hoàn thành hoặc đã hủy.</p>
     */

    private boolean laThanhToanVnPay(String phuongThucThanhToan) {
        String giaTri = chuanHoaText(phuongThucThanhToan);
        return "VNPAY".equalsIgnoreCase(giaTri) || "BANK".equalsIgnoreCase(giaTri);
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
            case TRANG_THAI_CHO_THANH_TOAN_VNPAY -> "status-cho-thanh-toan";
            case TRANG_THAI_CAN_XU_LY_ONLINE -> "status-can-xu-ly";
            default -> "";
        };
    }

    /**
     * Kiểm tra khách hàng online đã đăng nhập hay chưa.
     *
     * <p>Chỉ cần session có id khách hàng là xem như đã đăng nhập.
     * Các trang cần bảo vệ như hồ sơ và đơn hàng của tôi sẽ dùng hàm này để chặn khách chưa đăng nhập.</p>
     */
    private boolean daDangNhapKhachHangOnline(HttpSession session) {
        return session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY) instanceof Integer;
    }

    /**
     * Lấy thông tin khách hàng đang đăng nhập từ database.
     *
     * <p>Session chỉ giữ id khách hàng để nhẹ dữ liệu. Khi cần thông tin mới nhất,
     * hàm này đọc lại entity khách hàng từ repository và trả về Optional để tránh lỗi null.</p>
     */
    private Optional<KhachHang> layKhachHangOnlineDangNhap(HttpSession session) {
        Object khachHangId = session.getAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY);
        if (!(khachHangId instanceof Integer id)) {
            return Optional.empty();
        }
        return khachHangRepository.findById(id);
    }

    /**
     * Lưu thông tin khách hàng online vào session sau khi đăng nhập hoặc đăng ký.
     *
     * <p>Session sẽ giữ id, tên và email để header hiển thị tên khách,
     * đồng thời các endpoint khác biết khách hiện tại là ai.</p>
     */
    private void luuKhachHangOnlineVaoSession(HttpSession session, KhachHang khachHang) {
        session.setAttribute(KHACH_HANG_ONLINE_ID_SESSION_KEY, khachHang.getId());
        session.setAttribute(KHACH_HANG_ONLINE_TEN_SESSION_KEY, khachHang.getHoTen());
        session.setAttribute(KHACH_HANG_ONLINE_EMAIL_SESSION_KEY, khachHang.getEmail());
    }

    /**
     * Sinh mã khách hàng mới cho tài khoản online.
     *
     * <p>Hàm lấy khách hàng có id lớn nhất hiện tại, cộng thêm 1 rồi format thành dạng KH001, KH1000...
     * Mã này dùng cho bảng khách hàng khi khách đăng ký tài khoản mới.</p>
     */
    private String taoMaKhachHangMoi() {
        int nextNumber = Optional.ofNullable(khachHangRepository.findMaxSoThuTuMaKhachHang()).orElse(0) + 1;
        String maKhachHang;
        do {
            maKhachHang = String.format("KH%03d", nextNumber++);
        } while (khachHangRepository.existsByMaKhachHang(maKhachHang));
        return maKhachHang;
    }

    private Optional<DiaChiKhachHang> layDiaChiCuaKhach(Integer idDiaChi, Integer idKhachHang) {
        if (idDiaChi == null || idKhachHang == null) {
            return Optional.empty();
        }
        return diaChiKhachHangRepository.findById(idDiaChi)
                .filter(diaChi -> diaChi.getKhachHang() != null)
                .filter(diaChi -> Objects.equals(diaChi.getKhachHang().getId(), idKhachHang));
    }

    private void ganThongTinDiaChi(
            DiaChiKhachHang diaChi,
            String nguoiNhan,
            String sdt,
            String tinhThanh,
            String quanHuyen,
            String phuongXa,
            String diaChiChiTiet
    ) {
        diaChi.setNguoiNhan(chuanHoaText(nguoiNhan));
        diaChi.setSdt(chuanHoaSoDienThoai(sdt));
        diaChi.setTinhThanh(chuanHoaText(tinhThanh));
        diaChi.setPhuongXa(chuanHoaText(phuongXa));
        diaChi.setDiaChiChiTiet(gopDiaChiCuTheVaQuanHuyen(diaChiChiTiet, quanHuyen));
    }

    private String kiemTraDiaChiHoSo(String nguoiNhan, String sdt, String tinhThanh, String phuongXa) {
        String sdtDaChuanHoa = chuanHoaSoDienThoai(sdt);
        if (!duLieuDiaChiHopLe(nguoiNhan, sdtDaChuanHoa, tinhThanh, phuongXa)) {
            return "Vui lòng nhập đầy đủ người nhận, số điện thoại, tỉnh/thành và phường/xã.";
        }
        if (!soDienThoaiHopLe(sdtDaChuanHoa)) {
            return "Số điện thoại phải bắt đầu bằng 0 và đủ 10 chữ số.";
        }
        return null;
    }
    /**
     * Chuẩn hóa email nhập từ form.
     *
     * <p>Email được trim khoảng trắng, gom khoảng trắng thừa và chuyển về chữ thường
     * để tránh tạo hai tài khoản khác nhau chỉ vì khác chữ hoa/chữ thường.</p>
     */
    private String chuanHoaEmail(String value) {
        String text = chuanHoaText(value);
        return StringUtils.hasText(text) ? text.toLowerCase() : text;
    }

    /**
     * Chuẩn hóa số điện thoại nhập từ form.
     *
     * <p>Hàm này xóa toàn bộ khoảng trắng để người dùng nhập "070 834 0681"
     * vẫn được xử lý thành "0708340681" trước khi validate và lưu.</p>
     */
    private String chuanHoaSoDienThoai(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "");
    }

    /**
     * Kiểm tra số điện thoại có đúng rule không.
     *
     * <p>Rule hiện tại: bắt đầu bằng số 0 và có đúng 10 chữ số.</p>
     */
    private boolean soDienThoaiHopLe(String value) {
        return StringUtils.hasText(value) && SDT_HOP_LE.matcher(value).matches();
    }

    /**
     * Kiểm tra email có đúng định dạng cơ bản không.
     *
     * <p>Hàm này chỉ kiểm tra cấu trúc email, không kiểm tra email có tồn tại thật ngoài đời hay không.</p>
     */
    private boolean emailHopLe(String value) {
        return StringUtils.hasText(value) && EMAIL_HOP_LE.matcher(value).matches();
    }

    /**
     * Chuẩn hóa chuỗi text nhập từ form.
     *
     * <p>Nếu null thì trả về chuỗi rỗng. Nếu có dữ liệu thì trim hai đầu
     * và gom nhiều khoảng trắng liên tiếp thành một khoảng trắng.</p>
     */
    private String chuanHoaText(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Ghép địa chỉ cụ thể với quận/huyện khi lưu địa chỉ trong hồ sơ.
     *
     * <p>Form hồ sơ đang tách địa chỉ cụ thể và quận/huyện, còn dữ liệu cũ có thể cần lưu ghép.
     * Hàm này ghép hai phần có dữ liệu bằng dấu phẩy.</p>
     */
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

    /**
     * Chuyển ngày sinh từ chuỗi HTML form sang LocalDate.
     *
     * <p>Nếu khách bỏ trống hoặc nhập sai định dạng thì trả về null,
     * nghĩa là ngày sinh chưa được cập nhật.</p>
     */
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

    /**
     * Chuẩn hóa giới tính từ form hồ sơ.
     *
     * <p>Hiện hệ thống chỉ nhận 0 hoặc 1. Giá trị khác sẽ được đưa về null
     * để tránh lưu dữ liệu giới tính sai.</p>
     */
    private Integer chuanHoaGioiTinh(Integer gioiTinh) {
        if (gioiTinh == null) {
            return null;
        }
        return gioiTinh == 0 || gioiTinh == 1 ? gioiTinh : null;
    }

    /**
     * Kiểm tra dữ liệu địa chỉ hồ sơ có đủ điều kiện lưu không.
     *
     * <p>Bắt buộc có người nhận, số điện thoại, tỉnh/thành và phường/xã.
     * Số điện thoại phải theo rule 10 số bắt đầu bằng 0.</p>
     */
    private boolean duLieuDiaChiHopLe(String nguoiNhan, String sdt, String tinhThanh, String phuongXa) {
        return StringUtils.hasText(chuanHoaText(nguoiNhan))
                && StringUtils.hasText(chuanHoaSoDienThoai(sdt))
                && StringUtils.hasText(chuanHoaText(tinhThanh))
                && StringUtils.hasText(chuanHoaText(phuongXa));
    }

    /**
     * Kiểm tra thông tin giao hàng trước khi tạo đơn online.
     *
     * <p>Hàm trả về nội dung lỗi để hiển thị ở checkout. Nếu trả về null
     * nghĩa là thông tin giao hàng đã hợp lệ và có thể tiếp tục đặt hàng.</p>
     */
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

    /**
     * Bỏ cờ mặc định của toàn bộ địa chỉ khách hàng.
     *
     * <p>Dùng trước khi set một địa chỉ mới làm mặc định để đảm bảo mỗi khách chỉ có một địa chỉ mặc định.</p>
     */
    private void boMacDinhDiaChiOnline(List<DiaChiKhachHang> danhSachDiaChi) {
        danhSachDiaChi.forEach(diaChi -> {
            diaChi.setMacDinh(false);
            diaChi.setNgayCapNhat(LocalDateTime.now());
        });
        diaChiKhachHangRepository.saveAll(danhSachDiaChi);
    }

    /**
     * Lấy thông tin giao hàng đang lưu tạm trong session.
     *
     * <p>Dùng để giữ lại form checkout khi khách áp mã giảm giá, xóa mã giảm giá,
     * hoặc quay lại trang thanh toán mà không phải nhập lại từ đầu.</p>
     */
    private ThongTinDatHangOnlineRequest layThongTinDatHangTuSession(HttpSession session) {
        Object value = session.getAttribute(THONG_TIN_DAT_HANG_ONLINE_SESSION_KEY);
        if (value instanceof ThongTinDatHangOnlineRequest thongTinDatHang) {
            return thongTinDatHang;
        }
        return new ThongTinDatHangOnlineRequest();
    }

    /**
     * Lấy thông tin giao hàng để hiển thị trên trang checkout.
     *
     * <p>Nếu session chưa có dữ liệu thì tạo request rỗng. Nếu khách đã đăng nhập,
     * hàm sẽ tự điền họ tên, số điện thoại, email và địa chỉ mặc định nếu các ô đó đang trống.</p>
     */
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

    /**
     * Lưu thông tin giao hàng vào session.
     *
     * <p>Hàm này chuẩn hóa dữ liệu trước khi lưu, đặc biệt là email, số điện thoại và địa chỉ.
     * Nhờ vậy khi khách áp voucher hoặc refresh checkout thì dữ liệu nhập không bị reset trắng.</p>
     */
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

    /**
     * Tính phí vận chuyển cho đơn online theo tỉnh/thành nhận hàng.
     *
     * <p>Cửa hàng được xem là ở Hà Nội. Giao trong Hà Nội dùng phí nội tỉnh,
     * giao tới các tỉnh miền Bắc dùng phí nội miền, còn lại dùng phí liên miền.</p>
     */
    private BigDecimal tinhPhiVanChuyenOnline(ThongTinDatHangOnlineRequest thongTinDatHang, BigDecimal tongTienHang) {
        BigDecimal tongHang = tongTienHang == null ? BigDecimal.ZERO : tongTienHang;
        if (tongHang.compareTo(NGUONG_MIEN_PHI_SHIP) > 0) {
            return BigDecimal.ZERO;
        }

        String tinhThanhNhan = thongTinDatHang == null ? "" : chuanHoaTenTinhThanh(thongTinDatHang.getTinhThanh());
        if (!StringUtils.hasText(tinhThanhNhan) || TINH_THANH_CUA_HANG.equals(tinhThanhNhan)) {
            return PHI_SHIP_NOI_TINH;
        }
        if (CAC_TINH_MIEN_BAC.contains(tinhThanhNhan)) {
            return PHI_SHIP_NOI_MIEN;
        }
        return PHI_SHIP_LIEN_MIEN;
    }

    /**
     * Chuẩn hóa tên tỉnh/thành để so sánh khi tính phí ship.
     *
     * <p>Hàm này bỏ dấu tiếng Việt, chuyển về chữ thường và bỏ các tiền tố như
     * "Tỉnh", "Thành phố" để việc so sánh tên tỉnh ổn định hơn.</p>
     */
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

    /**
     * Điền địa chỉ mặc định của khách vào thông tin đặt hàng.
     *
     * <p>Chỉ tự điền những trường đang trống. Nếu khách đã nhập thông tin giao hàng trước đó,
     * hàm sẽ không ghi đè để tránh làm mất dữ liệu form.</p>
     */
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

    /**
     * Tách địa chỉ trong database thành các phần dùng cho checkout.
     *
     * <p>Dữ liệu địa chỉ cũ có thể đang được lưu dạng chuỗi ghép. Hàm này cố gắng tách lại
     * thành tỉnh/thành, quận/huyện, phường/xã và địa chỉ cụ thể để fill lên form thanh toán.</p>
     */
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

    /**
     * Tách một chuỗi địa chỉ theo dấu phẩy.
     *
     * <p>Các phần rỗng sẽ bị bỏ qua. Kết quả dùng cho việc đoán phường/xã, quận/huyện,
     * địa chỉ cụ thể từ dữ liệu địa chỉ cũ.</p>
     */
    private List<String> tachPhanDiaChi(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(this::chuanHoaText)
                .filter(StringUtils::hasText)
                .toList();
    }

    /**
     * Kiểm tra một phần địa chỉ có phải tên quận/huyện/thị xã/thành phố không.
     *
     * <p>Hàm này phục vụ việc tách địa chỉ cũ, nơi quận/huyện có thể bị lưu chung
     * trong trường địa chỉ chi tiết.</p>
     */
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

    /**
     * Object nhỏ dùng để chứa địa chỉ đã tách cho form checkout.
     */
    private record DiaChiCheckout(String tinhThanh, String quanHuyen, String phuongXa, String diaChiChiTiet) {
    }

    /**
     * Object nhỏ gom dữ liệu một đơn hàng để màn "Đơn hàng của tôi" render dễ hơn.
     */
    private record DonHangCuaToiView(
            HoaDonResponse hoaDon,
            List<ChiTietHoaDonResponse> chiTiet,
            int tongSanPham,
            String statusClass,
            String phuongThucThanhToan,
            String trangThaiThanhToan,
            boolean daThanhToan
    ) {
    }

    /**
     * Object nhỏ đại diện cho một lựa chọn trạng thái trong bộ lọc đơn hàng.
     */
    private record TrangThaiDonHangOption(Integer value, String label) {
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

    /**
     * Lấy voucher đang áp dụng trong session và kiểm tra lại còn dùng được không.
     *
     * <p>Voucher có thể hết hạn, hết lượt, không còn thuộc khách hoặc không đủ điều kiện
     * sau khi giỏ hàng/địa chỉ thay đổi, nên mỗi lần tính checkout đều phải kiểm tra lại.</p>
     */
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

    /**
     * Tính số tiền giảm của voucher đang áp dụng cho giỏ hàng hiện tại.
     *
     * <p>Nếu không có voucher hoặc giỏ hàng rỗng thì trả về 0.
     * Nếu có voucher thì chuyển sang hàm tính giảm chung theo tổng tiền hàng và phí ship.</p>
     */
    private BigDecimal tinhTienGiamVoucher(PhieuGiamGia phieuGiamGia, DuLieuGioHangOnline duLieuGioHang, BigDecimal phiVanChuyen) {
        if (phieuGiamGia == null || duLieuGioHang == null) {
            return BigDecimal.ZERO;
        }
        return tinhTienGiamChoDonHang(phieuGiamGia, duLieuGioHang.getTongCong(), phiVanChuyen);
    }

    private PhieuGiamGia timPhieuGiamGiaTotNhat(List<PhieuGiamGia> danhSachPhieu, DuLieuGioHangOnline duLieuGioHang, BigDecimal phiVanChuyen) {
        if (danhSachPhieu == null || danhSachPhieu.isEmpty() || duLieuGioHang == null) {
            return null;
        }

        BigDecimal tongTienHang = duLieuGioHang.getTongCong() == null ? BigDecimal.ZERO : duLieuGioHang.getTongCong();
        return danhSachPhieu.stream()
                .max((left, right) -> {
                    BigDecimal leftDiscount = tinhTienGiamChoDonHang(left, tongTienHang, phiVanChuyen);
                    BigDecimal rightDiscount = tinhTienGiamChoDonHang(right, tongTienHang, phiVanChuyen);
                    int compareDiscount = leftDiscount.compareTo(rightDiscount);
                    if (compareDiscount != 0) {
                        return compareDiscount;
                    }

                    BigDecimal leftMin = left.getGiaTriDonToiThieu() == null ? BigDecimal.ZERO : left.getGiaTriDonToiThieu();
                    BigDecimal rightMin = right.getGiaTriDonToiThieu() == null ? BigDecimal.ZERO : right.getGiaTriDonToiThieu();
                    return rightMin.compareTo(leftMin);
                })
                .orElse(null);
    }

    /**
     * Tìm voucher hợp lệ theo mã khách nhập.
     *
     * <p>Voucher chỉ được trả về khi mã tồn tại, phiếu đang hoạt động và khách hiện tại
     * được quyền sử dụng phiếu đó.</p>
     */
    private PhieuGiamGia timPhieuHopLeTheoMa(String maPhieuGiamGia, HttpSession session) {
        if (maPhieuGiamGia == null || maPhieuGiamGia.trim().isEmpty()) {
            return null;
        }

        return phieuGiamGiaRepository.findByMaPhieuGiamGia(maPhieuGiamGia.trim())
                .filter(this::laPhieuDangHopLe)
                .filter(phieu -> khachHangOnlineDuocDungPhieu(phieu, session))
                .orElse(null);
    }

    /**
     * Lấy danh sách voucher có thể dùng cho đơn hiện tại.
     *
     * <p>Hàm này dùng để hiển thị danh sách mã gợi ý khi khách bấm vào ô nhập mã giảm giá.
     * Bộ lọc gồm hiệu lực phiếu, quyền dùng theo khách, giá trị đơn tối thiểu và số tiền giảm thực tế.</p>
     */
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

    /**
     * Kiểm tra khách hiện tại có quyền dùng voucher không.
     *
     * <p>Với voucher thường, chỉ cần phiếu không bị gắn riêng cho khách nào là dùng được.
     * Với voucher riêng, khách phải đăng nhập và có bản ghi chưa sử dụng trong bảng liên kết khách-voucher.</p>
     */
    private boolean khachHangOnlineDuocDungPhieu(PhieuGiamGia phieuGiamGia, HttpSession session) {
        if (phieuGiamGia == null || phieuGiamGia.getId() == null) {
            return false;
        }

        boolean laPhieuGanRiengChoKhach = phieuGiamGiaKhachHangRepository.existsPhieuGanKhachHang(phieuGiamGia.getId());
        if (!laPhieuGanRiengChoKhach) {
            return true;
        }

        return layIdKhachHangOnlineTuSession(session)
                .map(idKhachHang -> phieuGiamGiaKhachHangRepository.existsKhachHangDuocDungPhieu(phieuGiamGia.getId(), idKhachHang))
                .orElse(false);
    }

    /**
     * Tính số tiền giảm cuối cùng cho một đơn hàng.
     *
     * <p>Hàm này xử lý voucher phần trăm, voucher giảm phí vận chuyển và voucher giảm tiền cố định.
     * Nếu phiếu có giới hạn giảm tối đa thì kết quả không vượt quá giới hạn đó.</p>
     */
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
            if (phieuGiamGia.getGiaTriGiam() == null) {
                return BigDecimal.ZERO;
            }
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

    /**
     * Kiểm tra voucher còn hoạt động ở mức cơ bản.
     *
     * <p>Hàm này kiểm tra trạng thái, ngày bắt đầu, ngày kết thúc và số lượt còn lại.
     * Điều kiện đơn tối thiểu và quyền dùng theo khách được kiểm tra ở các hàm khác.</p>
     */
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

    /**
     * Quy đổi trạng thái hóa đơn thành bước trên thanh timeline theo dõi đơn.
     *
     * <p>Ví dụ: chờ xác nhận là bước 1, đã xác nhận là bước 2,
     * chờ giao hàng/đang giao/đã giao/hoàn thành là các bước tiếp theo.</p>
     */
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

    /**
     * Sửa tương thích cho các đơn online cũ bị lưu sai trạng thái.
     *
     * <p>Trước đây có thời điểm đơn mới tạo bị nhảy thẳng sang "Đã xác nhận".
     * Hàm này chỉ sửa các đơn online có đúng một dòng lịch sử "Đặt hàng online",
     * đưa trạng thái về "Chờ xác nhận" để màn theo dõi hiển thị đúng.</p>
     */
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

    /**
     * Hiển thị trang giới thiệu của cửa hàng online.
     *
     * <p>Trang này là trang nội dung tĩnh, controller chỉ truyền tiêu đề, active menu và năm footer.</p>
     */
    @GetMapping("/cua-hang/gioi-thieu")
    public String gioiThieu(Model model) {
        model.addAttribute("pageTitle", "Aerion Sports | Giới thiệu");
        model.addAttribute("activePage", "gioi-thieu");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/gioi-thieu";
    }

    /**
     * Hiển thị trang liên hệ của cửa hàng online.
     *
     * <p>Trang này là trang nội dung tĩnh, dùng để khách xem thông tin liên hệ và hỗ trợ của shop.</p>
     */
    @GetMapping("/cua-hang/lien-he")
    public String lienHe(Model model) {
        model.addAttribute("pageTitle", "Aerion Sports | Liên hệ");
        model.addAttribute("activePage", "lien-he");
        model.addAttribute("footerYear", java.time.Year.now().getValue());
        return "ban-hang-online/lien-he";
    }
}
