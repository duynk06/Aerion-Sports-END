package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.ChiTietDotGiamGiaDTO;
import com.example.AerionSports_BE.dto.DotGiamGiaDTO;
import com.example.AerionSports_BE.service.DotGiamGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dot-giam-gia")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
/**
 * REST API cho đợt giảm giá.
 * File này phục vụ các client gọi JSON, còn màn admin HTML dùng ViewController riêng.
 */
public class DotGiamGiaController {

    private final DotGiamGiaService dotGiamGiaService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDanhSach(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            // Trả về nội dung trang + metadata phân trang để client tự render.
            Page<DotGiamGiaDTO> pageResult = dotGiamGiaService.getDanhSach(
                    keyword,
                    trangThai,
                    tuNgay,
                    denNgay,
                    page,
                    size);

            Map<String, Object> response = new HashMap<>();
            response.put("content", pageResult.getContent());
            response.put("totalElements", pageResult.getTotalElements());
            response.put("totalPages", pageResult.getTotalPages());
            response.put("currentPage", pageResult.getNumber());
            response.put("size", pageResult.getSize());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            // Lấy toàn bộ thông tin của một đợt giảm giá theo id.
            DotGiamGiaDTO dto = dotGiamGiaService.getById(id);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DotGiamGiaDTO dto) {
        try {
            // Tạo mới từ JSON body, phần validate nghiệp vụ nằm ở service.
            DotGiamGiaDTO created = dotGiamGiaService.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DotGiamGiaDTO dto) {
        try {
            // Cập nhật campaign theo id, giữ nguyên rule nghiệp vụ của service.
            DotGiamGiaDTO updated = dotGiamGiaService.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            // Xóa mềm: không xóa vật lý mà chuyển trạng thái đợt giảm giá sang hủy.
            dotGiamGiaService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Đã hủy đợt giảm giá thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id:\\d+}/trang-thai")
    public ResponseEntity<?> updateTrangThai(
            @PathVariable Integer id,
            @RequestParam Integer trangThai) {
        try {
            // Switch trạng thái từ client sẽ đi qua endpoint này.
            DotGiamGiaDTO updated = dotGiamGiaService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/san-pham")
    public ResponseEntity<?> getProductsForSelection(
            @RequestParam(required = false) String keyword) {
        try {
            // Lấy danh sách sản phẩm/biến thể còn active để đổ vào bảng chọn.
            List<ChiTietDotGiamGiaDTO> products = dotGiamGiaService.getProductsForSelection(keyword);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/san-pham/{chiTietSanPhamId}/hieu-luc")
    public ResponseEntity<?> getDotGiamGiaHieuLuc(
            @PathVariable Integer chiTietSanPhamId) {
        try {
            // Trả về campaign giảm giá mạnh nhất đang còn hiệu lực cho 1 biến thể.
            DotGiamGiaDTO best = dotGiamGiaService.getDotGiamGiaHieuLucCaoNhat(chiTietSanPhamId);
            return ResponseEntity.ok(best);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/grouped-products")
    public ResponseEntity<?> getGroupedProducts(@RequestParam(value = "keyword", required = false) String keyword) {
        // Trả dữ liệu cha/con để client dựng accordion chọn sản phẩm.
        return ResponseEntity.ok(dotGiamGiaService.getGroupedProductsForSelection(keyword));
    }
}
