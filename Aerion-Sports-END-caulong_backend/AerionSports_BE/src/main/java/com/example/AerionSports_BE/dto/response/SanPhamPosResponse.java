package com.example.AerionSports_BE.dto.response;

import com.example.AerionSports_BE.entity.ChiTietSanPham;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamPosResponse {

    private Integer id;

    private String ma;

    private String ten;

    private String anh;

    private String mauSac;

    private String trongLuong;

    private Integer soLuongTon;

    private BigDecimal gia;
    public SanPhamPosResponse(ChiTietSanPham ct){

        this.id = ct.getId();

        this.ma = ct.getMaCtsp();

        this.ten = ct.getIdSanPham()!=null
                ? ct.getIdSanPham().getTenSanPham()
                : "";

        this.anh = (ct.getHinhAnhs()!=null
                && !ct.getHinhAnhs().isEmpty())
                ? ct.getHinhAnhs().get(0).getDuongDanAnh()
                : "";

        this.mauSac = ct.getIdMauSac()!=null
                ? ct.getIdMauSac().getTenMauSac()
                : "";

        this.trongLuong = ct.getIdTrongLuong()!=null
                ? ct.getIdTrongLuong().getTenTrongLuong()
                : "";

        this.soLuongTon = ct.getSoLuong();

        this.gia = ct.getGiaBan();
    }
}