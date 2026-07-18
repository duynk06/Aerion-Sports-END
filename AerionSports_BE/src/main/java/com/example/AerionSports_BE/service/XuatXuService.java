package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.XuatXuRequest;
import com.example.AerionSports_BE.dto.response.XuatXuResponse;
import com.example.AerionSports_BE.entity.XuatXu;
import com.example.AerionSports_BE.repository.XuatXuRepository;
import com.example.AerionSports_BE.service.impl.IXuatXuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class XuatXuService implements IXuatXuService {

    @Autowired
    private XuatXuRepository repo;

    private XuatXuResponse toRes(XuatXu e) {
        return new XuatXuResponse(e.getId(), e.getMaXuatXu(), e.getTenXuatXu(), e.getTrangThai());
    }

    @Override
    public List<XuatXuResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<XuatXuResponse> search(int page, int size, Integer trangThai, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<XuatXu> entityPage = repo.search(keyword, trangThai, pageable);
        return entityPage.map(this::toRes);
    }

    @Override
    public XuatXuResponse save(XuatXuRequest r) {
        if (repo.existsByMaXuatXu(r.getMaXuatXu())) throw new RuntimeException("Mã gốc xuất xứ đã tồn tại!");
        XuatXu e = new XuatXu();
        e.setMaXuatXu(r.getMaXuatXu());
        e.setTenXuatXu(r.getTenXuatXu());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public XuatXuResponse update(Integer id, XuatXuRequest r) {
        XuatXu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu xuất xứ!"));
        e.setTenXuatXu(r.getTenXuatXu());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        XuatXu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        XuatXu e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}