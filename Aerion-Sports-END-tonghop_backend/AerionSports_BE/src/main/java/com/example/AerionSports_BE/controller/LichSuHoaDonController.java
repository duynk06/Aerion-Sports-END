package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.response.LichSuHoaDonResponse;
import com.example.AerionSports_BE.service.LichSuHoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lich-su-hoa-don")
@CrossOrigin("*")
public class LichSuHoaDonController {
    @Autowired
    private LichSuHoaDonService lichSuHoaDonService;

    @GetMapping("/hoa-don/{idHoaDon}")
    public List<LichSuHoaDonResponse> getByHoaDon(
            @PathVariable Integer idHoaDon) {

        return lichSuHoaDonService.getByHoaDon(idHoaDon);
    }
}
