package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.ChiTietDotGiamGiaDTO;
import com.example.AerionSports_BE.dto.DotGiamGiaDTO;
import com.example.AerionSports_BE.service.DotGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/dot-giam-gia")
@RequiredArgsConstructor
/**
 * Controller MVC cho màn quản lý đợt giảm giá trong admin.
 * File này lo toàn bộ flow:
 * - trang danh sách
 * - modal thêm/sửa/xem
 * - submit form HTML
 * - chuẩn bị model dùng chung cho các modal
 */
public class DotGiamGiaViewController {

    private final DotGiamGiaService dotGiamGiaService;

    @GetMapping
    public String index(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ) {
        // Chặn page/size âm để tránh query lỗi hoặc phân trang vô nghĩa.
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        try {
            // Lấy trang dữ liệu từ service, service sẽ xử lý toàn bộ filter nghiệp vụ.
            Page<DotGiamGiaDTO> pageData = dotGiamGiaService.getDanhSach(
                    keyword,
                    trangThai,
                    tuNgay != null ? tuNgay.toString() : null,
                    denNgay != null ? denNgay.toString() : null,
                    page,
                    size
            );

            model.addAttribute("rows", pageData.getContent());
            model.addAttribute("totalPages", pageData.getTotalPages());
            model.addAttribute("totalElements", pageData.getTotalElements());
        } catch (RuntimeException e) {
            // Nếu filter sai hoặc service báo lỗi, vẫn render trang danh sách với message.
            model.addAttribute("rows", Collections.emptyList());
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalElements", 0);
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);

        prepareModalModel(model, action, id);
        return "dot-giam-gia/index";
    }

    @GetMapping("/them")
    public String createForm() {
        // Chỉ redirect để mở lại index ở chế độ create.
        return "redirect:/dot-giam-gia?action=create";
    }

    @PostMapping("/them")
    public String create(
            @RequestParam String tenDotGiamGia,
            @RequestParam BigDecimal giaTriGiam,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime ngayBatDau,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime ngayKetThuc,
            @RequestParam(required = false) String moTa,
            @RequestParam(required = false, name = "selectedProductIds") List<Integer> selectedProductIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Gom dữ liệu form HTML thành DTO rồi gửi xuống service để validate + lưu.
            DotGiamGiaDTO dto = buildDto(tenDotGiamGia, giaTriGiam, ngayBatDau, ngayKetThuc, moTa, selectedProductIds);
            DotGiamGiaDTO created = dotGiamGiaService.create(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đợt giảm giá " + created.getMaDotGiamGia() + " thành công.");
            return "redirect:/dot-giam-gia";
        } catch (RuntimeException e) {
            // Nếu fail thì quay lại modal create và show flash error.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-giam-gia?action=create";
        }
    }

    @GetMapping("/sua/{id}")
    public String editForm(@PathVariable Integer id) {
        // Mở modal sửa bằng cách redirect sang index với action=edit.
        return "redirect:/dot-giam-gia?action=edit&id=" + id;
    }

    @PostMapping("/sua/{id}")
    public String update(
            @PathVariable Integer id,
            @RequestParam String tenDotGiamGia,
            @RequestParam BigDecimal giaTriGiam,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime ngayBatDau,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime ngayKetThuc,
            @RequestParam(required = false) String moTa,
            @RequestParam(required = false, name = "selectedProductIds") List<Integer> selectedProductIds,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Tương tự create, nhưng service sẽ áp dụng rule riêng cho đợt đang diễn ra.
            DotGiamGiaDTO dto = buildDto(tenDotGiamGia, giaTriGiam, ngayBatDau, ngayKetThuc, moTa, selectedProductIds);
            dotGiamGiaService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đợt giảm giá thành công.");
            return "redirect:/dot-giam-gia";
        } catch (RuntimeException e) {
            // Giữ người dùng ở modal edit để sửa lại dữ liệu vừa nhập.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-giam-gia?action=edit&id=" + id;
        }
    }

    @GetMapping("/xem/{id}")
    public String detail(@PathVariable Integer id) {
        // Mở modal xem chi tiết từ cùng layout index.
        return "redirect:/dot-giam-gia?action=view&id=" + id;
    }

    @PostMapping("/{id}/trang-thai")
    public String updateStatus(
            @PathVariable Integer id,
            @RequestParam Integer trangThai,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Cập nhật trạng thái thủ công từ switch ở danh sách.
            dotGiamGiaService.updateTrangThai(id, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đợt giảm giá.");
        } catch (RuntimeException e) {
            // Nếu trạng thái không hợp lệ hoặc id không tồn tại thì show lỗi cho người dùng.
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-giam-gia";
    }

    @PostMapping("/{id}/xoa")
    public String delete(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Xóa mềm bằng cách chuyển trạng thái sang hủy.
            dotGiamGiaService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đợt giảm giá thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-giam-gia";
    }

    private void prepareModalModel(Model model, String action, Integer id) {
        // Từ query action/id, quyết định trang đang ở list/create/edit/view nào.
        String resolvedAction = action == null ? "" : action.trim().toLowerCase();
        String viewMode = resolvedAction.isEmpty() ? "list" : resolvedAction;
        model.addAttribute("viewMode", viewMode);
        // Các attribute này luôn được set để template fragment dùng an toàn kể cả khi rỗng.
        model.addAttribute("groupedProducts", Collections.emptyList());
        model.addAttribute("products", Collections.emptyList());
        model.addAttribute("selectedProductIds", Collections.emptyList());
        model.addAttribute("selectedProducts", Collections.emptyList());
        model.addAttribute("detailProducts", Collections.emptyList());
        model.addAttribute("pageTitle", switch (viewMode) {
            case "create" -> "Thêm đợt giảm giá";
            case "edit" -> "Sửa đợt giảm giá";
            case "view" -> "Chi tiết đợt giảm giá";
            default -> "Quản lý đợt giảm giá";
        });

        // Không mở modal thì dừng ở đây, chỉ cần trang danh sách là đủ.
        if (!"create".equals(viewMode) && !"edit".equals(viewMode) && !"view".equals(viewMode)) {
            return;
        }

        // Modal create/edit/view đều cần dữ liệu sản phẩm bên phải.
        List<?> groupedProducts = dotGiamGiaService.getGroupedProductsForSelection(null);
        List<ChiTietDotGiamGiaDTO> products = dotGiamGiaService.getProductsForSelection(null);
        model.addAttribute("groupedProducts", groupedProducts != null ? groupedProducts : Collections.emptyList());
        model.addAttribute("products", products);

        if ("create".equals(viewMode)) {
            // Màn tạo mới dùng form rỗng và danh sách chọn rỗng.
            model.addAttribute("form", new DotGiamGiaDTO());
            model.addAttribute("selectedProductIds", Collections.emptyList());
            model.addAttribute("selectedProducts", Collections.emptyList());
            model.addAttribute("detailProducts", Collections.emptyList());
            model.addAttribute("formAction", "/dot-giam-gia/them");
            return;
        }

        // Nếu thiếu id thì không thể mở edit/view.
        if (id == null) {
            model.addAttribute("errorMessage", "Thiếu ID đợt giảm giá");
            model.addAttribute("viewMode", "");
            return;
        }

        DotGiamGiaDTO detail;
        try {
            // Load chi tiết campaign từ service; nếu không có sẽ quay lại trạng thái list.
            detail = dotGiamGiaService.getById(id);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("viewMode", "");
            return;
        }
        List<Integer> selectedIds = extractSelectedIds(detail);

        // Đổ dữ liệu hiện tại lên form để người dùng sửa hoặc xem.
        model.addAttribute("form", detail);
        model.addAttribute("detail", detail);
        model.addAttribute("detailProducts", detail.getChiTietList() != null ? detail.getChiTietList() : Collections.emptyList());
        model.addAttribute("selectedProductIds", selectedIds);
        model.addAttribute("selectedProducts", filterSelectedProducts(products, selectedIds));
        model.addAttribute("formAction", "/dot-giam-gia/sua/" + id);
    }

    private List<ChiTietDotGiamGiaDTO> filterSelectedProducts(List<ChiTietDotGiamGiaDTO> products, List<Integer> selectedIds) {
        if (products == null || selectedIds == null || selectedIds.isEmpty()) {
            return Collections.emptyList();
        }
        return products.stream()
                .filter(product -> selectedIds.contains(product.getIdChiTietSanPham()))
                .toList();
    }

    private DotGiamGiaDTO buildDto(
            String tenDotGiamGia,
            BigDecimal giaTriGiam,
            LocalDateTime ngayBatDau,
            LocalDateTime ngayKetThuc,
            String moTa,
            List<Integer> selectedProductIds
    ) {
        // Gom dữ liệu form HTML thành DTO đúng format service cần.
        DotGiamGiaDTO dto = new DotGiamGiaDTO();
        dto.setTenDotGiamGia(tenDotGiamGia);
        dto.setGiaTriGiam(giaTriGiam);
        dto.setNgayBatDau(ngayBatDau);
        dto.setNgayKetThuc(ngayKetThuc);
        dto.setMoTa(moTa);

        List<ChiTietDotGiamGiaDTO> chiTietList = new ArrayList<>();
        if (selectedProductIds != null) {
            for (Integer idChiTietSanPham : selectedProductIds) {
                ChiTietDotGiamGiaDTO item = new ChiTietDotGiamGiaDTO();
                item.setIdChiTietSanPham(idChiTietSanPham);
                chiTietList.add(item);
            }
        }
        dto.setChiTietList(chiTietList);
        return dto;
    }

    private List<Integer> extractSelectedIds(DotGiamGiaDTO detail) {
        // Tách lại danh sách id biến thể đã gắn với campaign để tick checkbox khi mở edit/view.
        if (detail == null || detail.getChiTietList() == null) {
            return Collections.emptyList();
        }
        return detail.getChiTietList().stream()
                .map(ChiTietDotGiamGiaDTO::getIdChiTietSanPham)
                .filter(id -> id != null)
                .toList();
    }
}