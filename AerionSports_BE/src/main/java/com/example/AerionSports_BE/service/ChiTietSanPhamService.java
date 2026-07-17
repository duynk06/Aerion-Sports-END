package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.request.ChiTietSanPhamFilter;
import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.entity.*;
import com.example.AerionSports_BE.repository.ChiTietSanPhamRepository;
import com.example.AerionSports_BE.repository.ChiTietDotGiamGiaRepository;
import com.example.AerionSports_BE.repository.MauSacRepository;
import com.example.AerionSports_BE.repository.TrongLuongRepository;
import com.example.AerionSports_BE.service.impl.IChiTietSanPhamService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ChiTietSanPhamService implements IChiTietSanPhamService {

    @Autowired
    private ChiTietSanPhamRepository repo;

    @Autowired private MauSacRepository mauSacRepo;
    @Autowired private TrongLuongRepository trongLuongRepo;
    @Autowired
    private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    // 🌟 ĐÃ THÊM: Đọc cấu hình thư mục lưu ảnh từ file application.properties
    @Value("${app.upload.dir}")
    private String uploadDir;

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
        String tenSanPhamCha = null;
        if (e.getIdSanPham() != null) {
            idSanPhamCha = e.getIdSanPham().getId();
            maSanPhamCha = e.getIdSanPham().getMaSanPham();
            tenSanPhamCha = e.getIdSanPham().getTenSanPham();
        }

        ChiTietSanPhamResponse dto = new ChiTietSanPhamResponse();
        dto.setId(e.getId());
        dto.setIdSanPham(idSanPhamCha);
        dto.setMaSanPham(maSanPhamCha);
        dto.setTenSanPham(tenSanPhamCha);
        dto.setMaCtsp(e.getMaCtsp());

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

        java.math.BigDecimal phanTramGiam = java.math.BigDecimal.ZERO;
        java.math.BigDecimal giaDaGiam = e.getGiaBan();
        java.time.LocalDateTime gioHienTaiVN = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        List<ChiTietDotGiamGia> discountLinks = chiTietDotGiamGiaRepository.findBestActiveByChiTietSanPhamId(e.getId(), gioHienTaiVN);

        if (discountLinks != null && !discountLinks.isEmpty()) {
            DotGiamGia dgg = discountLinks.get(0).getDotGiamGia();
            if (dgg != null && dgg.getGiaTriGiam() != null) {
                phanTramGiam = dgg.getGiaTriGiam();
                java.math.BigDecimal heSo = java.math.BigDecimal.valueOf(100).subtract(phanTramGiam);
                if (e.getGiaBan() != null) {
                    giaDaGiam = e.getGiaBan().multiply(heSo).divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                }
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
    public ChiTietSanPhamResponse findById(Integer id) {
        ChiTietSanPham e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm có ID: " + id));
        return toRes(e);
    }

    @Override
    public Page<ChiTietSanPhamResponse> search(ChiTietSanPhamFilter f) {
        return repo.search(f.getKeyword(), f.getIdSanPham(), f.getIdDanhMuc(), f.getIdMauSac(),
                f.getIdTrongLuong(), f.getIdChuViCanVot(), f.getIdDoCung(), f.getIdDiemCanBang(), f.getTrangThai(),
                f.getGiaTu(), f.getGiaDen(), PageRequest.of(f.getPage(), f.getSize())).map(this::toRes);

    }

    @Override
    public ChiTietSanPhamResponse save(ChiTietSanPhamRequest r) {
        if (repo.existsByMaCtsp(r.getMaCtsp()))
            throw new RuntimeException("Mã CTSP này đã tồn tại!");

        if (r.getIdMauSac() != null && r.getIdTrongLuong() != null) {
            boolean daTonTai = repo.existsByIdSanPham_IdAndIdMauSac_IdAndIdTrongLuong_Id(
                    r.getIdSanPham(), r.getIdMauSac(), r.getIdTrongLuong());
            if (daTonTai) {
                throw new RuntimeException(
                        "Biến thể với Màu sắc và Trọng lượng này đã tồn tại cho sản phẩm này!");
            }
        }
        ChiTietSanPham e = new ChiTietSanPham();
        mapFields(e, r);
        e.setNgayTao(Instant.now());
        e.setNgayCapNhat(Instant.now());

        dongBoTrangThaiTheoTonKho(e);

        ChiTietSanPham savedEntity = repo.save(e);

        // 🌟 XỬ LÝ LƯU FILE ẢNH VẬT LÝ KHI THÊM MỚI
        handleFileUpload(savedEntity, r);

        return toRes(savedEntity);
    }

    @Override
    public ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest r) {
        ChiTietSanPham e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm!"));
        mapFields(e, r);
        e.setNgayCapNhat(Instant.now());

        dongBoTrangThaiTheoTonKho(e);

        handleFileUpload(e, r);

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

    // 🌟 ĐÃ BỔ SUNG: Hàm xử lý đọc file từ Request, lưu vào thư mục và ghi nhận bảng liên kết hinh_anh_sp
    private void handleFileUpload(ChiTietSanPham e, ChiTietSanPhamRequest r) {
        if (r.getFileAnh() != null && !r.getFileAnh().isEmpty()) {
            try {
                File folder = new File(uploadDir);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String originalFilename = r.getFileAnh().getOriginalFilename();
                String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = UUID.randomUUID().toString() + extension;

                File destFile = new File(folder.getAbsolutePath() + File.separator + newFilename);
                r.getFileAnh().transferTo(destFile);

                String duongDanWeb = "/uploads/" + newFilename;

                // Tạo đối tượng thực thể hình ảnh mới đồng bộ liên kết bảng hinh_anh_sp
                HinhAnhSp anhEntity = new HinhAnhSp();
                anhEntity.setIdSanPhamChiTiet(e);
                anhEntity.setLaAnhChinh(true);
                anhEntity.setDuongDanAnh(duongDanWeb);
                anhEntity.setTrangThai(1);

                if (e.getHinhAnhs() != null) {
                    // Nếu đã có ảnh cũ, hạ cấp làm ảnh phụ để ảnh mới tải lên làm ảnh chính
                    e.getHinhAnhs().forEach(anh -> anh.setLaAnhChinh(false));
                    e.getHinhAnhs().add(anhEntity);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Lỗi trong quá trình lưu tệp tin ảnh hệ thống: " + ex.getMessage());
            }
        }
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
        if (trangThai == 1 && (e.getSoLuong() == null || e.getSoLuong() <= 0)) {
            throw new RuntimeException("Không thể kích hoạt biến thể đang hết hàng (số lượng tồn = 0)!");
        }

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

    @Override
    public ChiTietSanPhamResponse updateFullDetailsFromModal(Integer id, ChiTietSanPhamRequest r, MultipartFile fileAnh) throws Exception {
        ChiTietSanPham e = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm có ID: " + id));

        // 🟢 Dùng findById an toàn thay vì query JPQL cũ dễ ném NoResultException
        if (r.getIdMauSac() != null) {
            e.setIdMauSac(mauSacRepo.findById(r.getIdMauSac())
                    .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại (ID: " + r.getIdMauSac() + ")")));
        }
        if (r.getIdTrongLuong() != null) {
            e.setIdTrongLuong(trongLuongRepo.findById(r.getIdTrongLuong())
                    .orElseThrow(() -> new RuntimeException("Trọng lượng không tồn tại (ID: " + r.getIdTrongLuong() + ")")));
        }

        e.setGiaBan(r.getGiaBan());
        e.setSoLuong(r.getSoLuong());
        e.setNgayCapNhat(Instant.now());

        dongBoTrangThaiTheoTonKho(e);

        if (fileAnh != null && !fileAnh.isEmpty()) {
            r.setFileAnh(fileAnh);
            handleFileUpload(e, r);
        }

        return toRes(repo.save(e));
    }
    @Transactional
    public void saveVariantsToExistingProduct(Integer idSanPhamChaCu, String bienTheJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            // Đọc và chuyển đổi chuỗi JSON gửi từ Client thành danh sách Object Request
            List<ChiTietSanPhamRequest> listRequests = mapper.readValue(bienTheJson, new TypeReference<List<ChiTietSanPhamRequest>>() {});

            for (ChiTietSanPhamRequest req : listRequests) {
                // Thiết lập ID sản phẩm cha là sản phẩm cũ được chọn gộp
                req.setIdSanPham(idSanPhamChaCu);

                // Tận dụng lại hàm save() lẻ của chính bạn để tự động map fields, ghi nhận ngày tạo và xử lý upload ảnh
                this.save(req);
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý gộp danh sách biến thể vào sản phẩm cũ: " + e.getMessage());
        }
    }
    private void dongBoTrangThaiTheoTonKho(ChiTietSanPham e) {
        if (e.getSoLuong() == null) {
            return;
        }
        if (e.getSoLuong() <= 0) {
            e.setTrangThai(0);
        } else {
            if (e.getTrangThai() == null || e.getTrangThai() == 0) {
                e.setTrangThai(1);
            }
        }
    }
}