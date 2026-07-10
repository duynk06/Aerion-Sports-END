package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.response.ChiTietHoaDonResponse;
import com.example.AerionSports_BE.dto.response.HoaDonResponse;
import com.example.AerionSports_BE.dto.response.LichSuHoaDonResponse;
import com.example.AerionSports_BE.dto.response.LichSuThanhToanResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

import java.time.LocalDate;
import java.util.List;

public interface HoaDonService {
    List<HoaDonResponse> hienThi();

    List<HoaDonResponse> search(String keyword);

    // Đã cập nhật loaiHoaDon từ String sang Integer để khớp với DB
    Page<HoaDonResponse> filterHoaDon(
            String keyword,
            Integer loaiHoaDon,
            Integer trangThai,
            LocalDate tuNgay,
            LocalDate denNgay,
            int page,
            int size
    );

    HoaDonResponse detail(Integer id);
    // HoaDonService.java
    HoaDonResponse chuyenTrangThai(Integer id, Integer trangThaiMoi, String ghiChu, String username);
    List<ChiTietHoaDonResponse> getChiTietHoaDon(Integer idHoaDon);

    List<LichSuThanhToanResponse> getLichSuThanhToan(Integer idHoaDon);

    List<LichSuHoaDonResponse> getLichSuHoaDon(Integer idHoaDon);
    List<HoaDonResponse> filterHoaDonKhongPhanTrang(
            String keyword, Integer loaiHoaDon, Integer trangThai,
            LocalDate tuNgay, LocalDate denNgay
    );
}