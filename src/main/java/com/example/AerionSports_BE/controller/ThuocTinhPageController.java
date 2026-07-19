package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.*;
import com.example.AerionSports_BE.dto.response.*;
import com.example.AerionSports_BE.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/thuoc-tinh")
public class ThuocTinhPageController {

    @Autowired private DanhMucService danhMucService;
    @Autowired private ThuongHieuService thuongHieuService;
    @Autowired private XuatXuService xuatXuService;
    @Autowired private MauSacService mauSacService;
    @Autowired private TrongLuongService trongLuongService;
    @Autowired private DoCungService doCungService;
    @Autowired private DiemCanBangService diemCanBangService;
    @Autowired private ChuViCanVotService chuViCanVotService;
    @Autowired private ChatLieuThanVotService chatLieuThanVotService;
    @Autowired private ChatLieuKhungVotService chatLieuKhungVotService;

    // 1. HIỂN THỊ DANH SÁCH + PHÂN TRANG + BÁO TỔNG SỐ
    @GetMapping("/{loai}")
    public String hienThiTrangThuocTinh(
            @PathVariable String loai,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "5") int size,
            Model model) {

        String title = "";
        List<Map<String, Object>> unifiedList = new ArrayList<>();

        switch (loai) {
            case "danh-muc":
                title = "Danh mục";
                for (DanhMucResponse x : danhMucService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaDanhMuc() != null ? x.getMaDanhMuc() : "", "ten", x.getTenDanhMuc() != null ? x.getTenDanhMuc() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "thuong-hieu":
                title = "Thương hiệu";
                for (ThuongHieuResponse x : thuongHieuService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaThuongHieu() != null ? x.getMaThuongHieu() : "", "ten", x.getTenThuongHieu() != null ? x.getTenThuongHieu() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "xuat-xu":
                title = "Xuất xứ";
                for (XuatXuResponse x : xuatXuService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaXuatXu() != null ? x.getMaXuatXu() : "", "ten", x.getTenXuatXu() != null ? x.getTenXuatXu() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "mau-sac":
                title = "Màu sắc";
                for (MauSacResponse x : mauSacService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaMauSac() != null ? x.getMaMauSac() : "", "ten", x.getTenMauSac() != null ? x.getTenMauSac() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "trong-luong":
                title = "Trọng lượng";
                for (TrongLuongResponse x : trongLuongService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaTrongLuong() != null ? x.getMaTrongLuong() : "", "ten", x.getTenTrongLuong() != null ? x.getTenTrongLuong() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "do-cung":
                title = "Độ cứng thân";
                for (DoCungResponse x : doCungService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaDoCung() != null ? x.getMaDoCung() : "", "ten", x.getTenDoCung() != null ? x.getTenDoCung() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "diem-can-bang":
                title = "Điểm cân bằng";
                for (DiemCanBangResponse x : diemCanBangService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaDiemCanBang() != null ? x.getMaDiemCanBang() : "", "ten", x.getTenDiemCanBang() != null ? x.getTenDiemCanBang() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "chu-vi-can":
                title = "Chu vi cán";
                for (com.example.AerionSports_BE.entity.ChuViCanVot x : chuViCanVotService.getAllActive()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaChuViCanVot() != null ? x.getMaChuViCanVot() : "", "ten", x.getTenChuViCanVot() != null ? x.getTenChuViCanVot() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "chat-lieu-than":
                title = "Chất liệu thân";
                for (var x : chatLieuThanVotService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaChatLieuThanVot() != null ? x.getMaChatLieuThanVot() : "", "ten", x.getTenChatLieuThanVot() != null ? x.getTenChatLieuThanVot() : "", "trangThai", x.getTrangThai()));
                }
                break;
            case "chat-lieu-khung":
                title = "Chất liệu khung";
                for (ChatLieuKhungVotResponse x : chatLieuKhungVotService.getAll()) {
                    unifiedList.add(Map.of("id", x.getId(), "ma", x.getMaChatLieuKhungVot() != null ? x.getMaChatLieuKhungVot() : "", "ten", x.getTenChatLieuKhungVot() != null ? x.getTenChatLieuKhungVot() : "", "trangThai", x.getTrangThai()));
                }
                break;
            default:
                return "error/404";
        }

        int totalElements = unifiedList.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        if (totalPages == 0) totalPages = 1;

        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        int start = page * size;
        int end = Math.min(start + size, totalElements);

        List<Map<String, Object>> pagedList = new ArrayList<>();
        if (start < totalElements) {
            pagedList = unifiedList.subList(start, end);
        }

        model.addAttribute("title", title);
        model.addAttribute("apiPath", loai);
        model.addAttribute("dataList", pagedList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalElements", totalElements);
        model.addAttribute("pageSize", size);

        return "thuoc-tinh/thuoc-tinh-view";
    }

    // 2. XỬ LÝ LƯU (THÊM MỚI / CẬP NHẬT) - ĐÃ CẬP NHẬT TỰ TĂNG MÃ CHUẨN
    @PostMapping("/{loai}/luu")
    public String luuThuocTinh(
            @PathVariable String loai,
            @RequestParam(value = "id", required = false) Integer id,
            @RequestParam("tenThuocTinh") String ten,
            @RequestParam(value = "maThuocTinh", required = false) String ma) {

        String value = ten.trim();
        boolean isThemMoi = (id == null || id.toString().isEmpty());

        switch (loai) {
            case "danh-muc":
                String maDm = isThemMoi ? "DM" + String.format("%03d", danhMucService.getAll().size() + 1) : ma;
                DanhMucRequest dmr = new DanhMucRequest(); dmr.setTenDanhMuc(value); dmr.setMaDanhMuc(maDm); dmr.setTrangThai(1);
                if (isThemMoi) danhMucService.save(dmr); else danhMucService.update(id, dmr);
                break;

            case "thuong-hieu":
                String maTh = isThemMoi ? "TH" + String.format("%03d", thuongHieuService.getAll().size() + 1) : ma;
                ThuongHieuRequest thr = new ThuongHieuRequest(); thr.setTenThuongHieu(value); thr.setMaThuongHieu(maTh); thr.setTrangThai(1);
                if (isThemMoi) thuongHieuService.save(thr); else thuongHieuService.update(id, thr);
                break;

            case "xuat-xu":
                String maXx = isThemMoi ? "XX" + String.format("%03d", xuatXuService.getAll().size() + 1) : ma;
                XuatXuRequest xxr = new XuatXuRequest(); xxr.setTenXuatXu(value); xxr.setMaXuatXu(maXx); xxr.setTrangThai(1);
                if (isThemMoi) xuatXuService.save(xxr); else xuatXuService.update(id, xxr);
                break;

            case "mau-sac":
                String maMs = isThemMoi ? "MS" + String.format("%03d", mauSacService.getAll().size() + 1) : ma;
                MauSacRequest msr = new MauSacRequest(); msr.setTenMauSac(value); msr.setMaMauSac(maMs); msr.setTrangThai(1);
                if (isThemMoi) mauSacService.save(msr); else mauSacService.update(id, msr);
                break;

            case "trong-luong":
                String maTl = isThemMoi ? "TL" + String.format("%03d", trongLuongService.getAll().size() + 1) : ma;
                TrongLuongRequest tlr = new TrongLuongRequest(); tlr.setTenTrongLuong(value); tlr.setMaTrongLuong(maTl); tlr.setTrangThai(1);
                if (isThemMoi) trongLuongService.save(tlr); else trongLuongService.update(id, tlr);
                break;

            case "do-cung":
                String maDc = isThemMoi ? "DC" + String.format("%03d", doCungService.getAll().size() + 1) : ma;
                DoCungRequest dcr = new DoCungRequest(); dcr.setTenDoCung(value); dcr.setMaDoCung(maDc); dcr.setTrangThai(1);
                if (isThemMoi) doCungService.save(dcr); else doCungService.update(id, dcr);
                break;

            case "diem-can-bang":
                String maDcb = isThemMoi ? "DCB" + String.format("%03d", diemCanBangService.getAll().size() + 1) : ma;
                DiemCanBangRequest dcbr = new DiemCanBangRequest(); dcbr.setTenDiemCanBang(value); dcbr.setMaDiemCanBang(maDcb); dcbr.setTrangThai(1);
                if (isThemMoi) diemCanBangService.save(dcbr); else diemCanBangService.update(id, dcbr);
                break;

            case "chu-vi-can":
                String maCv = isThemMoi ? "CV" + String.format("%03d", chuViCanVotService.getAllActive().size() + 1) : ma;
                ChuViCanVotRequest cvr = new ChuViCanVotRequest(); cvr.setTenChuViCanVot(value); cvr.setMaChuViCanVot(maCv); cvr.setTrangThai(1);
                if (isThemMoi) chuViCanVotService.save(cvr); else chuViCanVotService.update(id, cvr);
                break;

            case "chat-lieu-than":
                String maClt = isThemMoi ? "CLT" + String.format("%03d", chatLieuThanVotService.getAll().size() + 1) : ma;
                ChatLieuThanVotRequest cltr = new ChatLieuThanVotRequest(); cltr.setTenChatLieuThanVot(value); cltr.setMaChatLieuThanVot(maClt); cltr.setTrangThai(1);
                if (isThemMoi) chatLieuThanVotService.save(cltr); else chatLieuThanVotService.update(id, cltr);
                break;

            case "chat-lieu-khung":
                String maClk = isThemMoi ? "CLK" + String.format("%03d", chatLieuKhungVotService.getAll().size() + 1) : ma;
                ChatLieuKhungVotRequest clkr = new ChatLieuKhungVotRequest(); clkr.setTenChatLieuKhungVot(value); clkr.setMaChatLieuKhungVot(maClk); clkr.setTrangThai(1);
                if (isThemMoi) chatLieuKhungVotService.save(clkr); else chatLieuKhungVotService.update(id, clkr);
                break;
        }

        return "redirect:/thuoc-tinh/" + loai;
    }

    // 3. ĐỔI TRẠNG THÁI HOẠT ĐỘNG
    @GetMapping("/{loai}/doi-trang-thai/{id}")
    public String doiTrangThaiThuocTinh(
            @PathVariable String loai,
            @PathVariable Integer id,
            @RequestParam("trangThaiHienTai") Integer trangThaiHienTai) {

        int trangThaiMoi = (trangThaiHienTai == 1) ? 0 : 1;

        switch (loai) {
            case "danh-muc": danhMucService.updateTrangThai(id, trangThaiMoi); break;
            case "thuong-hieu": thuongHieuService.updateTrangThai(id, trangThaiMoi); break;
            case "xuat-xu": xuatXuService.updateTrangThai(id, trangThaiMoi); break;
            case "mau-sac": mauSacService.updateTrangThai(id, trangThaiMoi); break;
            case "trong-luong": trongLuongService.updateTrangThai(id, trangThaiMoi); break;
            case "do-cung": doCungService.updateTrangThai(id, trangThaiMoi); break;
            case "diem-can-bang": diemCanBangService.updateTrangThai(id, trangThaiMoi); break;
            case "chu-vi-can": chuViCanVotService.updateTrangThai(id, trangThaiMoi); break;
            case "chat-lieu-than": chatLieuThanVotService.updateTrangThai(id, trangThaiMoi); break;
            case "chat-lieu-khung": chatLieuKhungVotService.updateTrangThai(id, trangThaiMoi); break;
        }

        return "redirect:/thuoc-tinh/" + loai;
    }
}