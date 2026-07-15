package com.example.AerionSports_BE.controller;


import com.example.AerionSports_BE.dto.request.ChuViCanVotFilter;
import com.example.AerionSports_BE.dto.request.ChuViCanVotRequest;
import com.example.AerionSports_BE.entity.ChuViCanVot;
import com.example.AerionSports_BE.service.ChuViCanVotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chu-vi-can-vot")
@CrossOrigin("*")
public class ChuViCanVotController {
    @Autowired
    private ChuViCanVotService service;

    // 1. Lấy tất cả chu vi đang hoạt động (Hiển thị lên combobox/select ở màn CTSP)
    @GetMapping("/active")
    public ResponseEntity<List<ChuViCanVot>> getAllActive() {
        return ResponseEntity.ok(service.getAllActive());
    }

    // 2. Tìm kiếm, lọc và phân trang (Hiển thị lên bảng danh mục Quản lý chu vi cán)
    @GetMapping("/search")
    public ResponseEntity<Page<ChuViCanVot>> search(@ModelAttribute ChuViCanVotFilter filter) {
        return ResponseEntity.ok(service.search(filter));
    }

    // 3. Lấy chi tiết 1 bản ghi theo ID
    @GetMapping("/{id}")
    public ResponseEntity<ChuViCanVot> getOne(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // 4. Thêm mới danh mục
    @PostMapping("/add")
    public ResponseEntity<?> create(@Valid @RequestBody ChuViCanVotRequest request) {
        try {
            return ResponseEntity.ok(service.save(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Cập nhật danh mục
    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody ChuViCanVotRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🟢 THÊM MỚI: API phục vụ hành động chuyển đổi công tắc nhanh (Xóa mềm) ngoài bảng hiển thị
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateTrangThai(@PathVariable Integer id, @RequestParam Integer trangThai) {
        try {
            service.updateTrangThai(id, trangThai);
            return ResponseEntity.ok("Cập nhật trạng thái xóa mềm thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🔴 SỬA LẠI: API phục vụ hành động nhấn nút Thùng rác (Xóa cứng hoàn toàn khỏi DB)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.ok("Xóa cứng thuộc tính vĩnh viễn khỏi Database thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa cứng do thuộc tính này đang gắn liền với biến thể sản phẩm!");
        }
    }
}
