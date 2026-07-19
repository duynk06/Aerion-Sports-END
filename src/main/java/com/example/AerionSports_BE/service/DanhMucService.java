package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.DanhMucRequest;
import com.example.AerionSports_BE.dto.response.DanhMucResponse;
import com.example.AerionSports_BE.entity.DanhMuc;
import com.example.AerionSports_BE.repository.DanhMucRepository;
import com.example.AerionSports_BE.service.impl.IDanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DanhMucService implements IDanhMucService {

    @Autowired
    private DanhMucRepository repo;

    private DanhMucResponse toRes(DanhMuc e) {
        return new DanhMucResponse(e.getId(), e.getMaDanhMuc(), e.getTenDanhMuc(), e.getTrangThai());
    }

    @Override
    public List<DanhMucResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<DanhMuc> search(int page, int size, Integer trangThai) {
        Pageable pageable = PageRequest.of(page, size);
        if (trangThai != null) {
            return repo.findByTrangThai(trangThai, pageable);
        }
        return repo.findAll(pageable);
    }

    @Override
    public DanhMucResponse save(DanhMucRequest r) {
        if (repo.existsByMaDanhMuc(r.getMaDanhMuc())) throw new RuntimeException("Mã danh mục này đã tồn tại!");
        DanhMuc e = new DanhMuc();
        e.setMaDanhMuc(r.getMaDanhMuc());
        e.setTenDanhMuc(r.getTenDanhMuc());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public DanhMucResponse update(Integer id, DanhMucRequest r) {
        DanhMuc e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục này!"));
        e.setTenDanhMuc(r.getTenDanhMuc());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        DanhMuc e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        DanhMuc e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}