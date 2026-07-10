package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.response.HoaDonResponse;
import com.example.AerionSports_BE.service.HoaDonService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/hoa-don")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @GetMapping
    public String hienThiDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer loaiHoaDonFilter,
            @RequestParam(required = false) Integer trangThaiFilter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String export,
            Model model,
            HttpServletResponse response
    ) throws IOException {
        if (tuNgay == null) {
            tuNgay = LocalDate.now();
        }
        if (denNgay == null) {
            denNgay = LocalDate.now();
        }
        if ("true".equalsIgnoreCase(export)) {
            xuatExcelHoaDon(keyword, loaiHoaDonFilter, trangThaiFilter, tuNgay, denNgay, response);
            return null;
        }
        Page<HoaDonResponse> pageData = hoaDonService.filterHoaDon(keyword, loaiHoaDonFilter, trangThaiFilter, tuNgay, denNgay, page, size);
        model.addAttribute("listHoaDon", pageData.getContent());
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalElements", pageData.getTotalElements());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("keyword", keyword);
        model.addAttribute("loaiHoaDonFilter", loaiHoaDonFilter);
        model.addAttribute("trangThaiFilter", trangThaiFilter);
        model.addAttribute("tuNgay", tuNgay);
        model.addAttribute("denNgay", denNgay);
        return "hoa-don/index";
    }
    private void xuatExcelHoaDon(
            String keyword, Integer loaiHoaDonFilter, Integer trangThaiFilter,
            LocalDate tuNgay, LocalDate denNgay, HttpServletResponse response
    ) throws IOException {

        List<HoaDonResponse> dsHoaDon = hoaDonService.filterHoaDonKhongPhanTrang(
                keyword, loaiHoaDonFilter, trangThaiFilter, tuNgay, denNgay);

        LocalDateTime thoiDiemXuat = LocalDateTime.now();
        String chuoiThoiGianXuat = thoiDiemXuat.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sach hoa don");

            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            CellStyle titleStyle = workbook.createCellStyle();
            titleStyle.setFont(titleFont);


            int rowIdx = 0;

            Row titleRow = sheet.createRow(rowIdx++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("DANH SÁCH HÓA ĐƠN");
            titleCell.setCellStyle(titleStyle);

            Row exportTimeRow = sheet.createRow(rowIdx++);
            exportTimeRow.createCell(0).setCellValue("Ngày giờ xuất: " + chuoiThoiGianXuat);

            rowIdx++;

            String[] headers = {
                    "STT", "Mã hóa đơn", "Tên nhân viên", "Khách hàng",
                    "Số điện thoại", "Loại hóa đơn", "Tổng tiền", "Ngày tạo", "Trạng thái"
            };
            Row headerRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int stt = 1;
            for (HoaDonResponse hd : dsHoaDon) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stt++);
                row.createCell(1).setCellValue(hd.getMaHoaDon());
                row.createCell(2).setCellValue(hd.getTenNv());
                row.createCell(3).setCellValue(hd.getTenKhachHang());
                row.createCell(4).setCellValue(hd.getSdtKhachHang());
                row.createCell(5).setCellValue(
                        hd.getLoaiHoaDon() != null && hd.getLoaiHoaDon() == 1 ? "Online" : "Tại quầy");
                row.createCell(6).setCellValue(
                        hd.getTongTienThanhToan() != null ? hd.getTongTienThanhToan().doubleValue() : 0.0);
                row.createCell(7).setCellValue(
                        hd.getNgayTao() != null
                                ? hd.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : "");
                row.createCell(8).setCellValue(hd.getTrangThaiName());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            String tenFile = "DanhSachHoaDon_"
                    + thoiDiemXuat.format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")) + ".xlsx";

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + tenFile + "\"");

            workbook.write(response.getOutputStream());
            response.getOutputStream().flush();
        }
    }

    @GetMapping("/chi-tiet/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        HoaDonResponse hoaDon = hoaDonService.detail(id);

        var chiTietSanPham = hoaDonService.getChiTietHoaDon(id);
        var lichSuThanhToan = hoaDonService.getLichSuThanhToan(id);
        var lichSuHoaDon    = hoaDonService.getLichSuHoaDon(id);

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("chiTietSanPham", chiTietSanPham);
        model.addAttribute("lichSuThanhToan", lichSuThanhToan);
        model.addAttribute("lichSuHoaDon", lichSuHoaDon);


        model.addAttribute("danhSachSanPham", chiTietSanPham);

        return "hoa-don/detail";
    }

    @PostMapping("/{id}/chuyen-trang-thai")
    public String chuyenTrangThai(
            @PathVariable Integer id,
            @RequestParam Integer trangThaiMoi,
            @RequestParam(required = false) String ghiChu,
            RedirectAttributes redirectAttributes
    ) {
        try {
            hoaDonService.chuyenTrangThai(id, trangThaiMoi, ghiChu, "admin@example.com");
            redirectAttributes.addFlashAttribute("successMessage", "Chuyển trạng thái thành công!");
            return "redirect:/hoa-don/chi-tiet/" + id;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/hoa-don/chi-tiet/" + id;
        }
    }
}