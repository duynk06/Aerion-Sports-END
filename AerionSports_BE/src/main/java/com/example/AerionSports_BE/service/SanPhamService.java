package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.request.ChiTietSanPhamRequest;
import com.example.AerionSports_BE.dto.request.SanPhamFilter;
import com.example.AerionSports_BE.dto.request.SanPhamRequest;
import com.example.AerionSports_BE.dto.response.ChiTietSanPhamResponse;
import com.example.AerionSports_BE.dto.response.SanPhamResponse;
import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.HinhAnhSp;
import com.example.AerionSports_BE.entity.SanPham;
import com.example.AerionSports_BE.repository.*;
import com.example.AerionSports_BE.service.impl.ISanPhamService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Service
public class SanPhamService implements ISanPhamService {

    @Autowired private SanPhamRepository repo;
    @Autowired private ThuongHieuRepository thuongHieuRepo;
    @Autowired private XuatXuRepository xuatXuRepo;
    @Autowired private ChiTietSanPhamRepository chiTietRepo;
    @Autowired private HinhAnhSpRepository hinhAnhRepo;
    @Autowired private MauSacRepository mauSacRepo;
    @Autowired private ChatLieuThanVotRepository chatLieuThanRepo;
    @Autowired private ChatLieuKhungVotRepository chatLieuKhungRepo;
    @Autowired private TrongLuongRepository trongLuongRepo;
    @Autowired private DoCungRepository doCungRepo;
    @Autowired private DanhMucRepository danhMucRepo;
    @Autowired private DiemCanBangRepository diemCanBangRepo;
    @Autowired private ChuViCanVotRepository chuViCanVotRepo;
    @Autowired private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    @Autowired private SanPhamRepository sanPhamRepo;

    @Value("${app.upload.dir}")
    private String uploadDir;

    // 🌟 BỔ SUNG: Tính tổng số sản phẩm hiện tại phục vụ sinh mã tự động tăng dần ngoài Controller
    @Override
    @Transactional(readOnly = true)
    public long countTotal() {
        return repo.count();
    }

    // 🌟 BỔ SUNG: Lấy toàn bộ sản phẩm (không phân trang) để Controller tính toán độ dài danh sách
    @Override
    @Transactional(readOnly = true)
    public List<SanPhamResponse> getAll() {
        return repo.findAll().stream().map(this::toRes).toList();
    }

    // 🌟 BỔ SUNG: Tìm kiếm thông tin sản phẩm cha theo ID phục vụ tính năng điều hướng Sửa (Edit)
    @Override
    @Transactional(readOnly = true)
    public SanPhamResponse findById(Integer id) {
        SanPham sanPham = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm cha có ID: " + id));
        return toRes(sanPham);
    }

    // Xóa hàm createProductWithVariants cũ (luồng Vue 3) vì đã chuyển hẳn sang hàm save nhận cấu trúc dữ liệu Thymeleaf phẳng bên dưới

    private ChiTietSanPhamResponse toChiTietRes(ChiTietSanPham ct) {
        if (ct == null) return null;

        String duongDanAnhThucTe = null;
        if (ct.getHinhAnhs() != null && !ct.getHinhAnhs().isEmpty()) {
            duongDanAnhThucTe = ct.getHinhAnhs().stream()
                    .filter(HinhAnhSp::getLaAnhChinh)
                    .map(HinhAnhSp::getDuongDanAnh)
                    .findFirst()
                    .orElse(ct.getHinhAnhs().iterator().next().getDuongDanAnh());
        }

        SanPham spCha = ct.getIdSanPham();
        Integer idSanPhamCha = spCha != null ? spCha.getId() : null;
        String maSanPhamCha = spCha != null ? spCha.getMaSanPham() : null;
        String tenSanPhamCha = spCha != null ? spCha.getTenSanPham() : null;

        java.math.BigDecimal phanTramGiam = java.math.BigDecimal.ZERO;
        java.math.BigDecimal giaDaGiam = ct.getGiaBan();
        java.time.LocalDateTime gioHienTaiVietNam = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh"));
        List<com.example.AerionSports_BE.entity.ChiTietDotGiamGia> discountLinks =
                chiTietDotGiamGiaRepository.findBestActiveByChiTietSanPhamId(ct.getId(), gioHienTaiVietNam);

        if (discountLinks != null && !discountLinks.isEmpty()) {
            com.example.AerionSports_BE.entity.DotGiamGia dgg = discountLinks.get(0).getDotGiamGia();
            if (dgg != null && dgg.getGiaTriGiam() != null) {
                phanTramGiam = dgg.getGiaTriGiam();
                java.math.BigDecimal heSo = java.math.BigDecimal.valueOf(100).subtract(phanTramGiam);
                giaDaGiam = ct.getGiaBan().multiply(heSo).divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            }
        }

        ChiTietSanPhamResponse res = new ChiTietSanPhamResponse();
        res.setId(ct.getId());
        res.setIdSanPham(idSanPhamCha);
        res.setMaSanPham(maSanPhamCha);
        res.setMaCtsp(ct.getMaCtsp());
        res.setTenSanPham(tenSanPhamCha);

        res.setIdMauSac(ct.getIdMauSac() != null ? ct.getIdMauSac().getId() : null);
        res.setTenMauSac(ct.getIdMauSac() != null ? ct.getIdMauSac().getTenMauSac() : null);
        res.setIdTrongLuong(ct.getIdTrongLuong() != null ? ct.getIdTrongLuong().getId() : null);
        res.setTenTrongLuong(ct.getIdTrongLuong() != null ? ct.getIdTrongLuong().getTenTrongLuong() : null);

        res.setGiaNhap(ct.getGiaNhap());
        res.setGiaBan(ct.getGiaBan());
        res.setSoLuong(ct.getSoLuong());
        res.setTrangThai(ct.getTrangThai());
        res.setNgayTao(ct.getNgayTao());
        res.setNgayCapNhat(ct.getNgayCapNhat());
        res.setHinhAnh(duongDanAnhThucTe);
        res.setGiaDaGiam(giaDaGiam);
        res.setPhanTramGiam(phanTramGiam);

        return res;
    }

    private SanPhamResponse toRes(SanPham e) {
        Set<ChiTietSanPhamResponse> chiTietDTOs = new LinkedHashSet<>();
        if (e.getChiTietSanPhams() != null) {
            for (ChiTietSanPham ct : e.getChiTietSanPhams()) {
                chiTietDTOs.add(this.toChiTietRes(ct));
            }
        }

        return new SanPhamResponse(
                e.getId(),
                e.getIdThuongHieu() != null ? e.getIdThuongHieu().getId() : null,
                e.getIdThuongHieu() != null ? e.getIdThuongHieu().getTenThuongHieu() : null,
                e.getIdXuatXu() != null ? e.getIdXuatXu().getId() : null,
                e.getIdXuatXu() != null ? e.getIdXuatXu().getTenXuatXu() : null,

                e.getIdDoCung() != null ? e.getIdDoCung().getId() : null,
                e.getIdDoCung() != null ? e.getIdDoCung().getTenDoCung() : null,
                e.getIdDiemCanBang() != null ? e.getIdDiemCanBang().getId() : null,
                e.getIdDiemCanBang() != null ? e.getIdDiemCanBang().getTenDiemCanBang() : null,
                e.getIdChatLieuThanVot() != null ? e.getIdChatLieuThanVot().getId() : null,
                e.getIdChatLieuThanVot() != null ? e.getIdChatLieuThanVot().getTenChatLieuThanVot() : null,
                e.getIdChatLieuKhungVot() != null ? e.getIdChatLieuKhungVot().getId() : null,
                e.getIdChatLieuKhungVot() != null ? e.getIdChatLieuKhungVot().getTenChatLieuKhungVot() : null,
                e.getIdDanhMuc() != null ? e.getIdDanhMuc().getId() : null,
                e.getIdDanhMuc() != null ? e.getIdDanhMuc().getTenDanhMuc() : null,
                e.getIdChuViCanVot() != null ? e.getIdChuViCanVot().getId() : null,
                e.getIdChuViCanVot() != null ? e.getIdChuViCanVot().getTenChuViCanVot() : null,

                e.getMaSanPham(), e.getTenSanPham(), e.getMoTa(),
                e.getTrangThai(), e.getNgayTao(), e.getNgaySua(),
                chiTietDTOs
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SanPhamResponse> search(SanPhamFilter f) {
        return repo.search(
                f.getKeyword(),
                f.getIdThuongHieu(),
                f.getIdXuatXu(),
                f.getIdDanhMuc(),
                f.getIdChuViCanVot(),
                f.getIdDoCung(),
                f.getIdDiemCanBang(),
                f.getTrangThai(),
                f.getSoLuongMin(),
                f.getGiaMax(),
                PageRequest.of(f.getPage(), f.getSize(), Sort.by("ngayTao").descending())
        ).map(this::toRes);
    }

    @Override
    @Transactional
    public void updateTrangThai(Integer id, Integer trangThai) {
        // 1. Cập nhật trạng thái cho sản phẩm cha
        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm yêu cầu"));

        sp.setTrangThai(trangThai);
        sanPhamRepo.save(sp);

        sanPhamRepo.updateTrangThaiBienTheTheoSanPhamCha(id, trangThai);
    }

    // 🌟 ĐÃ TỐI ƯU TOÀN DIỆN: Hàm save bóc tách chuỗi JSON ma trận biến thể và liên kết chính xác file ảnh theo từng màu sắc
    @Transactional
    @Override
    public SanPhamResponse save(SanPhamRequest r) {
        if (repo.existsByMaSanPham(r.getMaSanPham())) throw new RuntimeException("Mã sản phẩm cha đã tồn tại!");

        // 1. Lưu sản phẩm cha trước
        SanPham e = new SanPham();
        mapFields(e, r);
        e.setNgayTao(Instant.now());
        e.setNgaySua(Instant.now());
        SanPham sanPhamDaLuu = repo.save(e);

        // 2. Phân tích cú pháp chuỗi JSON ma trận biến thể thành List DTO từ biểu mẫu Thymeleaf
        if (r.getBienTheJson() != null && !r.getBienTheJson().isBlank()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<ChiTietSanPhamRequest> chiTietRequests = mapper.readValue(
                        r.getBienTheJson(),
                        new TypeReference<List<ChiTietSanPhamRequest>>() {}
                );

                // 3. Xử lý lưu trước danh sách các file ảnh vật lý lên đĩa cứng và map vào Map[idMauSac -> đường dẫn web]
                Map<Integer, String> mauPathMap = new HashMap<>();
                if (r.getFileAnhs() != null && r.getFileAnhMauIds() != null) {
                    for (int i = 0; i < r.getFileAnhs().size(); i++) {
                        MultipartFile file = r.getFileAnhs().get(i);
                        if (file != null && !file.isEmpty() && i < r.getFileAnhMauIds().size()) {
                            Integer mauId = r.getFileAnhMauIds().get(i);
                            String fileName = saveFileToDisk(file);
                            mauPathMap.put(mauId, "/uploads/" + fileName);
                        }
                    }
                }

                // 4. Lặp và lưu tuần tự từng biến thể chi tiết con trong ma trận
                for (ChiTietSanPhamRequest ctReq : chiTietRequests) {
                    ChiTietSanPham ctEntity = new ChiTietSanPham();
                    ctEntity.setIdSanPham(sanPhamDaLuu);
                    ctEntity.setMaCtsp(ctReq.getMaCtsp());

                    if (ctReq.getIdMauSac() != null) ctEntity.setIdMauSac(mauSacRepo.findById(ctReq.getIdMauSac()).orElseThrow());
                    if (ctReq.getIdTrongLuong() != null) ctEntity.setIdTrongLuong(trongLuongRepo.findById(ctReq.getIdTrongLuong()).orElseThrow());

                    ctEntity.setGiaNhap(ctReq.getGiaNhap());
                    ctEntity.setGiaBan(ctReq.getGiaBan());
                    ctEntity.setSoLuong(ctReq.getSoLuong());
                    ctEntity.setTrangThai(1);
                    ctEntity.setNgayTao(Instant.now());
                    ctEntity.setNgayCapNhat(Instant.now());

                    ChiTietSanPham chiTietDaLuu = chiTietRepo.save(ctEntity);

                    // 5. Trích xuất đường dẫn ảnh chính xác của nhóm màu này để ghi nhận vào bảng hinh_anh_sp
                    String pathAnhCuaMau = mauPathMap.get(ctReq.getIdMauSac());
                    if (pathAnhCuaMau != null && !pathAnhCuaMau.isEmpty()) {
                        HinhAnhSp anhEntity = new HinhAnhSp();
                        anhEntity.setIdSanPhamChiTiet(chiTietDaLuu);
                        anhEntity.setDuongDanAnh(pathAnhCuaMau);
                        anhEntity.setLaAnhChinh(true);
                        anhEntity.setTrangThai(1);
                        hinhAnhRepo.save(anhEntity);
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException("Lỗi hệ thống khi bóc tách ma trận biến thể hoặc lưu ảnh: " + ex.getMessage());
            }
        }
        return toRes(repo.findById(sanPhamDaLuu.getId()).orElse(sanPhamDaLuu));
    }

    @Transactional
    @Override
    public SanPhamResponse update(Integer id, SanPhamRequest r) {
        SanPham e = repo.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm!"));
        if (repo.existsByMaSanPhamAndIdNot(r.getMaSanPham(), id)) throw new RuntimeException("Mã sản phẩm đã được sử dụng!");

        mapFields(e, r);
        SanPham savedSanPham = repo.save(e);

        if (r.getChiTietSanPhams() != null) {
            for (ChiTietSanPhamRequest ctReq : r.getChiTietSanPhams()) {
                ChiTietSanPham ctEntity = chiTietRepo.findByMaCtsp(ctReq.getMaCtsp()).orElse(new ChiTietSanPham());
                ctEntity.setIdSanPham(savedSanPham);
                ctEntity.setMaCtsp(ctReq.getMaCtsp());
                if (ctReq.getIdMauSac() != null) ctEntity.setIdMauSac(mauSacRepo.findById(ctReq.getIdMauSac()).orElse(null));
                if (ctReq.getIdTrongLuong() != null) ctEntity.setIdTrongLuong(trongLuongRepo.findById(ctReq.getIdTrongLuong()).orElse(null));
                ctEntity.setGiaNhap(ctReq.getGiaNhap());
                ctEntity.setGiaBan(ctReq.getGiaBan());
                ctEntity.setSoLuong(ctReq.getSoLuong());
                ctEntity.setNgayCapNhat(Instant.now());
                chiTietRepo.save(ctEntity);
            }
        }
        return toRes(repo.findById(savedSanPham.getId()).orElse(savedSanPham));
    }

    private void mapFields(SanPham e, SanPhamRequest r) {
        e.setIdXuatXu(r.getIdXuatXu() != null ? xuatXuRepo.findById(r.getIdXuatXu()).orElse(null) : null);
        e.setIdThuongHieu(r.getIdThuongHieu() != null ? thuongHieuRepo.findById(r.getIdThuongHieu()).orElse(null) : null);

        e.setIdDoCung(r.getIdDoCung() != null ? doCungRepo.findById(r.getIdDoCung()).orElse(null) : null);
        e.setIdDiemCanBang(r.getIdDiemCanBang() != null ? diemCanBangRepo.findById(r.getIdDiemCanBang()).orElse(null) : null);
        e.setIdChatLieuThanVot(r.getIdChatLieuThanVot() != null ? chatLieuThanRepo.findById(r.getIdChatLieuThanVot()).orElse(null) : null);
        e.setIdChatLieuKhungVot(r.getIdChatLieuKhungVot() != null ? chatLieuKhungRepo.findById(r.getIdChatLieuKhungVot()).orElse(null) : null);
        e.setIdDanhMuc(r.getIdDanhMuc() != null ? danhMucRepo.findById(r.getIdDanhMuc()).orElse(null) : null);
        e.setIdChuViCanVot(r.getIdChuViCanVot() != null ? chuViCanVotRepo.findById(r.getIdChuViCanVot()).orElse(null) : null);

        e.setMaSanPham(r.getMaSanPham());
        e.setTenSanPham(r.getTenSanPham());
        e.setMoTa(r.getMoTa());
        e.setTrangThai(r.getTrangThai());
    }

    @Override
    public void delete(Integer id) {
        if (!repo.existsById(id)) throw new RuntimeException("Không tìm thấy sản phẩm cần xóa");
        repo.deleteById(id);
    }

    private String saveFileToDisk(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String tenGoc = file.getOriginalFilename() != null ? file.getOriginalFilename() : "anh.jpg";
        String tenSachSe = boDauTiengViet(tenGoc).replaceAll("[^a-zA-Z0-9._-]", "_");
        String fileName = UUID.randomUUID().toString() + "_" + tenSachSe;

        Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    private String boDauTiengViet(String input) {
        String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D');
    }

    @Transactional
    public void updateSingleVariantWithImage(Integer idCtsp, ChiTietSanPhamRequest req, MultipartFile file) throws IOException {
        ChiTietSanPham ct = chiTietRepo.findById(idCtsp)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể con!"));

        ct.setGiaNhap(req.getGiaNhap());
        ct.setGiaBan(req.getGiaBan());
        ct.setSoLuong(req.getSoLuong());
        if (req.getIdMauSac() != null) ct.setIdMauSac(mauSacRepo.findById(req.getIdMauSac()).orElse(null));
        if (req.getIdTrongLuong() != null) ct.setIdTrongLuong(trongLuongRepo.findById(req.getIdTrongLuong()).orElse(null));
        ct.setNgayCapNhat(Instant.now());
        ChiTietSanPham savedCt = chiTietRepo.save(ct);

        if (file != null && !file.isEmpty()) {
            String fileName = saveFileToDisk(file);
            String dbImagePath = "/uploads/" + fileName;

            HinhAnhSp anhEntity = hinhAnhRepo.findAll().stream()
                    .filter(anh -> anh.getIdSanPhamChiTiet() != null && anh.getIdSanPhamChiTiet().getId().equals(idCtsp))
                    .findFirst()
                    .orElse(new HinhAnhSp());

            anhEntity.setIdSanPhamChiTiet(savedCt);
            anhEntity.setDuongDanAnh(dbImagePath);
            anhEntity.setLaAnhChinh(true);
            anhEntity.setTrangThai(1);

            hinhAnhRepo.save(anhEntity);
        }
    }
}