package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.request.ThongTinDatHangOnlineRequest;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChiTietSanPhamOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangChuBanHangOnline;
import com.example.AerionSports_BE.dto.view.DuLieuTrangDanhSachSanPhamOnline;
import com.example.AerionSports_BE.dto.view.SanPhamBanHangOnlineView;
import com.example.AerionSports_BE.entity.PhieuGiamGia;

import java.math.BigDecimal;

public interface BanHangOnlineService {

    // Tra ve du lieu tong hop de render trang chu ban hang online.
    DuLieuTrangChuBanHangOnline layDuLieuTrangChu();

    /*
     * Tra ve du lieu cho trang danh sach san pham.
     * Service se tu xu ly:
     * - bo loc
     * - sap xep
     * - phan trang
     * Controller chi viec dua object nay ra giao dien.
     */
    DuLieuTrangDanhSachSanPhamOnline layDuLieuDanhSachSanPham(
            String keyword,
            String category,
            String brand,
            String color,
            String weight,
            String origin,
            String stiffness,
            String balancePoint,
            String gripSize,
            String shaftMaterial,
            String frameMaterial,
            Long minPrice,
            Long maxPrice,
            String status,
            String price,
            String sort,
            int page
    );

    // Tra ve du lieu day du de render trang chi tiet cua 1 san pham.
    DuLieuTrangChiTietSanPhamOnline layDuLieuChiTietSanPham(Integer productId, Integer variantId);

    // Lay dung bien the ma nguoi dung dang thao tac de xu ly gio hang online.
    SanPhamBanHangOnlineView layBienTheSanPham(Integer productId, Integer variantId);

    // Xu ly dat hang online: luu khach hang, dia chi, hoa don va chi tiet hoa don.
    String datHangOnline(
            ThongTinDatHangOnlineRequest thongTinDatHang,
            com.example.AerionSports_BE.dto.view.DuLieuGioHangOnline duLieuGioHang,
            PhieuGiamGia phieuDangAp,
            BigDecimal phiVanChuyen,
            BigDecimal tienGiamVoucher,
            BigDecimal tongThanhToan
    );
}
