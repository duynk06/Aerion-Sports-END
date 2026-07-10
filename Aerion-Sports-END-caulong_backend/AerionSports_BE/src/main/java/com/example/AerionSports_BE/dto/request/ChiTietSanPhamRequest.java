package com.example.AerionSports_BE.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChiTietSanPhamRequest {
    // Để trống hoặc không cần bắt buộc khi thêm dạng Cascade lồng từ sản phẩm cha
    private Integer idSanPham;

    // 🌟 CHỈ GIỮ LẠI MÀU SẮC VÀ TRỌNG LƯỢNG LÀM BIẾN THỂ PHẲNG
    private Integer idMauSac;
    private Integer idTrongLuong;

    @NotBlank(message = "Mã chi tiết sản phẩm không được trống")
    private String maCtsp;

    @NotNull(message = "Giá nhập không được trống")
    @Min(value = 0, message = "Giá nhập phải lớn hơn hoặc bằng 0")
    private BigDecimal giaNhap;

    @NotNull(message = "Giá bán không được trống")
    @Min(value = 0, message = "Giá bán phải lớn hơn hoặc bằng 0")
    private BigDecimal giaBan;

    @NotNull(message = "Số lượng không được trống")
    @Min(value = 0, message = "Số lượng không được âm")
    private Integer soLuong;

    @NotNull(message = "Trạng thái không được trống")
    private Integer trangThai;

    @NotBlank(message = "Hình ảnh không được trống") // Sửa thành @NotBlank cho chuỗi String
    private String hinhAnh;
}