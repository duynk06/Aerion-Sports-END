package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.MauSacRequest;
import com.example.AerionSports_BE.dto.response.MauSacResponse;
import com.example.AerionSports_BE.entity.MauSac;
import com.example.AerionSports_BE.repository.MauSacRepository;
import com.example.AerionSports_BE.service.impl.IMauSacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MauSacService implements IMauSacService {

    @Autowired
    private MauSacRepository repo;

    private MauSacResponse toRes(MauSac e) {
        return new MauSacResponse(e.getId(), e.getMaMauSac(), e.getTenMauSac(), e.getTrangThai());
    }

    @Override
    public List<MauSacResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<MauSacResponse> search(int page, int size, Integer trangThai, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MauSac> entityPage = repo.search(keyword, trangThai, pageable);
        return entityPage.map(this::toRes);
    }

    @Override
    public MauSacResponse save(MauSacRequest r) {
        if (repo.existsByMaMauSac(r.getMaMauSac())) throw new RuntimeException("Mã màu sắc đã tồn tại!");
        MauSac e = new MauSac();
        e.setMaMauSac(r.getMaMauSac());
        e.setTenMauSac(r.getTenMauSac());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public MauSacResponse update(Integer id, MauSacRequest r) {
        MauSac e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc này!"));
        e.setTenMauSac(r.getTenMauSac());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        MauSac e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        MauSac e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}