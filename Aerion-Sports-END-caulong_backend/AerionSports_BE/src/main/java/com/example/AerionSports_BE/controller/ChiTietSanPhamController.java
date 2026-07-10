package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.request.ChiTietSanPhamFilter;
import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.service.ChiTietSanPhamService;
import com.example.AerionSports_BE.service.SanPhamService; // ⚡ THÊM IMPORT
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@CrossOrigin("*")
public class  ChiTietSanPhamController {

    @Autowired
    private ChiTietSanPhamService service;

    @Autowired
    private SanPhamService sanPhamService; // ⚡ TIÊM SAN PHAM SERVICE ĐỂ LẤY HÀM MAP CHUẨN

    @GetMapping("/all")
    public ResponseEntity<List<ChiTietSanPhamResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // ⚡ ĐÃ SỬA: Ép dữ liệu phân trang chạy qua hàm toChiTietRes của SanPhamService để hiển thị chuẩn mã cha và ảnh UUID
    @GetMapping("/search")
    public ResponseEntity<Page<ChiTietSanPhamResponse>> search(@ModelAttribute ChiTietSanPhamFilter f) {
        // Gọi thẳng hàm search của service (Hàm này đã tự map qua toRes xịn rồi)
        return ResponseEntity.ok(service.search(f));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ChiTietSanPhamRequest r) {
        return ResponseEntity.ok(service.save(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody ChiTietSanPhamRequest r) {
        return ResponseEntity.ok(service.update(id, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Xóa mềm biến thể sản phẩm thành công!");
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id, @RequestParam("trangThai") Integer trangThai) {
        service.updateTrangThai(id, trangThai);
        return ResponseEntity.ok("Cập nhật trạng thái biến thể thành công!");
    }

    @GetMapping("/all-for-check")
    public ResponseEntity<?> getAllForCheck() {
        try {
            // Gọi xuống Service lấy toàn bộ danh sách sản phẩm cha kèm biến thể con
            // (Hàm này tương tự hàm search nhưng không truyền Pageable và trả về List phẳng)
            List<?> danhSachToanBo = service.getAllProductsWithVariantsForCheck();

            return ResponseEntity.ok(danhSachToanBo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy dữ liệu cấu hình hệ thống: " + e.getMessage());
        }
    }
}