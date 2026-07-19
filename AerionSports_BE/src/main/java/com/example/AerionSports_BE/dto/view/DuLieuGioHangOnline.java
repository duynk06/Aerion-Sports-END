package com.example.AerionSports_BE.dto.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DuLieuGioHangOnline {
    // Toan bo du lieu tong hop de render trang gio hang.
    private List<MucGioHangOnlineView> items;
    private int tongSoLuong;
    private BigDecimal tamTinh;
    private BigDecimal tongGiamGia;
    private BigDecimal tongCong;

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }
}
