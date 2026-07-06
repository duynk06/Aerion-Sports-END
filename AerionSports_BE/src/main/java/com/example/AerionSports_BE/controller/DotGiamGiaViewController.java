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
        page = Math.max(page, 0);
        size = Math.max(size, 1);

        try {
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
            DotGiamGiaDTO dto = buildDto(tenDotGiamGia, giaTriGiam, ngayBatDau, ngayKetThuc, moTa, selectedProductIds);
            DotGiamGiaDTO created = dotGiamGiaService.create(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo đợt giảm giá " + created.getMaDotGiamGia() + " thành công.");
            return "redirect:/dot-giam-gia";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-giam-gia?action=create";
        }
    }

    @GetMapping("/sua/{id}")
    public String editForm(@PathVariable Integer id) {
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
            DotGiamGiaDTO dto = buildDto(tenDotGiamGia, giaTriGiam, ngayBatDau, ngayKetThuc, moTa, selectedProductIds);
            dotGiamGiaService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật đợt giảm giá thành công.");
            return "redirect:/dot-giam-gia";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/dot-giam-gia?action=edit&id=" + id;
        }
    }

    @GetMapping("/xem/{id}")
    public String detail(@PathVariable Integer id) {
        return "redirect:/dot-giam-gia?action=view&id=" + id;
    }

    @PostMapping("/{id}/trang-thai")
    public String updateStatus(
            @PathVariable Integer id,
            @RequestParam Integer trangThai,
            RedirectAttributes redirectAttributes
    ) {
        try {
            dotGiamGiaService.updateTrangThai(id, trangThai);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đợt giảm giá.");
        } catch (RuntimeException e) {
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
            dotGiamGiaService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã hủy đợt giảm giá thành công.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/dot-giam-gia";
    }

    private void prepareModalModel(Model model, String action, Integer id) {
        String resolvedAction = action == null ? "" : action.trim().toLowerCase();
        String viewMode = resolvedAction.isEmpty() ? "list" : resolvedAction;
        model.addAttribute("viewMode", viewMode);
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

        if (!"create".equals(viewMode) && !"edit".equals(viewMode) && !"view".equals(viewMode)) {
            return;
        }

        List<?> groupedProducts = dotGiamGiaService.getGroupedProductsForSelection(null);
        List<ChiTietDotGiamGiaDTO> products = dotGiamGiaService.getProductsForSelection(null);
        model.addAttribute("groupedProducts", groupedProducts != null ? groupedProducts : Collections.emptyList());
        model.addAttribute("products", products);

        if ("create".equals(viewMode)) {
            model.addAttribute("form", new DotGiamGiaDTO());
            model.addAttribute("selectedProductIds", Collections.emptyList());
            model.addAttribute("selectedProducts", Collections.emptyList());
            model.addAttribute("detailProducts", Collections.emptyList());
            model.addAttribute("formAction", "/dot-giam-gia/them");
            return;
        }

        if (id == null) {
            model.addAttribute("errorMessage", "Thiếu ID đợt giảm giá");
            model.addAttribute("viewMode", "");
            return;
        }

        DotGiamGiaDTO detail;
        try {
            detail = dotGiamGiaService.getById(id);
        } catch (RuntimeException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("viewMode", "");
            return;
        }
        List<Integer> selectedIds = extractSelectedIds(detail);

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
        if (detail == null || detail.getChiTietList() == null) {
            return Collections.emptyList();
        }
        return detail.getChiTietList().stream()
                .map(ChiTietDotGiamGiaDTO::getIdChiTietSanPham)
                .filter(id -> id != null)
                .toList();
    }
}
