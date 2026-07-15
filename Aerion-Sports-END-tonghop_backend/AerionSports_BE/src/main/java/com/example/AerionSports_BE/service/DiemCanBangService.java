package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.DiemCanBangRequest;
import com.example.AerionSports_BE.dto.response.DiemCanBangResponse;
import com.example.AerionSports_BE.entity.DiemCanBang;
import com.example.AerionSports_BE.repository.DiemCanBangRepository;
import com.example.AerionSports_BE.service.impl.IDiemCanBangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DiemCanBangService implements IDiemCanBangService {

    @Autowired
    private DiemCanBangRepository repo;

    private DiemCanBangResponse toRes(DiemCanBang e) {
        return new DiemCanBangResponse(e.getId(), e.getMaDiemCanBang(), e.getTenDiemCanBang(), e.getTrangThai());
    }

    @Override
    public List<DiemCanBangResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<DiemCanBang> search(int page, int size, Integer trangThai) {
        Pageable pageable = PageRequest.of(page, size);
        if (trangThai != null) {
            return repo.findByTrangThai(trangThai, pageable);
        }
        return repo.findAll(pageable);
    }

    @Override
    public DiemCanBangResponse save(DiemCanBangRequest r) {
        if (repo.existsByMaDiemCanBang(r.getMaDiemCanBang())) throw new RuntimeException("Mã điểm cân bằng đã tồn tại!");
        DiemCanBang e = new DiemCanBang();
        e.setMaDiemCanBang(r.getMaDiemCanBang());
        e.setTenDiemCanBang(r.getTenDiemCanBang());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public DiemCanBangResponse update(Integer id, DiemCanBangRequest r) {
        DiemCanBang e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy điểm cân bằng này!"));
        e.setTenDiemCanBang(r.getTenDiemCanBang());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        DiemCanBang e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        DiemCanBang e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}