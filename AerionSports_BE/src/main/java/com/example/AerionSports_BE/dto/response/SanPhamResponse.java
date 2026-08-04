package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamResponse {
    private Integer id;

    // Thương hiệu & Xuất xứ
    private Integer idThuongHieu;
    private String tenThuongHieu;
    private Integer idXuatXu;
    private String tenXuatXu;

    // 🌟 ĐÃ CHUYỂN DỊCH: Trả về đầy đủ ID và Tên của 6 thuộc tính nền ở cấp Cha
    private Integer idDoCung;
    private String tenDoCung;

    private Integer idDiemCanBang;
    private String tenDiemCanBang;

    private Integer idChatLieuThanVot;
    private String tenChatLieuThanVot;

    private Integer idChatLieuKhungVot;
    private String tenChatLieuKhungVot;

    private Integer idDanhMuc;
    private String tenDanhMuc;

    private Integer idChuViCanVot;
    private String tenChuViCanVot;

    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private Integer trangThai;
    private Instant ngayTao;
    private Instant ngaySua;

    // Danh sách các biến thể con đi kèm
    private Set<ChiTietSanPhamResponse> chiTietSanPhams;
}