package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.request.ChiTietSanPhamFilter;
import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.entity.*;
import com.example.AerionSports_BE.repository.ChiTietSanPhamRepository;
import com.example.AerionSports_BE.repository.ChiTietDotGiamGiaRepository;
import com.example.AerionSports_BE.service.impl.IChiTietSanPhamService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class ChiTietSanPhamService implements IChiTietSanPhamService {

    @Autowired
    private ChiTietSanPhamRepository repo;

    @Autowired
    private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    private ChiTietSanPhamResponse toRes(ChiTietSanPham e) {
        if (e == null) return null;

        String duongDanAnhThucTe = null;
        if (e.getHinhAnhs() != null && !e.getHinhAnhs().isEmpty()) {
            duongDanAnhThucTe = e.getHinhAnhs().stream()
                    .filter(anh -> anh.getLaAnhChinh() != null && anh.getLaAnhChinh())
                    .map(HinhAnhSp::getDuongDanAnh)
                    .findFirst()
                    .orElse(e.getHinhAnhs().get(0).getDuongDanAnh());
        }

        Integer idSanPhamCha = null;
        String maSanPhamCha = null;
        if (e.getIdSanPham() != null) {
            idSanPhamCha = e.getIdSanPham().getId();
            maSanPhamCha = e.getIdSanPham().getMaSanPham();
        }

        ChiTietSanPhamResponse dto = new ChiTietSanPhamResponse();
        dto.setId(e.getId());
        dto.setIdSanPham(idSanPhamCha);
        dto.setMaSanPham(maSanPhamCha);
        dto.setMaCtsp(e.getMaCtsp());

        // Chỉ gán các trường đặc tính riêng thực tế của biến thể con
        dto.setIdMauSac(e.getIdMauSac() != null ? e.getIdMauSac().getId() : null);
        dto.setTenMauSac(e.getIdMauSac() != null ? e.getIdMauSac().getTenMauSac() : null);
        dto.setIdTrongLuong(e.getIdTrongLuong() != null ? e.getIdTrongLuong().getId() : null);
        dto.setTenTrongLuong(e.getIdTrongLuong() != null ? e.getIdTrongLuong().getTenTrongLuong() : null);

        dto.setGiaNhap(e.getGiaNhap());
        dto.setGiaBan(e.getGiaBan());
        dto.setSoLuong(e.getSoLuong());
        dto.setTrangThai(e.getTrangThai());
        dto.setNgayTao(e.getNgayTao());
        dto.setNgayCapNhat(e.getNgayCapNhat());
        dto.setHinhAnh(duongDanAnhThucTe);

        // Logic tính toán đợt giảm giá (Giữ nguyên)
        java.math.BigDecimal phanTramGiam = java.math.BigDecimal.ZERO;
        java.math.BigDecimal giaDaGiam = e.getGiaBan();
        java.time.LocalDateTime gioHienTaiVN = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        List<ChiTietDotGiamGia> discountLinks = chiTietDotGiamGiaRepository.findBestActiveByChiTietSanPhamId(e.getId(), gioHienTaiVN);

        if (discountLinks != null && !discountLinks.isEmpty()) {
            DotGiamGia dgg = discountLinks.get(0).getDotGiamGia();
            if (dgg != null && dgg.getGiaTriGiam() != null) {
                phanTramGiam = dgg.getGiaTriGiam();
                java.math.BigDecimal heSo = java.math.BigDecimal.valueOf(100).subtract(phanTramGiam);
                giaDaGiam = e.getGiaBan().multiply(heSo).divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
        }
        dto.setGiaDaGiam(giaDaGiam);
        dto.setPhanTramGiam(phanTramGiam);

        return dto;
    }

    @Override
    public List<ChiTietSanPhamResponse> getAll() {
        return repo.findByTrangThai(1).stream().map(this::toRes).toList();
    }

    @Override
    public Page<ChiTietSanPhamResponse> search(ChiTietSanPhamFilter f) {
        return repo.search(f.getKeyword(), f.getIdSanPham(), f.getIdDanhMuc(), f.getIdMauSac(),
                f.getIdTrongLuong(), f.getIdChuViCanVot(), f.getIdDoCung(), f.getIdDiemCanBang(), f.getTrangThai(),
                f.getGiaTu(), f.getGiaDen(), PageRequest.of(f.getPage(), f.getSize())).map(this::toRes);
    }

    @Override
    public ChiTietSanPhamResponse save(ChiTietSanPhamRequest r) {
        if (repo.existsByMaCtsp(r.getMaCtsp())) throw new RuntimeException("Mã CTSP này đã tồn tại!");
        ChiTietSanPham e = new ChiTietSanPham();
        mapFields(e, r);
        e.setNgayTao(Instant.now());
        e.setNgayCapNhat(Instant.now());
        return toRes(repo.save(e));
    }

    @Override
    public ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest r) {
        ChiTietSanPham e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));
        mapFields(e, r);
        e.setNgayCapNhat(Instant.now());
        return toRes(repo.save(e));
    }

    private void mapFields(ChiTietSanPham e, ChiTietSanPhamRequest r) {
        SanPham sp = new SanPham(); sp.setId(r.getIdSanPham()); e.setIdSanPham(sp);
        if (r.getIdMauSac() != null) { MauSac m = new MauSac(); m.setId(r.getIdMauSac()); e.setIdMauSac(m); }
        if (r.getIdTrongLuong() != null) { TrongLuong t = new TrongLuong(); t.setId(r.getIdTrongLuong()); e.setIdTrongLuong(t); }
        e.setMaCtsp(r.getMaCtsp());
        e.setGiaNhap(r.getGiaNhap());
        e.setGiaBan(r.getGiaBan());
        e.setSoLuong(r.getSoLuong());
        e.setTrangThai(r.getTrangThai());
    }

    @Override
    public void delete(Integer id) {
        ChiTietSanPham e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy!"));
        e.setTrangThai(0);
        repo.save(e);
    }

    @Override
    public void updateTrangThai(Integer id, Integer trangThai) {
        ChiTietSanPham e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));
        e.setTrangThai(trangThai);
        repo.save(e);
    }

    @Override
    public List<SanPhamResponse> getAllProductsWithVariantsForCheck() {
        List<ChiTietSanPham> listFlat = repo.searchActiveProducts(null);
        java.util.Map<SanPham, List<ChiTietSanPham>> groupMap = listFlat.stream()
                .filter(ctsp -> ctsp.getIdSanPham() != null)
                .collect(java.util.stream.Collectors.groupingBy(ChiTietSanPham::getIdSanPham));

        return groupMap.entrySet().stream().map(entry -> {
            SanPham spChaEntity = entry.getKey();
            List<ChiTietSanPham> ctspList = entry.getValue();
            List<ChiTietSanPhamResponse> listVariantsDto = ctspList.stream().map(this::toRes).toList();

            SanPhamResponse parentDto = new SanPhamResponse();
            parentDto.setId(spChaEntity.getId());
            parentDto.setMaSanPham(spChaEntity.getMaSanPham());
            parentDto.setTenSanPham(spChaEntity.getTenSanPham());
            parentDto.setMoTa(spChaEntity.getMoTa());
            parentDto.setBaoHanh(spChaEntity.getBaoHanh());
            parentDto.setTrangThai(spChaEntity.getTrangThai());
            parentDto.setNgayTao(spChaEntity.getNgayTao());
            parentDto.setNgaySua(spChaEntity.getNgaySua());

            if (spChaEntity.getIdThuongHieu() != null) {
                parentDto.setIdThuongHieu(spChaEntity.getIdThuongHieu().getId());
                parentDto.setTenThuongHieu(spChaEntity.getIdThuongHieu().getTenThuongHieu());
            }
            if (spChaEntity.getIdXuatXu() != null) {
                parentDto.setIdXuatXu(spChaEntity.getIdXuatXu().getId());
                parentDto.setTenXuatXu(spChaEntity.getIdXuatXu().getTenXuatXu());
            }

            // 🌟 ĐỒNG BỘ: Đổ dữ liệu 6 thông số nền ra API phẳng phục vụ check trùng ngoài UI Vue 3
            parentDto.setIdDoCung(spChaEntity.getIdDoCung() != null ? spChaEntity.getIdDoCung().getId() : null);
            parentDto.setTenDoCung(spChaEntity.getIdDoCung() != null ? spChaEntity.getIdDoCung().getTenDoCung() : null);
            parentDto.setIdDiemCanBang(spChaEntity.getIdDiemCanBang() != null ? spChaEntity.getIdDiemCanBang().getId() : null);
            parentDto.setTenDiemCanBang(spChaEntity.getIdDiemCanBang() != null ? spChaEntity.getIdDiemCanBang().getTenDiemCanBang() : null);
            parentDto.setIdChuViCanVot(spChaEntity.getIdChuViCanVot() != null ? spChaEntity.getIdChuViCanVot().getId() : null);
            parentDto.setTenChuViCanVot(spChaEntity.getIdChuViCanVot() != null ? spChaEntity.getIdChuViCanVot().getTenChuViCanVot() : null);
            parentDto.setIdChatLieuThanVot(spChaEntity.getIdChatLieuThanVot() != null ? spChaEntity.getIdChatLieuThanVot().getId() : null);
            parentDto.setTenChatLieuThanVot(spChaEntity.getIdChatLieuThanVot() != null ? spChaEntity.getIdChatLieuThanVot().getTenChatLieuThanVot() : null);
            parentDto.setIdChatLieuKhungVot(spChaEntity.getIdChatLieuKhungVot() != null ? spChaEntity.getIdChatLieuKhungVot().getId() : null);
            parentDto.setTenChatLieuKhungVot(spChaEntity.getIdChatLieuKhungVot() != null ? spChaEntity.getIdChatLieuKhungVot().getTenChatLieuKhungVot() : null);
            parentDto.setIdDanhMuc(spChaEntity.getIdDanhMuc() != null ? spChaEntity.getIdDanhMuc().getId() : null);
            parentDto.setTenDanhMuc(spChaEntity.getIdDanhMuc() != null ? spChaEntity.getIdDanhMuc().getTenDanhMuc() : null);

            parentDto.setChiTietSanPhams(new java.util.HashSet<>(listVariantsDto));
            return parentDto;
        }).toList();
    }
}