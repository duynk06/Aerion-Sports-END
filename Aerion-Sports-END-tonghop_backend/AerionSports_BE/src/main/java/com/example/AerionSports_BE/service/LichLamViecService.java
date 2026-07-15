package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.LichLamViec;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LichLamViecService {

    List<Map<String, Object>> layLichTrongKhoangNgay(LocalDate tuNgay, LocalDate denNgay);

    LichLamViec xepLichMoi(LichLamViec lich);

    void xoaLich(Integer id);
}