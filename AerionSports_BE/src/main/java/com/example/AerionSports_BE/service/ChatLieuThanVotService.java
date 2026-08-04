package com.example.AerionSports_BE.service;


import com.example.AerionSports_BE.dto.request.ChatLieuThanVotRequest;
import com.example.AerionSports_BE.dto.response.ChatLieuThanVotResponse;
import com.example.AerionSports_BE.entity.ChatLieuThanVot;
import com.example.AerionSports_BE.repository.ChatLieuThanVotRepository;
import com.example.AerionSports_BE.service.impl.IChatLieuThanVotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ChatLieuThanVotService implements IChatLieuThanVotService {

    @Autowired
    private ChatLieuThanVotRepository repo;

    private ChatLieuThanVotResponse toRes(ChatLieuThanVot e) {
        return new ChatLieuThanVotResponse(e.getId(), e.getMaChatLieuThanVot(), e.getTenChatLieuThanVot(), e.getTrangThai());
    }

    @Override
    public List<ChatLieuThanVotResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    @Override
    public Page<ChatLieuThanVot> search(int page, int size, Integer trangThai) {
        Pageable pageable = PageRequest.of(page, size);
        if (trangThai != null) {
            return repo.findByTrangThai(trangThai, pageable);
        }
        return repo.findAll(pageable);
    }

    @Override
    public ChatLieuThanVotResponse save(ChatLieuThanVotRequest r) {
        if (repo.existsByMaChatLieuThanVot(r.getMaChatLieuThanVot())) throw new RuntimeException("Mã chất liệu thân đã tồn tại!");
        ChatLieuThanVot e = new ChatLieuThanVot();
        e.setMaChatLieuThanVot(r.getMaChatLieuThanVot());
        e.setTenChatLieuThanVot(r.getTenChatLieuThanVot());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    @Override
    public ChatLieuThanVotResponse update(Integer id, ChatLieuThanVotRequest r) {
        ChatLieuThanVot e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu thân này!"));
        e.setTenChatLieuThanVot(r.getTenChatLieuThanVot());
        e.setTrangThai(r.getTrangThai());
        return toRes(repo.save(e));
    }

    public void updateTrangThai(Integer id, Integer trangThai) {
        ChatLieuThanVot e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public void delete(Integer id) {
        ChatLieuThanVot e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu để xóa cứng!"));
        repo.delete(e); // 🟢 ĐÃ ĐỔI THÀNH XÓA CỨNG
    }
}