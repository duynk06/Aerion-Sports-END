package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.VoucherRequestDTO;
import com.example.AerionSports_BE.entity.PhieuGiamGia;

import java.util.List;

public interface PhieuGiamGiaService {

    List<PhieuGiamGia> getAll();

    PhieuGiamGia getById(Integer id);

    PhieuGiamGia add(VoucherRequestDTO dto);

    PhieuGiamGia update(Integer id, PhieuGiamGia phieuGiamGia);

    void delete(Integer id);
}
