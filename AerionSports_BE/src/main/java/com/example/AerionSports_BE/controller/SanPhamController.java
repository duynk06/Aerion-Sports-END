package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.SanPhamFilter;
import com.example.AerionSports_BE.dto.request.SanPhamRequest;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.service.*;
import com.example.AerionSports_BE.repository.ChiTietSanPhamRepository; // 🟢 Bổ sung import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/san-pham")
public class SanPhamController {

    @Autowired private SanPhamService sanPhamService;
    @Autowired private ThuongHieuService thuongHieuService;
    @Autowired private XuatXuService xuatXuService;
    @Autowired private DanhMucService danhMucService;
    @Autowired private ChuViCanVotService chuViCanVotService;
    @Autowired private DoCungService doCungService;
    @Autowired private DiemCanBangService diemCanBangService;
    @Autowired private ChatLieuThanVotService chatLieuThanVotService;
    @Autowired private ChatLieuKhungVotService chatLieuKhungVotService;
    @Autowired private MauSacService mauSacService;
    @Autowired private TrongLuongService trongLuongService;
    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;
    @Autowired private ChiTietSanPhamService chiTietSanPhamService;

    // 1. Xem danh sách sản phẩm cha
    @GetMapping
    public String danhSach(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "idThuongHieu", required = false) Integer idThuongHieu,
            @RequestParam(value = "idXuatXu", required = false) Integer idXuatXu,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "soLuongMin", required = false) Integer soLuongMin,
            @RequestParam(value = "giaMax", required = false) Long giaMax,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model
    ) {
        SanPhamFilter filter = new SanPhamFilter();
        filter.setKeyword(keyword);
        filter.setIdThuongHieu(idThuongHieu);
        filter.setIdXuatXu(idXuatXu);
        filter.setTrangThai(trangThai);
        filter.setSoLuongMin(soLuongMin);
        filter.setGiaMax(giaMax);
        filter.setPage(page);
        filter.setSize(size);

        Page<SanPhamResponse> result = sanPhamService.search(filter);
        List<SanPhamResponse> danhSach = result.getContent();

        Map<Integer, Map<String, Object>> thongKeBienThe = new HashMap<>();
        for (SanPhamResponse sp : danhSach) {
            int tongTon = 0;
            Long giaMin = null;
            Long giaMaxBienThe = null;
            try {
                var chiTietSanPhams = sp.getChiTietSanPhams();
                if (chiTietSanPhams != null) {
                    for (var bt : chiTietSanPhams) {
                        int soLuong = bt.getSoLuong() == null ? 0 : bt.getSoLuong();
                        long giaBan = bt.getGiaBan() == null ? 0L : bt.getGiaBan().longValue();
                        tongTon += soLuong;
                        if (giaMin == null || giaBan < giaMin) giaMin = giaBan;
                        if (giaMaxBienThe == null || giaBan > giaMaxBienThe) giaMaxBienThe = giaBan;
                    }
                }
            } catch (Exception ignored) {}
            Map<String, Object> tk = new HashMap<>();
            tk.put("tongTon", tongTon);
            tk.put("giaMin", giaMin);
            tk.put("giaMax", giaMaxBienThe);
            thongKeBienThe.put(sp.getId(), tk);
        }

        // 🟢 Lấy mức giá bán cao nhất động từ cơ sở dữ liệu để làm mốc slider tối đa
        java.math.BigDecimal maxPriceDb = chiTietSanPhamRepository.getGiaMax();
        long giaCaoNhatHeThong = (maxPriceDb != null) ? maxPriceDb.longValue() : 50000000L;
        model.addAttribute("danhSachSanPham", danhSach);
        model.addAttribute("thongKeBienThe", thongKeBienThe);
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("currentPage", result.getNumber());
        model.addAttribute("pageSize", result.getSize());
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("idThuongHieu", idThuongHieu);
        model.addAttribute("idXuatXu", idXuatXu);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("soLuongMin", soLuongMin);      // 🟢 giữ giá trị trên ô input
        model.addAttribute("giaMaxChon", giaMax);
        model.addAttribute("giaCaoNhatHeThong", giaCaoNhatHeThong); // 🟢 Đẩy mốc giá max sang HTML
        model.addAttribute("danhSachThuongHieu", thuongHieuService.getAll());
        model.addAttribute("danhSachXuatXu", xuatXuService.getAll());

        return "san-pham/san-pham";
    }

    // 2. Tải trang Thêm mới sản phẩm cha
    @GetMapping("/them-moi")
    public String trangThemMoi(Model model) {
        String maTuSinh = "SP" + String.format("%03d", sanPhamService.getAll().size() + 1);
        model.addAttribute("maTuSinh", maTuSinh);

        model.addAttribute("danhSachThuongHieu", thuongHieuService.getAll());
        model.addAttribute("danhSachXuatXu", xuatXuService.getAll());
        model.addAttribute("danhSachDanhMuc", danhMucService.getAll());
        model.addAttribute("danhSachChuViCan", chuViCanVotService.getAll());
        model.addAttribute("danhSachDoCung", doCungService.getAll());
        model.addAttribute("danhSachDiemCanBang", diemCanBangService.getAll());
        model.addAttribute("danhSachChatLieuThan", chatLieuThanVotService.getAll());
        model.addAttribute("danhSachChatLieuKhung", chatLieuKhungVotService.getAll());

        model.addAttribute("danhSachMauSac", mauSacService.getAll());
        model.addAttribute("danhSachTrongLuong", trongLuongService.getAll());

        return "san-pham/them-san-pham";
    }

    @PostMapping("/luu")
    public String luuSanPham(
            @ModelAttribute SanPhamRequest request,
            @RequestParam(value = "idSanPhamGop", required = false) Integer idSanPhamGop,
            @RequestParam(value = "bienTheJson", required = false) String bienTheJson
    ) {
        try {
            // 🛑 CASE TRÙNG LẶP: Nếu người dùng chọn phương án "Gộp biến thể"
            if (idSanPhamGop != null && bienTheJson != null && !bienTheJson.isBlank()) {
                // Gọi sang tầng Service xử lý bóc tách JSON và lưu nối tiếp danh sách CTSP vào ID sản phẩm cũ
                chiTietSanPhamService.saveVariantsToExistingProduct(idSanPhamGop, bienTheJson);
                return "redirect:/san-pham/bien-the?idSP=" + idSanPhamGop;
            }

            // CASE MẶC ĐỊNH: Khởi tạo sản phẩm cha mới cùng danh sách ma trận biến thể ban đầu
            sanPhamService.save(request);
            return "redirect:/san-pham";

        } catch (Exception e) {
            e.printStackTrace();
            String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/san-pham/them-moi?error=" + encodedError;
        }
    }

    // 4. API REST đổi trạng thái kinh doanh nhanh
    @PutMapping("/api/san-pham/{id}/trang-thai")
    @ResponseBody
    public ResponseEntity<?> thayDoiTrangThai(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai
    ) {
        try {
            sanPhamService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Cập nhật thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/api/them-nhanh-thuoc-tinh/{loai}")
    @ResponseBody
    public ResponseEntity<?> themNhanhThuocTinh(
            @PathVariable String loai,
            @RequestParam("ten") String ten) {
        try {
            String value = ten.trim();
            Map<String, Object> responseData = new HashMap<>();

            switch (loai) {
                case "danh-muc":
                    String maDm = "DM" + String.format("%03d", danhMucService.getAll().size() + 1);
                    var dmr = new com.example.AerionSports_BE.dto.request.DanhMucRequest();
                    dmr.setTenDanhMuc(value); dmr.setMaDanhMuc(maDm); dmr.setTrangThai(1);
                    var savedDm = danhMucService.save(dmr);
                    responseData.put("id", savedDm.getId()); responseData.put("ten", savedDm.getTenDanhMuc()); responseData.put("ma", savedDm.getMaDanhMuc());
                    break;

                case "thuong-hieu":
                    String maTh = "TH" + String.format("%03d", thuongHieuService.getAll().size() + 1);
                    var thr = new com.example.AerionSports_BE.dto.request.ThuongHieuRequest();
                    thr.setTenThuongHieu(value); thr.setMaThuongHieu(maTh); thr.setTrangThai(1);
                    var savedTh = thuongHieuService.save(thr);
                    responseData.put("id", savedTh.getId()); responseData.put("ten", savedTh.getTenThuongHieu()); responseData.put("ma", savedTh.getMaThuongHieu());
                    break;

                case "xuat-xu":
                    String maXx = "XX" + String.format("%03d", xuatXuService.getAll().size() + 1);
                    var xxr = new com.example.AerionSports_BE.dto.request.XuatXuRequest();
                    xxr.setTenXuatXu(value); xxr.setMaXuatXu(maXx); xxr.setTrangThai(1);
                    var savedXx = xuatXuService.save(xxr);
                    responseData.put("id", savedXx.getId()); responseData.put("ten", savedXx.getTenXuatXu()); responseData.put("ma", savedXx.getMaXuatXu());
                    break;

                default:
                    return ResponseEntity.badRequest().body(Map.of("message", "Loại thuộc tính không hỗ trợ thêm nhanh"));
            }
            return ResponseEntity.ok(responseData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}