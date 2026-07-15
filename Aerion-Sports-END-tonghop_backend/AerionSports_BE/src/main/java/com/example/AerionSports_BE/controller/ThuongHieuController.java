package com.example.AerionSports_BE.controller;


import com.example.AerionSports_BE.dto.request.ThuongHieuRequest;
import com.example.AerionSports_BE.dto.response.ThuongHieuResponse;
import com.example.AerionSports_BE.service.ThuongHieuService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/thuong-hieu")
@CrossOrigin("*")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService service;

    @GetMapping("/all")
    public ResponseEntity<List<ThuongHieuResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "trangThai", required = false) Integer trangThai,
            @RequestParam(value = "keyword", required = false) String keyword // Đồng bộ tham số tìm kiếm
    ) {
        Page<ThuongHieuResponse> result = service.search(page, size, trangThai, keyword);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ThuongHieuResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ThuongHieuRequest request) {
        try {
            return ResponseEntity.ok(service.save(request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody ThuongHieuRequest request) {
        try {
            return ResponseEntity.ok(service.update(id, request));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getLocalizedMessage());
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