package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.DoCungRequest;
import com.example.AerionSports_BE.dto.response.DoCungResponse;
import com.example.AerionSports_BE.entity.DoCung;
import com.example.AerionSports_BE.repository.DoCungRepository;
import com.example.AerionSports_BE.service.impl.IDoCungService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DoCungService implements IDoCungService {

    @Autowired
    private DoCungRepository repo;

    private DoCungResponse toRes(DoCung e) {
        return new DoCungResponse(e.getId(), e.getMaDoCung(), e.getTenDoCung(), e.getTrangThai());
    }

    @Override
    public List<DoCungResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<DoCungResponse> search(int page, int size, Integer trangThai, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<DoCung> entityPage = repo.search(keyword, trangThai, pageable);
        return entityPage.map(this::toRes);
    }

    @Override
    public DoCungResponse save(DoCungRequest r) {
        if (repo.existsByMaDoCung(r.getMaDoCung())) {
            throw new RuntimeException("Mã chỉ số độ cứng đã tồn tại hệ thống!");
        }
        DoCung e = new DoCung();
        e.setMaDoCung(r.getMaDoCung());
        e.setTenDoCung(r.getTenDoCung());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public DoCungResponse update(Integer id, DoCungRequest r) {
        DoCung e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chỉ số độ cứng này!"));
        e.setTenDoCung(r.getTenDoCung());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        DoCung e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        DoCung e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}