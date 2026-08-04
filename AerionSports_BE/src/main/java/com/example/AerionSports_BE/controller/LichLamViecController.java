package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.CaLamViec;
import com.example.AerionSports_BE.entity.LichLamViec;
import com.example.AerionSports_BE.service.LichLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lich-lam-viec")
@CrossOrigin("*")
public class LichLamViecController {

    @Autowired
    private LichLamViecService lichLamViecService;

    @GetMapping("/danh-sach")
    public ResponseEntity<?> getLich(
            @RequestParam("tuNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam("denNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(lichLamViecService.layLichTrongKhoangNgay(tuNgay, denNgay));
    }

    @PostMapping("/xep-lich")
    public ResponseEntity<?> taoLichMoi(@RequestBody Map<String, Object> body) {
        try {
            Integer idNhanVien = (Integer) body.get("idNhanVien");
            String ngayLamViecStr = body.get("ngayLamViec").toString();
            LocalDate ngayLamViec = LocalDate.parse(ngayLamViecStr);

            Map<String, Object> caLamViecMap = (Map<String, Object>) body.get("caLamViec");
            Integer idCaLamViec = (Integer) caLamViecMap.get("id");

            LichLamViec lichMoi = new LichLamViec();
            lichMoi.setIdNhanVien(idNhanVien);
            lichMoi.setNgayLamViec(ngayLamViec);
            lichMoi.setTrangThai(1);

            CaLamViec caGiaLap = new CaLamViec();
            caGiaLap.setId(idCaLamViec);
            lichMoi.setCaLamViec(caGiaLap);

            LichLamViec result = lichLamViecService.xepLichMoi(lichMoi);
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", "Sai định dạng dữ liệu đầu vào hoặc lỗi hệ thống!"));
        }
    }

    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<?> xoaLichLamViec(@PathVariable("id") Integer id) {
        try {
            lichLamViecService.xoaLich(id);
            return ResponseEntity.ok(Map.of("message", "Xóa lịch làm việc thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Không thể xóa lịch làm việc!"));
        }
    }
}