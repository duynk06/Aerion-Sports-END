package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.LichSuGiaoCa;
import com.example.AerionSports_BE.service.LichSuGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/giao-ca")
@CrossOrigin("*")
public class LichSuGiaoCaController {

    @Autowired
    private LichSuGiaoCaService giaoCaService;

    @PostMapping("/mo-ca")
    public ResponseEntity<?> moCaMoi(@RequestBody Map<String, Object> body) {
        try {
            Integer idNhanVien = (Integer) body.get("idNhanVien");
            BigDecimal tienBanDau = new BigDecimal(body.get("tienBanDau").toString());

            LichSuGiaoCa result = giaoCaService.moCa(idNhanVien, tienBanDau);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/ca-hien-tai/{idNhanVien}")
    public ResponseEntity<?> getCaHienTai(@PathVariable("idNhanVien") Integer idNhanVien) {
        try {
            Map<String, Object> data = giaoCaService.layThongTinCaHienTai(idNhanVien);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/ket-thuc")
    public ResponseEntity<?> chotGiaoCa(@RequestBody Map<String, Object> body) {
        try {
            Integer idNhanVien = (Integer) body.get("idNhanVien");
            Integer idNhanVienCaSau = (Integer) body.get("idNhanVienCaSau");
            BigDecimal tienMatDoanhThu = new BigDecimal(body.get("tienMatDoanhThu").toString());
            BigDecimal tienChuyenKhoan = new BigDecimal(body.get("tienChuyenKhoan").toString());
            String ghiChu = body.get("ghiChu") != null ? body.get("ghiChu").toString() : "";

            LichSuGiaoCa result = giaoCaService.ketThucCa(idNhanVien, idNhanVienCaSau, tienMatDoanhThu, tienChuyenKhoan, ghiChu);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}