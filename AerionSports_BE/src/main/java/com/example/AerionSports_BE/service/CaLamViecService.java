package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.CaLamViec;
import java.util.List;

public interface CaLamViecService {

    List<CaLamViec> layTatCaCa();

    CaLamViec luuCaMoi(CaLamViec caLamViec);

    void xoaCa(Integer id);
}