package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.repository.PhieuGiamGiaRepository;
import com.example.AerionSports_BE.service.PhieuGiamGiaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    private final PhieuGiamGiaRepository repository;

    public PhieuGiamGiaServiceImpl(PhieuGiamGiaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PhieuGiamGia> getAll() {
        return repository.findAll();
    }

    @Override
    public PhieuGiamGia getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public PhieuGiamGia add(PhieuGiamGia pgg) {
        if (pgg.getSoLuongDaSuDung() == null) {
            pgg.setSoLuongDaSuDung(0);
        }
        if (pgg.getTrangThai() == null) {
            pgg.setTrangThai(1);
        }
        pgg.setNgayTao(java.time.LocalDateTime.now());
        pgg.setNgayCapNhat(java.time.LocalDateTime.now());

        return repository.save(pgg);
    }

    @Override
    @Transactional
    public PhieuGiamGia update(Integer id, PhieuGiamGia pggInput) {
        PhieuGiamGia existingPgg = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá với ID: " + id));

        existingPgg.setMaPhieuGiamGia(pggInput.getMaPhieuGiamGia());
        existingPgg.setTenPhieuGiamGia(pggInput.getTenPhieuGiamGia());
        existingPgg.setLoaiPhieuGiamGia(pggInput.getLoaiPhieuGiamGia());
        existingPgg.setGiaTriGiam(pggInput.getGiaTriGiam());
        existingPgg.setGiaTriDonToiThieu(pggInput.getGiaTriDonToiThieu());
        existingPgg.setGiaTriGiamToiDa(pggInput.getGiaTriGiamToiDa());
        existingPgg.setSoLuong(pggInput.getSoLuong());
        existingPgg.setNgayBatDau(pggInput.getNgayBatDau());
        existingPgg.setNgayKetThuc(pggInput.getNgayKetThuc());
        existingPgg.setMoTa(pggInput.getMoTa());
        existingPgg.setTrangThai(pggInput.getTrangThai());
        existingPgg.setNgayCapNhat(java.time.LocalDateTime.now());

        return repository.save(existingPgg);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}