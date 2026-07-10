package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.VoucherRequestDTO;
import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.service.PhieuGiamGiaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/phieu-giam-gia")
@CrossOrigin("*")
public class PhieuGiamGiaController {

    private final PhieuGiamGiaService service;

    public PhieuGiamGiaController(PhieuGiamGiaService service) {
        this.service = service;
    }

    @GetMapping("/hien-thi")
    public ResponseEntity<List<PhieuGiamGia>> getAll() {
        List<PhieuGiamGia> list = service.getAll();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<PhieuGiamGia> add(@RequestBody VoucherRequestDTO dto) {
        // Gọi thẳng Service nhận DTO để bóc tách mảng IDs và lưu 2 bảng kết hợp
        return ResponseEntity.ok(service.add(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhieuGiamGia> update(@PathVariable Integer id, @RequestBody PhieuGiamGia pgg) {
        return ResponseEntity.ok(service.update(id, pgg));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}