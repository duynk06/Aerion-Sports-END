package com.example.AerionSports_BE.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class KhachHangNhanhRequest {
    private String hoTen;
    private String sdt;
    private String email;
    private Integer gioiTinh;   // 1 = Nam, 0 = Nữ
    private LocalDate ngaySinh;
}