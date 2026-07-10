package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.LichSuGiaoCa;
import java.math.BigDecimal;
import java.util.Map;

public interface LichSuGiaoCaService {

    LichSuGiaoCa moCa(Integer idNhanVien, BigDecimal tienBanDau);

    Map<String, Object> layThongTinCaHienTai(Integer idNhanVien);

    LichSuGiaoCa ketThucCa(Integer idNhanVien, Integer idNhanVienCaSau, BigDecimal tienMatDoanhThuMoi, BigDecimal tienChuyenKhoanMoi, String ghiChu);
}