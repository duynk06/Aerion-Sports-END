package com.example.AerionSports_BE.service.impl;


import com.example.AerionSports_BE.dto.request.ChuViCanVotFilter;
import com.example.AerionSports_BE.dto.request.ChuViCanVotRequest;
import com.example.AerionSports_BE.entity.ChuViCanVot;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IChuViCanVotService {
    List<ChuViCanVot> getAllActive();
    Page<ChuViCanVot> search(ChuViCanVotFilter f);
    ChuViCanVot getById(Integer id);
    ChuViCanVot save(ChuViCanVotRequest r);
    ChuViCanVot update(Integer id, ChuViCanVotRequest r);
    void delete(Integer id);
}
