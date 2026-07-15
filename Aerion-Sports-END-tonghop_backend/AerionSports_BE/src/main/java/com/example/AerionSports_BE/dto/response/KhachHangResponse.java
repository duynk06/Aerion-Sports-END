package com.example.AerionSports_BE.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KhachHangResponse {
    private Integer id;
    private String maKhachHang;
    private String hoTen;
    private String email;
    private String sdt;
    private LocalDate ngaySinh;
    private Long tongSoDonHang;          // Trường tự tính, không cần có trong DB
    private LocalDateTime donHangGanNhat; // Trường tự tính, không cần có trong DB
}