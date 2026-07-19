package com.example.AerionSports_BE.dto.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamBanHangOnlineView {
    /*
     * DTO nay la "goi du lieu de render giao dien".
     * No khong nhat thiet phai giong 100% cau truc bang trong database.
     * Muc tieu la gom du cac thong tin ma template can dung vao 1 cho.
     *
     * Thuong moi object se dai dien cho 1 bien the san pham
     * (vi du: cung 1 vot nhung khac mau sac hoac trong luong).
     *
     * Nhom thong tin:
     * - Nhan dien: productId, variantId, productCode
     * - Hien thi: name, brand, origin, description
     * - Bien the: color, weight, stock
     * - Gia va anh: imageUrl, price, originalPrice, discountPercent
     */
    private Integer productId;
    private Integer variantId;
    private String productCode;
    private String name;
    private String brand;
    private String origin;
    private String color;
    private String weight;
    private String imageUrl;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private BigDecimal discountPercent;
    private Integer stock;

    // Cho phep template goi product.onSale de bat/tat badge giam gia.
    public boolean isOnSale() {
        return originalPrice != null
                && price != null
                && originalPrice.compareTo(price) > 0;
    }
}
