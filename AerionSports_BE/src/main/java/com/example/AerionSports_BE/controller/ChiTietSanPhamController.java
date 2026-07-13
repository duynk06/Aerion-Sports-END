package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.ChiTietSanPhamFilter;
import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.request.SanPhamFilter;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.service.ChiTietSanPhamService;
import com.example.AerionSports_BE.service.SanPhamService;
import com.example.AerionSports_BE.service.MauSacService;
import com.example.AerionSports_BE.service.TrongLuongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Controller
public class ChiTietSanPhamController {

    @Autowired private ChiTietSanPhamService chiTietSanPhamService;
    @Autowired private SanPhamService sanPhamService;
    @Autowired private MauSacService mauSacService;
    @Autowired private TrongLuongService trongLuongService;

    // 1. Chi tiết biến thể của 1 sản phẩm cha cụ thể
    @GetMapping("/san-pham/bien-the")
    public String danhSach(
            @RequestParam("idSP") Integer idSP,
            @RequestParam(value = "maSP", required = false) String maSP,
            @RequestParam(value = "tenSP", required = false) String tenSP,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model
    ) {
        SanPhamResponse productInfo = null;
        if (maSP != null && !maSP.isBlank()) {
            SanPhamFilter spFilter = new SanPhamFilter();
            spFilter.setKeyword(maSP);
            spFilter.setPage(0);
            spFilter.setSize(10);
            Page<SanPhamResponse> spResult = sanPhamService.search(spFilter);
            for (SanPhamResponse sp : spResult.getContent()) {
                if (sp.getId() != null && sp.getId().equals(idSP)) {
                    productInfo = sp;
                    break;
                }
            }
        }

        ChiTietSanPhamFilter filter = new ChiTietSanPhamFilter();
        filter.setIdSanPham(idSP);
        filter.setPage(page);
        filter.setSize(size);
        if (keyword != null && !keyword.isBlank()) filter.setKeyword(keyword);
        if (trangThai != null) filter.setTrangThai(trangThai);

        Page<ChiTietSanPhamResponse> result = chiTietSanPhamService.search(filter);

        model.addAttribute("danhSachBienThe", result.getContent());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("idSP", idSP);
        model.addAttribute("maSP", maSP == null ? "" : maSP);
        model.addAttribute("tenSP", tenSP == null ? "" : tenSP);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("productInfo", productInfo);

        return "san-pham/bien-the";
    }

    // 2. Danh sách tất cả biến thể hệ thống (Đã bổ sung nạp Combobox phục vụ Modal Sửa)
    @GetMapping("/san-pham/danh-sach-bien-the")
    public String danhSachTatCa(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model
    ) {
        ChiTietSanPhamFilter filter = new ChiTietSanPhamFilter();
        filter.setPage(page);
        filter.setSize(size);
        if (keyword != null && !keyword.isBlank()) filter.setKeyword(keyword);
        if (trangThai != null) filter.setTrangThai(trangThai);

        Page<ChiTietSanPhamResponse> result = chiTietSanPhamService.search(filter);

        model.addAttribute("danhSachBienThe", result.getContent());
        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("totalElements", result.getTotalElements());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("trangThai", trangThai);

        // 🟢 BỔ SUNG: Đổ dữ liệu thuộc tính để Modal ở trang này không bị rỗng danh sách chọn
        model.addAttribute("danhSachMauSac", mauSacService.getAll());
        model.addAttribute("danhSachTrongLuong", trongLuongService.getAll());

        return "san-pham/danh-sach-bien-the";
    }

    // 3. Hiển thị form thêm mới biến thể lẻ
    @GetMapping("/san-pham/bien-the/them-moi")
    public String trangThemMoiBienThe(
            @RequestParam("idSP") Integer idSP,
            @RequestParam("maSP") String maSP,
            @RequestParam("tenSP") String tenSP,
            Model model
    ) {
        SanPhamResponse productInfo = sanPhamService.findById(idSP);

        int maxSuffix = 0;
        if (productInfo != null && productInfo.getChiTietSanPhams() != null) {
            for (var bt : productInfo.getChiTietSanPhams()) {
                String maCtsp = bt.getMaCtsp();
                if (maCtsp != null && maCtsp.contains("-CT-")) {
                    try {
                        String[] parts = maCtsp.split("-CT-");
                        int suffix = Integer.parseInt(parts[parts.length - 1].trim());
                        if (suffix > maxSuffix) maxSuffix = suffix;
                    } catch (Exception ignored) {}
                }
            }
        }
        String maSkuTuSinh = maSP + "-CT-" + String.format("%02d", maxSuffix + 1);

        model.addAttribute("idSP", idSP);
        model.addAttribute("maSP", maSP);
        model.addAttribute("tenSP", tenSP);
        model.addAttribute("maSkuTuSinh", maSkuTuSinh);
        model.addAttribute("productInfo", productInfo);
        model.addAttribute("danhSachMauSac", mauSacService.getAll());
        model.addAttribute("danhSachTrongLuong", trongLuongService.getAll());

        return "san-pham/them-bien-the";
    }

    // 4. Hiển thị form cập nhật thông tin biến thể lẻ
    @GetMapping("/san-pham/bien-the/sua")
    public String trangSuaBienThe(
            @RequestParam("id") Integer id,
            @RequestParam("idSP") Integer idSP,
            @RequestParam("maSP") String maSP,
            @RequestParam("tenSP") String tenSP,
            Model model
    ) {
        ChiTietSanPhamResponse bienTheHienTai = chiTietSanPhamService.findById(id);
        SanPhamResponse productInfo = sanPhamService.findById(idSP);

        model.addAttribute("bienThe", bienTheHienTai);
        model.addAttribute("idSP", idSP);
        model.addAttribute("maSP", maSP);
        model.addAttribute("tenSP", tenSP);
        model.addAttribute("productInfo", productInfo);
        model.addAttribute("danhSachMauSac", mauSacService.getAll());
        model.addAttribute("danhSachTrongLuong", trongLuongService.getAll());

        return "san-pham/sua-bien-the";
    }

    // 5. Xử lý nhận dữ liệu POST lưu thực thể biến thể mới (Đã tối ưu cho giao diện rút gọn)
    @PostMapping("/san-pham/bien-the/luu")
    public String luuBienThe(
            @ModelAttribute ChiTietSanPhamRequest request,
            @RequestParam("idSP") Integer idSP,
            @RequestParam("maSP") String maSP,
            @RequestParam("tenSP") String tenSP,
            @RequestParam(value = "fileAnh", required = false) MultipartFile fileAnh // Nhận tệp ảnh nếu giao diện đẩy lên
    ) {
        try {
            // Thiết lập các thuộc tính ngầm để đảm bảo tính toàn vẹn của DTO/Entity
            request.setIdSanPham(idSP);
            request.setTrangThai(1); // Mặc định biến thể mới tạo ở trạng thái Đang hoạt động

            // Nếu Service của bạn có hàm hỗ trợ upload xử lý fileAnh kèm Request, hãy truyền vào.
            // Nếu dùng hàm mặc định, Spring sẽ tự động ánh xạ các trường số tiền thông qua @ModelAttribute.
            chiTietSanPhamService.save(request);

            // Mã hóa tên sản phẩm tránh lỗi ký tự tiếng Việt có dấu trên URL khi redirect
            String encodedTen = URLEncoder.encode(tenSP, StandardCharsets.UTF_8);
            return "redirect:/san-pham/bien-the?idSP=" + idSP + "&maSP=" + maSP + "&tenSP=" + encodedTen;
        } catch (Exception e) {
            e.printStackTrace();
            String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            String encodedTen = URLEncoder.encode(tenSP, StandardCharsets.UTF_8);
            return "redirect:/san-pham/bien-the/them-moi?idSP=" + idSP + "&maSP=" + maSP + "&tenSP=" + encodedTen + "&error=" + encodedError;
        }
    }

    // 6. Xử lý cập nhật biến thể chi tiết từ biểu mẫu chỉnh sửa
    @PostMapping("/san-pham/bien-the/cap-nhat/{id}")
    public String capNhatBienThe(
            @PathVariable("id") Integer id,
            @ModelAttribute ChiTietSanPhamRequest request,
            @RequestParam("idSP") Integer idSP,
            @RequestParam("maSP") String maSP,
            @RequestParam("tenSP") String tenSP
    ) {
        try {
            chiTietSanPhamService.update(id, request);
            return "redirect:/san-pham/bien-the?idSP=" + idSP + "&maSP=" + maSP + "&tenSP=" + tenSP;
        } catch (Exception e) {
            String encodedError = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/san-pham/bien-the/sua?id=" + id + "&idSP=" + idSP + "&maSP=" + maSP + "&tenSP=" + tenSP + "&error=" + encodedError;
        }
    }

    // 7. API REST đổi trạng thái nhanh cho Biến Thể Chi Tiết
    @PutMapping("/api/chi-tiet-san-pham/{id}/trang-thai")
    @ResponseBody
    public ResponseEntity<?> thayDoiTrangThaiBienThe(
            @PathVariable("id") Integer id,
            @RequestParam("trangThai") Integer trangThai
    ) {
        try {
            chiTietSanPhamService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Cập nhật thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 🟢 BỔ SUNG ĐÚNG NƠI: API xử lý cập nhật đầy đủ thuộc tính kèm File ảnh từ Modal Độc lập
    @PostMapping("/api/chi-tiet-san-pham/{id}/cap-nhat-day-du")
    @ResponseBody
    public ResponseEntity<?> capNhatDayDuBienTheModal(
            @PathVariable("id") Integer id,
            @RequestParam(value = "idMauSac", required = false) Integer idMauSac,
            @RequestParam(value = "idTrongLuong", required = false) Integer idTrongLuong,
            @RequestParam("giaBan") java.math.BigDecimal giaBan,
            @RequestParam("soLuong") Integer soLuong,
            @RequestParam(value = "fileAnh", required = false) MultipartFile fileAnh
    ) {
        try {
            ChiTietSanPhamRequest req = new ChiTietSanPhamRequest();
            req.setIdMauSac(idMauSac);
            req.setIdTrongLuong(idTrongLuong);
            req.setGiaBan(giaBan);
            req.setSoLuong(soLuong);
            req.setGiaNhap(java.math.BigDecimal.ZERO);
            req.setTrangThai(1); // Mặc định giữ nguyên trạng thái hoạt động khi cập nhật nhanh

            // Gọi hàm xử lý tập trung trực tiếp của ChiTietSanPhamService
            chiTietSanPhamService.updateFullDetailsFromModal(id, req, fileAnh);
            return ResponseEntity.ok().body(Map.of("success", true, "message", "Cập nhật thông tin biến thể thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}