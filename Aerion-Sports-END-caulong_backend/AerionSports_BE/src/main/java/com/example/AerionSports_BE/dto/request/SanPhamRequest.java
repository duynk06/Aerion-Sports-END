package com.example.AerionSports_BE.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SanPhamRequest {
    @NotNull(message = "Vui lòng chọn Thương hiệu")
    private Integer idThuongHieu;

    @NotNull(message = "Vui lòng chọn Xuất xứ")
    private Integer idXuatXu;

    // 🌟 ĐÃ THÊM: 6 thuộc tính nền được nhấc lên cấp Sản phẩm cha
    @NotNull(message = "Vui lòng chọn Chu vi cán")
    private Integer idChuViCanVot;

    @NotNull(message = "Vui lòng chọn Độ cứng")
    private Integer idDoCung;

    @NotNull(message = "Vui lòng chọn Điểm cân bằng")
    private Integer idDiemCanBang;

    @NotNull(message = "Vui lòng chọn Chất liệu thân")
    private Integer idChatLieuThanVot;

    @NotNull(message = "Vui lòng chọn Chất liệu khung")
    private Integer idChatLieuKhungVot;

    @NotNull(message = "Vui lòng chọn Danh mục")
    private Integer idDanhMuc;

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 50, message = "Mã không được quá 50 ký tự")
    private String maSanPham;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String tenSanPham;

    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String moTa;

    @Size(max = 100, message = "Thông tin bảo hành tối đa 100 ký tự")
    private String baoHanh;

    @NotNull(message = "Trạng thái không được để trống")
    private Integer trangThai;

    @Valid
    private List<ChiTietSanPhamRequest> chiTietSanPhams;
}