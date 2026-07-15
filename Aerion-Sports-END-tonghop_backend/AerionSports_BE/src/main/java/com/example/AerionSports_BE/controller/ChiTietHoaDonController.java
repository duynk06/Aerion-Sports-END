package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.service.ChiTietHoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("/chi-tiet-hoa-don")
public class ChiTietHoaDonController {
    @Autowired
    private ChiTietHoaDonService chiTietHoaDonService;
    @GetMapping("/chi-tiet/{id}")
    public ResponseEntity<?> getChiTietHoaDon(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                chiTietHoaDonService.getChiTietHoaDon(id)
        );
    }
}
