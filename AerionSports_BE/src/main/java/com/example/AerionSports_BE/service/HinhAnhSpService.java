package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.HinhAnhSpFilter;
import com.example.AerionSports_BE.dto.request.HinhAnhSpRequest;
import com.example.AerionSports_BE.dto.response.HinhAnhSpResponse;
import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.HinhAnhSp;
import com.example.AerionSports_BE.repository.HinhAnhSpRepository;
import com.example.AerionSports_BE.service.impl.IHinhAnhSpSerivce;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HinhAnhSpService implements IHinhAnhSpSerivce {

    @Autowired
    private HinhAnhSpRepository repo;

    private HinhAnhSpResponse toRes(HinhAnhSp e) {
        return new HinhAnhSpResponse(e.getId(), e.getIdSanPhamChiTiet().getId(), e.getLaAnhChinh(), e.getDuongDanAnh(), e.getNgayTao(), e.getTrangThai());
    }

    @Override
    public List<HinhAnhSpResponse> getImages(HinhAnhSpFilter f) {
        return repo.findImages(f.getIdSanPhamChiTiet(), f.getTrangThai()).stream().map(this::toRes).collect(Collectors.toList());
    }

    @Override
    public HinhAnhSpResponse save(HinhAnhSpRequest r) {
        HinhAnhSp e = new HinhAnhSp();

        // phải tạo ctsp trước
        ChiTietSanPham ct = new ChiTietSanPham();
        ct.setId(r.getIdSanPhamChiTiet());
        e.setIdSanPhamChiTiet(ct);

        e.setLaAnhChinh(r.getLaAnhChinh());
        e.setDuongDanAnh(r.getDuongDanAnh());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public void delete(Integer id) {
        repo.deleteById(id); // xóa cứng
    }
}
