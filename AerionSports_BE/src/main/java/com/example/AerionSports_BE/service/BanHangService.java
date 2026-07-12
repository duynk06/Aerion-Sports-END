package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.SanPhamPosDTO;
import com.example.AerionSports_BE.dto.request.DiaChiRequest;
import com.example.AerionSports_BE.dto.request.ThanhToanRequest;
import com.example.AerionSports_BE.dto.request.ThemSanPhamRequest;
import com.example.AerionSports_BE.dto.response.*;

import java.math.BigDecimal;
import java.util.List;

public interface BanHangService {

    BanHangResponse taoHoaDonCho(String username);
    BanHangResponse capNhatKhachHangVaoHoaDon(Integer idHoaDon, Integer idKhachHang);
    BanHangResponse themSanPhamVaoHoaDon(ThemSanPhamRequest request);
    BanHangResponse capNhatSoLuong(Integer idChiTiet, Integer soLuong);
    List<BanHangResponse> getHoaDonCho();
    void xoaChiTietHoaDon(Integer idChiTiet);
    BanHangResponse thanhToan(ThanhToanRequest request);
    void huyHoaDon(Integer idHoaDon);
    BanHangResponse capNhatLoaiHoaDon(Integer idHoaDon, Integer loaiHoaDon);
    BanHangResponse capNhatPhiVanChuyen(Integer idHoaDon, BigDecimal phiVanChuyen);
    List<DiaChiKhachHangResponse> getDiaChiKhachHang(Integer idKhachHang);
    PhieuGiamGiaPosResponse timPhieuGiamGiaTotNhat(Integer idHoaDon);
    BanHangResponse apDungPhieuGiamGia(Integer idHoaDon, Integer idPhieuGiamGia);
    List<KiemTraGiaResponse> kiemTraGiaThayDoi(Integer idHoaDon);
    SanPhamPosDTO timSanPhamTheoMa(String maCtsp);
    List<DiaChiKhachHangResponse> themDiaChiKhachHang(Integer idKhachHang, DiaChiRequest request);
    List<DiaChiKhachHangResponse> capNhatDiaChiKhachHang(Integer idDiaChi, DiaChiRequest request);
    BanHangResponse capNhatDiaChiGiaoHang(Integer idHoaDon, Integer idDiaChi);
}