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
/**
 * DTO cho một dòng chi tiết sản phẩm trong đợt giảm giá.
 * File này được dùng chung cho:
 * - danh sách sản phẩm cha/con ở modal chọn
 * - bảng chi tiết sản phẩm đã chọn
 * - dữ liệu trả về từ API
 */
public class ChiTietDotGiamGiaDTO {

    private Integer id;
    private Integer idDotGiamGia;
    // ID biến thể sản phẩm chi tiết, đây là khóa chính dùng khi tick checkbox chọn sản phẩm.
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
