package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.dto.ChiTietDotGiamGiaDTO;
import com.example.AerionSports_BE.dto.DotGiamGiaDTO;
import com.example.AerionSports_BE.entity.ChiTietDotGiamGia;
import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.DotGiamGia;
import com.example.AerionSports_BE.entity.SanPham;
import com.example.AerionSports_BE.repository.ChiTietDotGiamGiaRepository;
import com.example.AerionSports_BE.repository.ChiTietSanPhamRepository;
import com.example.AerionSports_BE.repository.DotGiamGiaRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
/**
 * Service chứa toàn bộ business rule của đợt giảm giá.
 * Đây là nơi quyết định:
 * - tìm kiếm/lọc danh sách
 * - tạo/sửa/hủy campaign
 * - map dữ liệu sang DTO cho view/API
 * - validate nghiệp vụ về ngày giờ, giá trị giảm và trạng thái
 */
public class DotGiamGiaService {

    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    // 2 là trạng thái "đang diễn ra" - đây là mốc đặc biệt để khóa một số trường khi sửa.
    private static final int TRANG_THAI_DANG_DIEN_RA = 2;
    // Tất cả rule so thời gian đều lấy theo múi giờ Việt Nam để đồng bộ với UI và scheduler.
    private static final ZoneId ZONE_VIETNAM = ZoneId.of("Asia/Ho_Chi_Minh");

    public Page<DotGiamGiaDTO> getDanhSach(
            String keyword,
            Integer trangThai,
            String tuNgay,
            String denNgay,
            int page,
            int size) {
        // Chuẩn hóa filter ngày, chạy query phân trang rồi map entity sang DTO cho layer hiển thị.
        validateFilterDateRange(tuNgay, denNgay);

        Pageable pageable = PageRequest.of(page, size);
        Page<DotGiamGia> pageResult = dotGiamGiaRepository.searchDotGiamGia(
                normalizeKeyword(keyword),
                trangThai,
                parseStartOfDay(tuNgay),
                parseEndOfDay(denNgay),
                pageable);

        return pageResult.map(this::toDTO);
    }

    @Transactional
    public DotGiamGiaDTO getById(Integer id) {
        // Load một campaign cụ thể và nạp luôn các dòng chi tiết đang active để màn view/edit dùng ngay.
        DotGiamGia entity = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));

        DotGiamGiaDTO dto = toDTO(entity);
        List<ChiTietDotGiamGia> chiTietList = chiTietDotGiamGiaRepository.findActiveByDotGiamGiaId(id);
        dto.setChiTietList(chiTietList.stream()
                .map(this::toChiTietDTO)
                .collect(Collectors.toList()));
        return dto;
    }

    @Transactional
    public DotGiamGiaDTO create(DotGiamGiaDTO dto) {
        // Tạo mới: validate rule trước, sinh mã tự động, set trạng thái, rồi lưu các sản phẩm áp dụng.
        validateCreateDotGiamGia(dto);

        DotGiamGia entity = new DotGiamGia();
        entity.setMaDotGiamGia(generateMaDotGiamGia());
        entity.setTenDotGiamGia(dto.getTenDotGiamGia());
        entity.setGiaTriGiam(dto.getGiaTriGiam());
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());
        entity.setMoTa(dto.getMoTa());
        entity.setTrangThai(calculateTrangThai(dto.getNgayBatDau(), dto.getNgayKetThuc()));

        DotGiamGia saved = dotGiamGiaRepository.save(entity);
        saveChiTietList(saved, dto.getChiTietList());
        return getById(saved.getId());
    }

    @Transactional
    public DotGiamGiaDTO update(Integer id, DotGiamGiaDTO dto) {
        // Update: cần kiểm tra trạng thái hiện tại vì campaign đang diễn ra có rule sửa riêng.
        DotGiamGia entity = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));

        Integer currentTrangThai = resolveTrangThai(entity);
        validateUpdateDotGiamGia(dto, entity, currentTrangThai);
        LocalDateTime ngayBatDau = currentTrangThai == TRANG_THAI_DANG_DIEN_RA
                ? entity.getNgayBatDau()
                : dto.getNgayBatDau();

        entity.setTenDotGiamGia(dto.getTenDotGiamGia());
        entity.setGiaTriGiam(dto.getGiaTriGiam());
        entity.setNgayBatDau(ngayBatDau);
        entity.setNgayKetThuc(dto.getNgayKetThuc());
        entity.setMoTa(dto.getMoTa());
        entity.setTrangThai(calculateTrangThai(entity.getNgayBatDau(), entity.getNgayKetThuc()));

        dotGiamGiaRepository.save(entity);
        chiTietDotGiamGiaRepository.deleteByDotGiamGia_Id(id);
        saveChiTietList(entity, dto.getChiTietList());
        return getById(id);
    }

    @Transactional
    public void delete(Integer id) {
        // Xóa mềm bằng cách chuyển campaign sang trạng thái hủy.
        DotGiamGia entity = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));
        entity.setTrangThai(0);
        dotGiamGiaRepository.save(entity);
    }

    @Transactional
    public DotGiamGiaDTO updateTrangThai(Integer id, Integer trangThai) {
        // Endpoint toggle ở UI gọi vào đây để đổi trạng thái thủ công.
        DotGiamGia entity = dotGiamGiaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đợt giảm giá với ID: " + id));
        validateTrangThai(trangThai);
        entity.setTrangThai(trangThai);
        dotGiamGiaRepository.save(entity);
        return toDTO(entity);
    }

    @Transactional
    public List<ChiTietDotGiamGiaDTO> getProductsForSelection(String keyword) {
        // Lấy các sản phẩm/biến thể còn active để đưa vào bảng chọn ở modal create/edit.
        List<ChiTietSanPham> products = chiTietSanPhamRepository.searchActiveProducts(normalizeKeyword(keyword));
        return products.stream()
                .map(this::toChiTietDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public DotGiamGiaDTO getDotGiamGiaHieuLucCaoNhat(Integer chiTietSanPhamId) {
        // Tìm đợt giảm giá đang hiệu lực mạnh nhất của 1 biến thể để dùng ở các màn tính giá.
        if (chiTietSanPhamId == null) {
            throw new IllegalArgumentException("ID sản phẩm chi tiết không được để trống");
        }

        LocalDateTime gioHienTaiVN = LocalDateTime.now(ZONE_VIETNAM);
        List<ChiTietDotGiamGia> activeDiscounts =
                chiTietDotGiamGiaRepository.findBestActiveByChiTietSanPhamId(chiTietSanPhamId, gioHienTaiVN);

        if (activeDiscounts.isEmpty()) {
            return null;
        }

        return toDTO(activeDiscounts.get(0).getDotGiamGia());
    }

    @Transactional
    public List<Map<String, Object>> getGroupedProductsForSelection(String keyword) {
        // Gom sản phẩm theo cha để template dựng accordion nhóm -> biến thể.
        List<ChiTietSanPham> products = chiTietSanPhamRepository.searchActiveProducts(normalizeKeyword(keyword));
        return products.stream()
                .filter(ctsp -> ctsp.getIdSanPham() != null)
                .collect(Collectors.groupingBy(
                        ctsp -> ctsp.getIdSanPham().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> {
                    ChiTietSanPham sample = entry.getValue().get(0);
                    SanPham sanPhamCha = sample.getIdSanPham();
                    Map<String, Object> group = new LinkedHashMap<>();
                    group.put("idSanPhamCha", sanPhamCha.getId());
                    group.put("maSanPham", sanPhamCha.getMaSanPham());
                    group.put("tenSanPham", sanPhamCha.getTenSanPham());
                    group.put("moTa", sanPhamCha.getMoTa());
                    group.put("baoHanh", sanPhamCha.getBaoHanh());
                    group.put("trangThai", sanPhamCha.getTrangThai());
                    group.put("tenThuongHieu", sanPhamCha.getIdThuongHieu() != null ? sanPhamCha.getIdThuongHieu().getTenThuongHieu() : null);
                    group.put("tenXuatXu", sanPhamCha.getIdXuatXu() != null ? sanPhamCha.getIdXuatXu().getTenXuatXu() : null);
                    group.put("tenDoCung", sanPhamCha.getIdDoCung() != null ? sanPhamCha.getIdDoCung().getTenDoCung() : null);
                    group.put("tenDiemCanBang", sanPhamCha.getIdDiemCanBang() != null ? sanPhamCha.getIdDiemCanBang().getTenDiemCanBang() : null);
                    group.put("tenChatLieuThanVot", sanPhamCha.getIdChatLieuThanVot() != null ? sanPhamCha.getIdChatLieuThanVot().getTenChatLieuThanVot() : null);
                    group.put("tenChatLieuKhungVot", sanPhamCha.getIdChatLieuKhungVot() != null ? sanPhamCha.getIdChatLieuKhungVot().getTenChatLieuKhungVot() : null);
                    group.put("tenDanhMuc", sanPhamCha.getIdDanhMuc() != null ? sanPhamCha.getIdDanhMuc().getTenDanhMuc() : null);
                    group.put("tenChuViCanVot", sanPhamCha.getIdChuViCanVot() != null ? sanPhamCha.getIdChuViCanVot().getTenChuViCanVot() : null);
                    group.put("mangBienTheCon", entry.getValue().stream()
                            .map(this::toChiTietDTO)
                            .peek(dto -> {
                                dto.setMaSanPham(sanPhamCha.getMaSanPham());
                                dto.setTenSanPham(sanPhamCha.getTenSanPham());
                                dto.setTenThuongHieu(sanPhamCha.getIdThuongHieu() != null ? sanPhamCha.getIdThuongHieu().getTenThuongHieu() : null);
                                dto.setTenXuatXu(sanPhamCha.getIdXuatXu() != null ? sanPhamCha.getIdXuatXu().getTenXuatXu() : null);
                                dto.setTenDoCung(sanPhamCha.getIdDoCung() != null ? sanPhamCha.getIdDoCung().getTenDoCung() : null);
                                dto.setTenDiemCanBang(sanPhamCha.getIdDiemCanBang() != null ? sanPhamCha.getIdDiemCanBang().getTenDiemCanBang() : null);
                                dto.setTenChatLieuThanVot(sanPhamCha.getIdChatLieuThanVot() != null ? sanPhamCha.getIdChatLieuThanVot().getTenChatLieuThanVot() : null);
                                dto.setTenChatLieuKhungVot(sanPhamCha.getIdChatLieuKhungVot() != null ? sanPhamCha.getIdChatLieuKhungVot().getTenChatLieuKhungVot() : null);
                                dto.setTenDanhMuc(sanPhamCha.getIdDanhMuc() != null ? sanPhamCha.getIdDanhMuc().getTenDanhMuc() : null);
                                dto.setTenChuViCanVot(sanPhamCha.getIdChuViCanVot() != null ? sanPhamCha.getIdChuViCanVot().getTenChuViCanVot() : null);
                                dto.setChuViCanVot(sanPhamCha.getIdChuViCanVot() != null ? sanPhamCha.getIdChuViCanVot().getTenChuViCanVot() : null);
                            })
                            .collect(Collectors.toList()));
                    return group;
                })
                .toList();
    }

    private String generateMaDotGiamGia() {
        // Mã DGGxxxx được sinh dựa trên phần số lớn nhất hiện có trong DB.
        Integer maxId = dotGiamGiaRepository.findMaxMaDotGiamGia();
        int nextId = (maxId != null ? maxId : 0) + 1;
        return String.format("DGG%04d", nextId);
    }

    private Integer calculateTrangThai(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        // Tự suy ra trạng thái dựa trên thời điểm hiện tại so với ngày bắt đầu/kết thúc.
        LocalDateTime now = LocalDateTime.now(ZONE_VIETNAM);
        if (ngayBatDau != null && now.isBefore(ngayBatDau)) {
            return 1;
        }
        if (ngayKetThuc != null && now.isAfter(ngayKetThuc)) {
            return 3;
        }
        return 2;
    }

    private Integer resolveTrangThai(DotGiamGia entity) {
        // Trạng thái hủy/kết thúc thủ công được giữ nguyên, còn lại thì tính lại theo thời gian.
        Integer trangThai = entity.getTrangThai();
        if (trangThai != null && (trangThai == 0 || trangThai == 3)) {
            return trangThai;
        }

        return calculateTrangThai(entity.getNgayBatDau(), entity.getNgayKetThuc());
    }

    private void validateTrangThai(Integer trangThai) {
        // Chỉ chấp nhận 4 trạng thái hợp lệ: 0,1,2,3.
        if (trangThai == null || trangThai < 0 || trangThai > 3) {
            throw new RuntimeException("Trạng thái đợt giảm giá không hợp lệ");
        }
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) {
            return "Không xác định";
        }
        return switch (trangThai) {
            case 0 -> "Đã hủy";
            case 1 -> "Sắp diễn ra";
            case 2 -> "Đang diễn ra";
            case 3 -> "Đã kết thúc";
            default -> "Không xác định";
        };
    }

    private LocalDateTime parseStartOfDay(String value) {
        // Chuyển ngày lọc sang đầu ngày để query range bao trọn ngày được chọn.
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value).atStartOfDay();
    }

    private LocalDateTime parseEndOfDay(String value) {
        // Chuyển ngày lọc sang cuối ngày để không bỏ sót bản ghi trong ngày đó.
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value).atTime(LocalTime.MAX);
    }

    private void validateFilterDateRange(String tuNgay, String denNgay) {
        // Bộ lọc ngày phải đi theo cặp, và ngày bắt đầu không được lớn hơn ngày kết thúc.
        boolean hasFrom = tuNgay != null && !tuNgay.isBlank();
        boolean hasTo = denNgay != null && !denNgay.isBlank();

        if (hasFrom ^ hasTo) {
            throw new IllegalArgumentException("Vui lòng chọn đủ từ ngày và đến ngày");
        }

        if (hasFrom && hasTo && LocalDate.parse(tuNgay).isAfter(LocalDate.parse(denNgay))) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }
    }

    private void validateCreateDotGiamGia(DotGiamGiaDTO dto) {
        // Rule khi tạo mới: bắt buộc đủ dữ liệu cơ bản, thứ tự thời gian đúng và không chọn quá khứ.
        validateDotGiamGiaRequired(dto);
        validateStartBeforeEnd(dto.getNgayBatDau(), dto.getNgayKetThuc());
        validateNotBeforeCurrentMinute(dto.getNgayBatDau(), "Ngày bắt đầu");
        validateNotBeforeCurrentMinute(dto.getNgayKetThuc(), "Ngày kết thúc");
    }

    private void validateUpdateDotGiamGia(DotGiamGiaDTO dto, DotGiamGia entity, Integer currentTrangThai) {
        // Rule khi sửa: campaign đang diễn ra không được đổi ngày bắt đầu, các thời điểm còn lại vẫn phải hợp lệ.
        validateDotGiamGiaRequired(dto);

        LocalDateTime effectiveNgayBatDau = currentTrangThai == TRANG_THAI_DANG_DIEN_RA
                ? entity.getNgayBatDau()
                : dto.getNgayBatDau();

        if (currentTrangThai == TRANG_THAI_DANG_DIEN_RA
                && !isSameMinute(dto.getNgayBatDau(), entity.getNgayBatDau())) {
            throw new RuntimeException("Đợt giảm giá đang diễn ra không được sửa ngày bắt đầu");
        }

        validateStartBeforeEnd(effectiveNgayBatDau, dto.getNgayKetThuc());

        if (currentTrangThai != TRANG_THAI_DANG_DIEN_RA) {
            validateNotBeforeCurrentMinute(dto.getNgayBatDau(), "Ngày bắt đầu");
        }
        validateNotBeforeCurrentMinute(dto.getNgayKetThuc(), "Ngày kết thúc");
    }

    private void validateDotGiamGiaRequired(DotGiamGiaDTO dto) {
        // Các field tối thiểu của campaign: tên, giá trị giảm, ngày bắt đầu và ngày kết thúc.
        if (dto == null) {
            throw new RuntimeException("Dữ liệu đợt giảm giá không hợp lệ");
        }
        if (dto.getTenDotGiamGia() == null || dto.getTenDotGiamGia().isBlank()) {
            throw new RuntimeException("Tên đợt giảm giá không được để trống");
        }
        if (dto.getGiaTriGiam() == null) {
            throw new RuntimeException("Giá trị giảm không được để trống");
        }
        validateGiaTriGiamPercent(dto.getGiaTriGiam());
        if (dto.getNgayBatDau() == null) {
            throw new RuntimeException("Ngày bắt đầu không được để trống");
        }
        if (dto.getNgayKetThuc() == null) {
            throw new RuntimeException("Ngày kết thúc không được để trống");
        }
    }

    private void validateStartBeforeEnd(LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc) {
        // Cặp thời gian chỉ hợp lệ khi ngày bắt đầu nằm trước ngày kết thúc.
        if (!ngayBatDau.isBefore(ngayKetThuc)) {
            throw new RuntimeException("Ngày bắt đầu phải trước ngày kết thúc");
        }
    }

    private void validateNotBeforeCurrentMinute(LocalDateTime value, String fieldName) {
        // Không cho chọn thời gian trong quá khứ, làm tròn theo phút để khớp input datetime-local.
        if (value.truncatedTo(ChronoUnit.MINUTES).isBefore(currentMinute())) {
            throw new RuntimeException(fieldName + " không được chọn thời gian trong quá khứ");
        }
    }

    private LocalDateTime currentMinute() {
        // Mốc thời gian hiện tại đã truncate xuống phút để so sánh nhất quán.
        return LocalDateTime.now(ZONE_VIETNAM).truncatedTo(ChronoUnit.MINUTES);
    }

    private boolean isSameMinute(LocalDateTime left, LocalDateTime right) {
        // So sánh theo phút, bỏ qua giây và nano vì input datetime-local không gửi phần này.
        return left != null
                && right != null
                && left.truncatedTo(ChronoUnit.MINUTES).isEqual(right.truncatedTo(ChronoUnit.MINUTES));
    }

    private void validateGiaTriGiamPercent(BigDecimal giaTriGiam) {
        // Giá trị giảm phải nằm trong khoảng 0 < x <= 100.
        if (giaTriGiam.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Giá trị giảm phải lớn hơn 0%");
        }
        if (giaTriGiam.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new RuntimeException("Giá trị giảm không được vượt quá 100%");
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void saveChiTietList(DotGiamGia dotGiamGia, List<ChiTietDotGiamGiaDTO> dtoList) {
        // Lưu các dòng chi tiết bằng cách:
        // 1) lấy unique id
        // 2) kiểm tra product có tồn tại thật không
        // 3) tạo entity liên kết campaign - biến thể
        if (dtoList == null || dtoList.isEmpty()) {
            return;
        }

        List<Integer> ids = dtoList.stream()
                .map(ChiTietDotGiamGiaDTO::getIdChiTietSanPham)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ids.isEmpty()) {
            return;
        }

        Map<Integer, ChiTietSanPham> productsById = chiTietSanPhamRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(ChiTietSanPham::getId, product -> product, (left, right) -> left, LinkedHashMap::new));

        List<ChiTietDotGiamGia> entities = new ArrayList<>();
        for (Integer productId : ids) {
            ChiTietSanPham ctsp = productsById.get(productId);
            if (ctsp == null) {
                throw new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + productId);
            }

            ChiTietDotGiamGia ct = new ChiTietDotGiamGia();
            ct.setDotGiamGia(dotGiamGia);
            ct.setChiTietSanPham(ctsp);
            entities.add(ct);
        }

        chiTietDotGiamGiaRepository.saveAll(entities);
    }

    private DotGiamGiaDTO toDTO(DotGiamGia entity) {
        // Map entity -> DTO để view/API chỉ làm việc với dữ liệu đã chuẩn hóa.
        DotGiamGiaDTO dto = new DotGiamGiaDTO();
        dto.setId(entity.getId());
        dto.setMaDotGiamGia(entity.getMaDotGiamGia());
        dto.setTenDotGiamGia(entity.getTenDotGiamGia());
        dto.setGiaTriGiam(entity.getGiaTriGiam());
        dto.setNgayBatDau(entity.getNgayBatDau());
        dto.setNgayKetThuc(entity.getNgayKetThuc());
        dto.setMoTa(entity.getMoTa());
        dto.setTrangThai(resolveTrangThai(entity));
        dto.setTrangThaiText(getTrangThaiText(dto.getTrangThai()));
        return dto;
    }

    private ChiTietDotGiamGiaDTO toChiTietDTO(ChiTietDotGiamGia entity) {
        // Map relation entity -> DTO chi tiết, giữ lại cả id relation và id sản phẩm chi tiết.
        ChiTietDotGiamGiaDTO dto = toChiTietDTO(entity.getChiTietSanPham());
        dto.setId(entity.getId());
        dto.setIdDotGiamGia(entity.getDotGiamGia() != null ? entity.getDotGiamGia().getId() : null);
        return dto;
    }

    private ChiTietDotGiamGiaDTO toChiTietDTO(ChiTietSanPham ctsp) {
        // Map một biến thể sản phẩm sang DTO hiển thị ở bảng chọn và bảng chi tiết.
        ChiTietDotGiamGiaDTO dto = new ChiTietDotGiamGiaDTO();
        if (ctsp == null) {
            return dto;
        }

        SanPham sanPhamCha = ctsp.getIdSanPham();
        String tenChuVi = sanPhamCha != null && sanPhamCha.getIdChuViCanVot() != null
                ? sanPhamCha.getIdChuViCanVot().getTenChuViCanVot()
                : null;
        String tenThuongHieu = sanPhamCha != null && sanPhamCha.getIdThuongHieu() != null
                ? sanPhamCha.getIdThuongHieu().getTenThuongHieu()
                : null;
        String tenXuatXu = sanPhamCha != null && sanPhamCha.getIdXuatXu() != null
                ? sanPhamCha.getIdXuatXu().getTenXuatXu()
                : null;

        dto.setId(ctsp.getId());
        dto.setIdChiTietSanPham(ctsp.getId());
        dto.setMaCtsp(ctsp.getMaCtsp());
        dto.setMaSanPham(sanPhamCha != null ? sanPhamCha.getMaSanPham() : ctsp.getMaCtsp());
        dto.setTenSanPham(sanPhamCha != null ? sanPhamCha.getTenSanPham() : null);
        dto.setTenThuongHieu(tenThuongHieu);
        dto.setAnhDaiDien(null);
        dto.setTenMauSac(ctsp.getIdMauSac() != null ? ctsp.getIdMauSac().getTenMauSac() : null);
        dto.setTenTrongLuong(ctsp.getIdTrongLuong() != null ? ctsp.getIdTrongLuong().getTenTrongLuong() : null);
        dto.setTenChuViCanVot(tenChuVi);
        dto.setChuViCanVot(tenChuVi);
        dto.setTenDoCung(sanPhamCha != null && sanPhamCha.getIdDoCung() != null
                ? sanPhamCha.getIdDoCung().getTenDoCung()
                : null);
        dto.setTenDiemCanBang(sanPhamCha != null && sanPhamCha.getIdDiemCanBang() != null
                ? sanPhamCha.getIdDiemCanBang().getTenDiemCanBang()
                : null);
        dto.setTenChatLieuThanVot(sanPhamCha != null && sanPhamCha.getIdChatLieuThanVot() != null
                ? sanPhamCha.getIdChatLieuThanVot().getTenChatLieuThanVot()
                : null);
        dto.setTenChatLieuKhungVot(sanPhamCha != null && sanPhamCha.getIdChatLieuKhungVot() != null
                ? sanPhamCha.getIdChatLieuKhungVot().getTenChatLieuKhungVot()
                : null);
        dto.setTenDanhMuc(sanPhamCha != null && sanPhamCha.getIdDanhMuc() != null
                ? sanPhamCha.getIdDanhMuc().getTenDanhMuc()
                : null);
        dto.setTenXuatXu(tenXuatXu);
        dto.setXuatXuChiTiet(tenXuatXu);
        dto.setGiaNhap(ctsp.getGiaNhap());
        dto.setGiaBan(ctsp.getGiaBan());
        dto.setSoLuong(ctsp.getSoLuong());
        dto.setSoLuongTon(ctsp.getSoLuong());
        dto.setTrangThai(ctsp.getTrangThai());
        dto.setGiaTriGiamRieng(null);
        dto.setGhiChu(null);
        return dto;
    }

}
