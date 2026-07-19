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
public class DuLieuTrangDanhSachSanPhamOnline {
    // Danh sach san pham cuoi cung se hien thi tren luoi san pham.
    private List<SanPhamBanHangOnlineView> products;

    // Cac lua chon thuong hieu de render cot bo loc ben trai.
    private List<TuyChonBoLocBanHangOnline> brandOptions;

    // Cac muc trong luong dang ton tai trong du lieu that.
    private List<String> weightOptions;

    /*
     * Cac gia tri nguoi dung vua loc/chon.
     * Can giu lai de khi server tra HTML ve:
     * - o tim kiem van con tu khoa cu
     * - checkbox/select van hien dung trang thai dang chon
     * - nguoi dung khong bi mat bo loc sau moi lan submit form
     */
    private String selectedKeyword;
    private String selectedBrand;
    private String selectedWeight;
    private String selectedPrice;
    private String selectedSort;

    // Thong tin phan trang de ve nut trang truoc / trang sau.
    private int currentPage;
    private int totalPages;
    private int totalItems;
}
