package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.SanPhamPosDTO;
import com.example.AerionSports_BE.dto.request.ThanhToanRequest;
import com.example.AerionSports_BE.dto.request.ThemSanPhamRequest;
import com.example.AerionSports_BE.dto.response.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface BanHangService {

    // BanHangService.java
    BanHangResponse taoHoaDonCho(String username);

    BanHangResponse capNhatKhachHangVaoHoaDon(Integer idHoaDon, Integer idKhachHang);

    BanHangResponse themSanPhamVaoHoaDon(ThemSanPhamRequest request);
    // BanHangService.java — thêm method
    BanHangResponse capNhatSoLuong(Integer idChiTiet, Integer soLuong);
    // BanHangService.java — thêm 2 method
    List<BanHangResponse> getHoaDonCho();


    // BanHangService.java — thêm method
    void xoaChiTietHoaDon(Integer idChiTiet);

    @Transactional
    BanHangResponse thanhToan(ThanhToanRequest request);

    // BanHangServiceImpl.java
    @Transactional
    void huyHoaDon(Integer idHoaDon);

    BanHangResponse capNhatLoaiHoaDon(Integer idHoaDon, Integer loaiHoaDon);

    BanHangResponse capNhatPhiVanChuyen(Integer idHoaDon, BigDecimal phiVanChuyen);
    // BanHangService.java
    List<DiaChiKhachHangResponse> getDiaChiKhachHang(Integer idKhachHang);
    PhieuGiamGiaPosResponse timPhieuGiamGiaTotNhat(Integer idHoaDon);
    BanHangResponse apDungPhieuGiamGia(Integer idHoaDon, Integer idPhieuGiamGia);
    BanHangResponse boPhieuGiamGia(Integer idHoaDon);
    List<KiemTraGiaResponse> kiemTraGiaThayDoi(Integer idHoaDon);
    // BanHangService.java — thêm method
    SanPhamPosDTO timSanPhamTheoMa(String maCtsp);
}