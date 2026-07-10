package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.response.LichSuThanhToanResponse;
import com.example.AerionSports_BE.service.LichSuThanhToanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lich-su-thanh-toan")
@CrossOrigin("*")
public class LichSuThanhToanController {

    @Autowired
    private  LichSuThanhToanService lichSuThanhToanService;

    @GetMapping("/hoa-don/{idHoaDon}")
    public List<LichSuThanhToanResponse>
    getByHoaDon(
            @PathVariable Integer idHoaDon
    ) {
        return lichSuThanhToanService.getByHoaDon(idHoaDon);
    }
}