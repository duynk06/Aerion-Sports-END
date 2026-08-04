package com.example.AerionSports_BE.dto.view;

import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DuLieuTrangChiTietSanPhamOnline {
    // Thong tin san pham cha lay tu bang/DTO SanPham.
    private SanPhamResponse product;

    // Toan bo bien the cua san pham, dung de render anh/mau/trong luong.
    private List<SanPhamBanHangOnlineView> variants;

    // Bien the dang duoc chon o thoi diem mo trang chi tiet.
    private SanPhamBanHangOnlineView selectedVariant;

    // Mot nhom san pham lien quan de goi y mua them.
    private List<SanPhamBanHangOnlineView> relatedProducts;
}
