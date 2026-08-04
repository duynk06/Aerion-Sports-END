package com.example.AerionSports_BE.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThuongHieuFilter {
    private String keyword; // Tìm theo cả mã hoặc tên
    private Integer trangThai; // Lọc theo trạng thái (1: Hoạt động, 0: Ngừng)
    private int page = 0; // Trang hiện tại (mặc định trang 0)
    private int size = 10; // Số bản ghi trên 1 trang (mặc định 10)
}
