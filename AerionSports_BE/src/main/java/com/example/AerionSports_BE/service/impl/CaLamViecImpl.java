package com.example.AerionSports_BE.service.impl;

import com.example.AerionSports_BE.entity.CaLamViec;
import com.example.AerionSports_BE.repository.CaLamViecRepository;
import com.example.AerionSports_BE.service.CaLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CaLamViecImpl implements CaLamViecService {

    @Autowired
    private CaLamViecRepository caLamViecRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CaLamViec> layTatCaCa() {
        return caLamViecRepository.findAll();
    }

    @Override
    @Transactional
    public CaLamViec luuCaMoi(CaLamViec caLamViec) {
        return caLamViecRepository.save(caLamViec);
    }

    @Override
    @Transactional
    public void xoaCa(Integer id) {
        caLamViecRepository.deleteById(id);
    }
}