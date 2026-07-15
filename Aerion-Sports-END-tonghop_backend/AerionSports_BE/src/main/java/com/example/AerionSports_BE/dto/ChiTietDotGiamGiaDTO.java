package com.example.AerionSports_BE.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietDotGiamGiaDTO {



    private Integer id;
    private Integer idDotGiamGia;
    @JsonProperty("idChiTietSanPham")
    private Integer idChiTietSanPham;
    private String maCtsp;
    private String maSanPham;
    private String tenSanPham;
    private String tenThuongHieu;
    private String anhDaiDien;
    private String tenMauSac;
    private String tenTrongLuong;
    private String tenChuViCanVot;
    private String chuViCanVot;
    private String tenDoCung;
    private String tenDiemCanBang;
    private String tenChatLieuThanVot;
    private String tenChatLieuKhungVot;
    private String tenDanhMuc;
    private String tenXuatXu;
    private String xuatXuChiTiet;
    private BigDecimal giaBan;
    private BigDecimal giaNhap;
    private Integer soLuong;
    private Integer soLuongTon;
    private Integer trangThai;
    private BigDecimal giaTriGiamRieng;
    private String ghiChu;
}
