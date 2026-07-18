package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.repository.ChiTietSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/san-pham")
@CrossOrigin("*")
public class SanPhamPublicController {

    @Autowired private ChiTietSanPhamRepository chiTietSanPhamRepository;

    // Xem danh sách sản phẩm đang bán (trangThai = 1), có tìm kiếm theo từ khoá
    @GetMapping
    public ResponseEntity<?> danhSachSanPham(@RequestParam(required = false) String keyword) {
        List<ChiTietSanPham> ds = chiTietSanPhamRepository.searchActiveProducts(keyword);
        return ResponseEntity.ok(ds);
    }

    // Xem chi tiết 1 biến thể sản phẩm
    @GetMapping("/{id}")
    public ResponseEntity<?> chiTietSanPham(@PathVariable Integer id) {
        return chiTietSanPhamRepository.findById(id)
                .filter(ct -> ct.getTrangThai() != null && ct.getTrangThai() == 1)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(java.util.Map.of("message", "Không tìm thấy sản phẩm!")));
    }
}