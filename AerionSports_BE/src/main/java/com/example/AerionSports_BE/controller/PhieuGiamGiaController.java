package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.service.PhieuGiamGiaService;
import com.example.AerionSports_BE.service.KhachHangService;
import com.example.AerionSports_BE.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/phieu-giam-gia")
public class PhieuGiamGiaController {

    private final PhieuGiamGiaService service;
    private final KhachHangService khachHangService;

    @Autowired
    private EmailService emailService;

    public PhieuGiamGiaController(PhieuGiamGiaService service, KhachHangService khachHangService) {
        this.service = service;
        this.khachHangService = khachHangService;
    }

    @GetMapping("/hien-thi")
    public String getAll(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(value = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(value = "page", defaultValue = "1") int page,
            Model model) {

        List<PhieuGiamGia> list = service.getAll();

        LocalDate homNay = LocalDate.now();

        // === FILTER ===
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            list = list.stream().filter(p ->
                    (p.getMaPhieuGiamGia() != null && p.getMaPhieuGiamGia().toLowerCase().contains(kw)) ||
                            (p.getTenPhieuGiamGia() != null && p.getTenPhieuGiamGia().toLowerCase().contains(kw))
            ).toList();
        }

        if (trangThai != null) {
            list = list.stream().filter(p -> {
                boolean isActive = p.getTrangThai() != null && p.getTrangThai() == 1;
                boolean notExpired = p.getNgayKetThuc() == null || !p.getNgayKetThuc().toLocalDate().isBefore(homNay);
                boolean started = p.getNgayBatDau() == null || !homNay.isBefore(p.getNgayBatDau().toLocalDate());
                boolean hasStock = p.getSoLuong() > (p.getSoLuongDaSuDung() != null ? p.getSoLuongDaSuDung() : 0);

                if (trangThai == 1) return isActive && notExpired && started && hasStock;
                if (trangThai == 2) return isActive && notExpired && !started && hasStock;
                if (trangThai == 0) return !isActive || !notExpired || !hasStock;
                return true;
            }).toList();
        }

        if (tuNgay != null) {
            list = list.stream().filter(p -> p.getNgayBatDau() != null &&
                    !p.getNgayBatDau().toLocalDate().isBefore(tuNgay)).toList();
        }
        if (denNgay != null) {
            list = list.stream().filter(p -> p.getNgayKetThuc() != null &&
                    !p.getNgayKetThuc().toLocalDate().isAfter(denNgay)).toList();
        }

        // === PHÂN TRANG ===
        int pageSize = 5;
        int totalItems = list.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<PhieuGiamGia> pagedList = (fromIndex < totalItems)
                ? list.subList(fromIndex, toIndex)
                : Collections.emptyList();

        model.addAttribute("listPhieuGiamGia", pagedList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalItems);

        return "voucher/danh-sach";
    }
    @GetMapping("/form-them")
    public String showFormAdd(Model model) {
        PhieuGiamGia phieuGiamGia = new PhieuGiamGia();

        List<PhieuGiamGia> dsPhieu = service.getAll();
        int maxNumber = 0;

        for (PhieuGiamGia p : dsPhieu) {
            if (p.getMaPhieuGiamGia() != null && p.getMaPhieuGiamGia().startsWith("PGG")) {
                try {
                    int num = Integer.parseInt(p.getMaPhieuGiamGia().replace("PGG", ""));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int tiepTheo = maxNumber + 1;
        String formatMa = "PGG" + (tiepTheo < 10 ? "0" + tiepTheo : tiepTheo);
        phieuGiamGia.setMaPhieuGiamGia(formatMa);

        model.addAttribute("phieuGiamGia", phieuGiamGia);
        model.addAttribute("khachHangList", khachHangService.getAll());
        return "voucher/form-them";
    }

    @PostMapping("/luu")
    public String add(@ModelAttribute("phieuGiamGia") PhieuGiamGia phieuGiamGia,
                      @RequestParam("doiTuongApDung") String doiTuongApDung,
                      @RequestParam(value = "khachHangIds", required = false) List<Integer> khachHangIds,
                      RedirectAttributes redirectAttributes) {
        try {
            phieuGiamGia.setTrangThai(1);
            service.addVoucherVoiKhachHang(phieuGiamGia, khachHangIds);

            if ("PERSONAL".equals(doiTuongApDung) && khachHangIds != null && !khachHangIds.isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                String ngayChotHan = phieuGiamGia.getNgayKetThuc().format(formatter);

                String chuoiGiaTriGiam = "";
                if ("Sale %".equals(phieuGiamGia.getLoaiPhieuGiamGia())) {
                    chuoiGiaTriGiam = phieuGiamGia.getGiaTriGiam() + "%";
                } else {
                    DecimalFormat df = new DecimalFormat("#,###");
                    chuoiGiaTriGiam = df.format(phieuGiamGia.getGiaTriGiam()) + " đ";
                }

                for (Integer idKh : khachHangIds) {
                    KhachHang kh = khachHangService.getById(idKh);
                    if (kh != null && kh.getEmail() != null && !kh.getEmail().trim().isEmpty()) {

                        emailService.sendVoucherEmail(
                                kh.getEmail(),
                                kh.getHoTen(),
                                phieuGiamGia.getMaPhieuGiamGia(),
                                chuoiGiaTriGiam,
                                ngayChotHan
                        );
                    }
                }
            }
            redirectAttributes.addFlashAttribute("successMessage", "Thêm phiếu giảm giá mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Thêm phiếu giảm giá thất bại: " + e.getMessage());
        }
        return "redirect:/phieu-giam-gia/hien-thi";
    }

    @GetMapping("/form-sua/{id}")
    public String showFormUpdate(@PathVariable Integer id, Model model) {
        PhieuGiamGia phieuGiamGia = service.getById(id);
        model.addAttribute("phieuGiamGia", phieuGiamGia);
        model.addAttribute("khachHangList", khachHangService.getAll());
        model.addAttribute("selectedKhachHangIds", Collections.emptyList());
        return "voucher/form-sua";
    }

    @PostMapping("/cap-nhat/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("phieuGiamGia") PhieuGiamGia phieuGiamGia,
                         @RequestParam(value = "khachHangIds", required = false) List<Integer> khachHangIds,
                         RedirectAttributes redirectAttributes) {
        try {
            service.updateVoucherVoiKhachHang(id, phieuGiamGia, khachHangIds);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thông tin phiếu thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật thất bại: " + e.getMessage());
        }
        return "redirect:/phieu-giam-gia/hien-thi";
    }

    @GetMapping("/chi-tiet/{id}")
    public String showDetail(@PathVariable Integer id, Model model) {
        PhieuGiamGia phieuGiamGia = service.getById(id);
        model.addAttribute("phieuGiamGia", phieuGiamGia);
        model.addAttribute("khachHangList", khachHangService.getAll());
        return "voucher/form-chi-tiet";
    }

    @GetMapping("/toggle/{id}")
    public String toggleStatus(
            @PathVariable("id") Integer id,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "tuNgay", required = false) String tuNgay,
            @RequestParam(value = "denNgay", required = false) String denNgay) {

        PhieuGiamGia phieu = service.getById(id);
        if (phieu != null) {
            phieu.setTrangThai(phieu.getTrangThai() == 1 ? 0 : 1);
            service.update(id, phieu);
        }
        StringBuilder redirectUrl = new StringBuilder("redirect:/phieu-giam-gia/hien-thi?page=" + page);
        if (keyword != null && !keyword.trim().isEmpty()) redirectUrl.append("&keyword=").append(keyword);
        if (trangThai != null) redirectUrl.append("&trangThai=").append(trangThai);
        if (tuNgay != null && !tuNgay.trim().isEmpty()) redirectUrl.append("&tuNgay=").append(tuNgay);
        if (denNgay != null && !denNgay.trim().isEmpty()) redirectUrl.append("&denNgay=").append(denNgay);

        return redirectUrl.toString();
    }
}