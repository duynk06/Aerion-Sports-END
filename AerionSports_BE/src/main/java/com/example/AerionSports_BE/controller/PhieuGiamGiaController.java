package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.service.PhieuGiamGiaService;
import com.example.AerionSports_BE.service.KhachHangService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/phieu-giam-gia")
public class PhieuGiamGiaController {

    private final PhieuGiamGiaService service;
    private final KhachHangService khachHangService;

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

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.toLowerCase().trim();
            list = list.stream().filter(p ->
                    (p.getMaPhieuGiamGia() != null && p.getMaPhieuGiamGia().toLowerCase().contains(kw)) ||
                            (p.getTenPhieuGiamGia() != null && p.getTenPhieuGiamGia().toLowerCase().contains(kw))
            ).toList();
        }

        if (trangThai != null) {
            if (trangThai == 1) {
                list = list.stream().filter(p -> p.getTrangThai() != null && p.getTrangThai() == 1
                        && (p.getNgayKetThuc() == null || !p.getNgayKetThuc().toLocalDate().isBefore(homNay))).toList();
            } else if (trangThai == 0) {
                list = list.stream().filter(p -> p.getTrangThai() != null && (p.getTrangThai() == 0
                        || (p.getTrangThai() == 1 && p.getNgayKetThuc() != null && p.getNgayKetThuc().toLocalDate().isBefore(homNay)))).toList();
            }
        }

        if (tuNgay != null) {
            list = list.stream().filter(p -> p.getNgayBatDau() != null && !p.getNgayBatDau().toLocalDate().isBefore(tuNgay)).toList();
        }

        if (denNgay != null) {
            list = list.stream().filter(p -> p.getNgayKetThuc() != null && !p.getNgayKetThuc().toLocalDate().isAfter(denNgay)).toList();
        }

        int pageSize = 5;
        int totalItems = list.size();
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        if (page < 1) page = 1;
        if (page > totalPages && totalPages > 0) page = totalPages;

        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);

        List<PhieuGiamGia> pagedList = Collections.emptyList();
        if (fromIndex < totalItems) {
            pagedList = list.subList(fromIndex, toIndex);
        }

        model.addAttribute("listPhieuGiamGia", pagedList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
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
                      @RequestParam(value = "khachHangIds", required = false) List<Integer> khachHangIds) {
        service.add(phieuGiamGia);
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
                         @RequestParam(value = "khachHangIds", required = false) List<Integer> khachHangIds) {
        service.update(id, phieuGiamGia);
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