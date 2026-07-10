package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SanPhamFilter {
    private String keyword;
    private Integer idThuongHieu;
    private Integer idXuatXu;

    // 🌟 BỔ SUNG: Phục vụ bộ lọc tìm kiếm nâng cao ngay tại cây Sản phẩm cha
    private Integer idDanhMuc;
    private Integer idChuViCanVot;
    private Integer idDoCung;
    private Integer idDiemCanBang;

    private Integer trangThai;
    private int page = 0;
    private int size = 10;
}