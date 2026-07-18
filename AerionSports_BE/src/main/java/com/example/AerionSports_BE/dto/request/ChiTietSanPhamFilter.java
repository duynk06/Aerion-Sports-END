package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ChiTietSanPhamFilter {
    private String keyword;
    private Integer idSanPham;
    private Integer idDanhMuc;
    private Integer idMauSac;
    private Integer idTrongLuong;
    private Integer idChuViCanVot;
    private Integer idDoCung;
    private Integer idDiemCanBang;
    private Integer trangThai;
    private BigDecimal giaTu;
    private BigDecimal giaDen;
    private int page = 0;
    private int size = 10;
}