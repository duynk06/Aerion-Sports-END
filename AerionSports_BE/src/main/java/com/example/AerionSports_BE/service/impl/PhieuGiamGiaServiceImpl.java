package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.PhieuGiamGia;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.entity.PhieuGiamGiaKhachHang;
import com.example.AerionSports_BE.repository.PhieuGiamGiaRepository;
import com.example.AerionSports_BE.repository.KhachHangRepository;
import com.example.AerionSports_BE.repository.PhieuGiamGiaKhachHangRepository;
import com.example.AerionSports_BE.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    private final PhieuGiamGiaRepository repository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private PhieuGiamGiaKhachHangRepository pggKhachHangRepository;

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
        pgg.setNgayTao(LocalDateTime.now());
        pgg.setNgayCapNhat(LocalDateTime.now());

        return repository.save(pgg);
    }

    @Override
    @Transactional
    public PhieuGiamGia addVoucherVoiKhachHang(PhieuGiamGia pgg, List<Integer> khachHangIds) {
        PhieuGiamGia savedPgg = add(pgg);

        if (khachHangIds != null && !khachHangIds.isEmpty()) {
            List<KhachHang> khachHangs = khachHangRepository.findAllById(khachHangIds);

            for (KhachHang kh : khachHangs) {
                PhieuGiamGiaKhachHang liênKet = new PhieuGiamGiaKhachHang();
                liênKet.setPhieuGiamGia(savedPgg);
                liênKet.setKhachHang(kh);
                liênKet.setDaSuDung(false);
                liênKet.setNgayNhan(LocalDateTime.now());
                liênKet.setTrangThai(1);

                pggKhachHangRepository.save(liênKet);
            }
        }

        return savedPgg;
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
        existingPgg.setNgayCapNhat(LocalDateTime.now());

        return repository.save(existingPgg);
    }

    @Override
    @Transactional
    public PhieuGiamGia updateVoucherVoiKhachHang(Integer id, PhieuGiamGia pggInput, List<Integer> khachHangIds) {
        PhieuGiamGia existingPgg = update(id, pggInput);

        return existingPgg;
    }
    @Override
    @Transactional
    public void delete(Integer id) {
        repository.deleteById(id);
    }
}