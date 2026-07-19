package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class HinhAnhSpResponse {
    private Integer id;
    private Integer idSanPhamChiTiet;
    private Boolean laAnhChinh;
    private String duongDanAnh;
    private Instant ngayTao;
    private Integer trangThai;
}
