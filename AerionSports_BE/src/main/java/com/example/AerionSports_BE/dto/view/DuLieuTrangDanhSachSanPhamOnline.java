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

    // Cac lua chon bo loc
    private List<TuyChonBoLocBanHangOnline> brandOptions;
    private List<String> categoryOptions;
    private List<String> colorOptions;
    private List<String> weightOptions;
    private List<String> originOptions;
    private List<String> stiffnessOptions;
    private List<String> balancePointOptions;
    private List<String> gripSizeOptions;
    private List<String> shaftMaterialOptions;
    private List<String> frameMaterialOptions;

    // Cac gia tri nguoi dung vua loc/chon
    private String selectedKeyword;
    private String selectedCategory;
    private String selectedBrand;
    private String selectedColor;
    private String selectedWeight;
    private String selectedOrigin;
    private String selectedStiffness;
    private String selectedBalancePoint;
    private String selectedGripSize;
    private String selectedShaftMaterial;
    private String selectedFrameMaterial;
    private Long selectedMinPrice;
    private Long selectedMaxPrice;
    private String selectedStatus;
    private String selectedPrice;
    private String selectedSort;

    // Thong tin phan trang de ve nut trang truoc / trang sau.
    private int currentPage;
    private int totalPages;
    private int totalItems;
}
