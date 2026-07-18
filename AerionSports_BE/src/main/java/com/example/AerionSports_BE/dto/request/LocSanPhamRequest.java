package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class LocSanPhamRequest {

    private String keyword;

    private Integer idMauSac;

    private Integer idTrongLuong;

    private BigDecimal giaMin;

    private BigDecimal giaMax;

    private Integer trangThai;

    private Integer page=0;

    private Integer size=10;
}