package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.ThuongHieuRequest;
import com.example.AerionSports_BE.dto.response.ThuongHieuResponse;
import com.example.AerionSports_BE.entity.ThuongHieu;
import com.example.AerionSports_BE.repository.ThuongHieuRepository;
import com.example.AerionSports_BE.service.impl.IThuongHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ThuongHieuService implements IThuongHieuService {

    @Autowired
    private ThuongHieuRepository repo;

    private ThuongHieuResponse toRes(ThuongHieu e) {
        return new ThuongHieuResponse(e.getId(), e.getMaThuongHieu(), e.getTenThuongHieu(), e.getTrangThai());
    }

    @Override
    public List<ThuongHieuResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<ThuongHieuResponse> search(int page, int size, Integer trangThai, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ThuongHieu> entityPage = repo.search(keyword, trangThai, pageable);
        return entityPage.map(this::toRes);
    }

    @Override
    public ThuongHieuResponse getById(Integer id) {
        ThuongHieu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu!"));
        return toRes(e);
    }

    @Override
    public ThuongHieuResponse save(ThuongHieuRequest r) {
        if (repo.existsByMaThuongHieu(r.getMaThuongHieu())) throw new RuntimeException("Mã thương hiệu đã tồn tại!");
        ThuongHieu e = new ThuongHieu();
        e.setMaThuongHieu(r.getMaThuongHieu());
        e.setTenThuongHieu(r.getTenThuongHieu());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public ThuongHieuResponse update(Integer id, ThuongHieuRequest r) {
        ThuongHieu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu!"));
        e.setTenThuongHieu(r.getTenThuongHieu());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        ThuongHieu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        ThuongHieu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}