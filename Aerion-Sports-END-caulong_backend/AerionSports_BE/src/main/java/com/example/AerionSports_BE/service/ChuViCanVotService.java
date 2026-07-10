package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.ChuViCanVotFilter;
import com.example.AerionSports_BE.dto.request.ChuViCanVotRequest;
import com.example.AerionSports_BE.entity.ChuViCanVot;
import com.example.AerionSports_BE.repository.ChuViCanVotRepository;
import com.example.AerionSports_BE.service.impl.IChuViCanVotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChuViCanVotService implements IChuViCanVotService {

    @Autowired
    private ChuViCanVotRepository repo;

    @Override
    public List<ChuViCanVot> getAllActive() {
        return repo.findByTrangThai(1);
    }

    @Override
    public Page<ChuViCanVot> search(ChuViCanVotFilter f) {
        return repo.search(f.getKeyword(), f.getTrangThai(), PageRequest.of(f.getPage(), f.getSize()));
    }

    @Override
    public ChuViCanVot getById(Integer id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chu vi cán vợt!"));
    }

    @Override
    public ChuViCanVot save(ChuViCanVotRequest r) {
        if (repo.existsByMaChuViCanVot(r.getMaChuViCanVot())) {
            throw new RuntimeException("Mã chu vi cán vợt này đã tồn tại!");
        }
        ChuViCanVot e = new ChuViCanVot();
        e.setMaChuViCanVot(r.getMaChuViCanVot());
        e.setTenChuViCanVot(r.getTenChuViCanVot());
        e.setTrangThai(r.getTrangThai());
        return repo.save(e);
    }

    @Override
    public ChuViCanVot update(Integer id, ChuViCanVotRequest r) {
        ChuViCanVot e = getById(id);
        if (repo.existsByMaChuViCanVotAndIdNot(r.getMaChuViCanVot(), id)) {
            throw new RuntimeException("Mã chu vi cán vợt này đã tồn tại ở một bản ghi khác!");
        }
        e.setMaChuViCanVot(r.getMaChuViCanVot());
        e.setTenChuViCanVot(r.getTenChuViCanVot());
        e.setTrangThai(r.getTrangThai());
        return repo.save(e);
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        ChuViCanVot e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        ChuViCanVot e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}