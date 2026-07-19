package com.example.AerionSports_BE.dto.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DuLieuTrangChuBanHangOnline {
    // Danh sach san pham noi bat de render khu "san pham noi bat" o trang chu.
    private List<SanPhamBanHangOnlineView> featuredProducts;

    // San pham duoc day len khu hero neu muon hien 1 item noi bat nhat.
    private SanPhamBanHangOnlineView heroProduct;

    // Tong so thuong hieu dang co san pham xuat hien tren shop online.
    private int brandCount;

    // Tong so san pham dang duoc dua len kenh ban hang online.
    private int productCount;
}
