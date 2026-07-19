package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.LichLamViec;
import com.example.AerionSports_BE.repository.LichLamViecRepository;
import com.example.AerionSports_BE.service.LichLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class LichLamViecImpl implements LichLamViecService {

    @Autowired
    private LichLamViecRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> layLichTrongKhoangNgay(LocalDate tuNgay, LocalDate denNgay) {
        return repository.findLichTrongKhoangNgay(tuNgay, denNgay);
    }

    @Override
    @Transactional
    public LichLamViec xepLichMoi(LichLamViec lich) {
        Integer idNhanVien = lich.getIdNhanVien();
        Integer idCaLamViec = lich.getCaLamViec() != null ? lich.getCaLamViec().getId() : null;
        LocalDate ngayLamViec = lich.getNgayLamViec();

        if (idNhanVien != null && idCaLamViec != null && ngayLamViec != null) {
            boolean isTrung = repository.existsByIdNhanVienAndCaLamViecIdAndNgayLamViec(idNhanVien, idCaLamViec, ngayLamViec);

            if (isTrung) {
                throw new RuntimeException("Nhân viên đã được xếp ca này trong ngày rồi!");
            }
        }
        lich.setTrangThai(1);
        return repository.save(lich);
    }

    @Override
    @Transactional
    public void xoaLich(Integer id) {
        repository.deleteById(id);
    }
}