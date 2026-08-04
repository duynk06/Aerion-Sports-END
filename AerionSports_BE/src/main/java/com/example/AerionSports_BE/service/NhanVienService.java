package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.NhanVien;

import java.util.List;

public interface NhanVienService {

    List<NhanVien> findAll();

    NhanVien findById(Integer id);

    NhanVien add(NhanVien nhanVien);

    NhanVien update(Integer id, NhanVien nhanVien);

    void delete(Integer id);

    List<NhanVien> search(String tenNv);

    void changeStatus(Integer id, Integer trangThai);
}