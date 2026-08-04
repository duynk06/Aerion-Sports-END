package com.example.AerionSports_BE.controller;


import com.example.AerionSports_BE.dto.request.DanhMucRequest;
import com.example.AerionSports_BE.dto.response.DanhMucResponse;
import com.example.AerionSports_BE.service.DanhMucService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danh-muc")
@CrossOrigin("*")
public class DanhMucController {

    @Autowired
    private DanhMucService service;

    @GetMapping("/all")
    public ResponseEntity<List<DanhMucResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "trangThai", required = false) Integer trangThai
    ) {
        return ResponseEntity.ok(service.search(page, size, trangThai));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody DanhMucRequest r) {
        return ResponseEntity.ok(service.save(r));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody DanhMucRequest r) {
        return ResponseEntity.ok(service.update(id, r));
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
