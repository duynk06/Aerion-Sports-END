package com.example.AerionSports_BE.dto.response;



public interface KhachHangPosResponse {
    Integer getId();
    String getHoTen(); // Lưu ý: Trong câu SQL phải dùng AS hoTen thay vì AS ten
    String getSdt();
    String getEmail();
    String getDiaChi();
    String getTinhThanh();
}