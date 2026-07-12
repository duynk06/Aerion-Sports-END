package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.PhieuGiamGia;

import java.util.List;

public interface PhieuGiamGiaService {

    List<PhieuGiamGia> getAll();

    PhieuGiamGia getById(Integer id);

    PhieuGiamGia add(PhieuGiamGia phieuGiamGia);

    PhieuGiamGia update(Integer id, PhieuGiamGia phieuGiamGia);

    void delete(Integer id);
    PhieuGiamGia addVoucherVoiKhachHang(PhieuGiamGia pgg, List<Integer> khachHangIds);
    PhieuGiamGia updateVoucherVoiKhachHang(Integer id, PhieuGiamGia pggInput, List<Integer> khachHangIds);
}