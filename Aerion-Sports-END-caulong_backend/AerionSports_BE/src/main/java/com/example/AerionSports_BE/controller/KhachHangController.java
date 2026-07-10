package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.response.KhachHangResponse;
import com.example.AerionSports_BE.entity.KhachHang;
import com.example.AerionSports_BE.service.KhachHangService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public/khach-hang")
@CrossOrigin("*")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // Thư mục vật lý lưu ảnh đại diện khách hàng (đổi lại theo cấu hình server thật của bạn)
    private static final String UPLOAD_DIR = "uploads/avatar/khach-hang";

    @GetMapping("/hien-thi")
    public ResponseEntity<List<KhachHang>> getAll() {
        List<KhachHang> list = khachHangService.getAll();
        System.out.println("======> Số lượng khách hàng lấy được từ DB: " + (list != null ? list.size() : 0));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<KhachHang> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(khachHangService.getById(id));
    }

    // 🌟 ĐÃ SỬA: nhận multipart/form-data (part "data" = JSON, part "avatar" = file ảnh)
    // thay vì @RequestBody JSON thuần, để khớp với FormData mà trang Thêm khách hàng gửi lên.
    // Vẫn hỗ trợ trường hợp không có ảnh (avatar = null).
    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> add(@RequestPart("data") String dataJson,
                                 @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        try {
            KhachHang khachHang = objectMapper.readValue(dataJson, KhachHang.class);

            if (avatar != null && !avatar.isEmpty()) {
                khachHang.setAvatar(luuFileAvatar(avatar));
            }

            KhachHang savedKhachHang = khachHangService.add(khachHang);
            return ResponseEntity.ok(savedKhachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lưu ảnh đại diện: " + e.getMessage());
        }
    }

    // Endpoint JSON thuần (giữ lại để tương thích ngược nếu nơi khác vẫn gọi JSON không kèm ảnh)
    @PostMapping(value = "/add", consumes = {"application/json"})
    public ResponseEntity<?> addJson(@RequestBody KhachHang khachHang) {
        try {
            KhachHang savedKhachHang = khachHangService.add(khachHang);
            return ResponseEntity.ok(savedKhachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update/{id:\\d+}", consumes = {"application/json"})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody KhachHang khachHang) {
        try {
            KhachHang updatedKhachHang = khachHangService.update(id, khachHang);
            return ResponseEntity.ok(updatedKhachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Bản update hỗ trợ đổi ảnh đại diện qua multipart, dùng khi cần upload file thật ở trang Sửa
    @PutMapping(value = "/update/{id:\\d+}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMultipart(@PathVariable Integer id,
                                             @RequestPart("data") String dataJson,
                                             @RequestPart(value = "avatar", required = false) MultipartFile avatar) {
        try {
            KhachHang khachHang = objectMapper.readValue(dataJson, KhachHang.class);
            if (avatar != null && !avatar.isEmpty()) {
                khachHang.setAvatar(luuFileAvatar(avatar));
            }
            KhachHang updatedKhachHang = khachHangService.update(id, khachHang);
            return ResponseEntity.ok(updatedKhachHang);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Lỗi khi lưu ảnh đại diện: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id:\\d+}")
    public ResponseEntity<String> delete(@PathVariable Integer id) {
        try {
            khachHangService.delete(id);
            return ResponseEntity.ok("Xóa khách hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/summary-info")
    public ResponseEntity<List<KhachHangResponse>> getKhachHangSummary() {
        return ResponseEntity.ok(khachHangService.getAllSummary());
    }

    // 🌟 ĐÃ SỬA: thêm tham số excludeId để khi đang sửa 1 khách hàng, SĐT/Email
    // của chính khách hàng đó không bị báo trùng với chính nó. Trước đây trang
    // Sửa phải tự tải lại toàn bộ danh sách và lọc bằng tay để bù cho thiếu sót này.
    @GetMapping("/check-trung")
    public ResponseEntity<?> checkTrungData(@RequestParam(value = "sdt", required = false) String sdt,
                                            @RequestParam(value = "email", required = false) String email,
                                            @RequestParam(value = "excludeId", required = false) Integer excludeId) {
        boolean trungSdt = false;
        boolean trungEmail = false;

        List<KhachHang> all = khachHangService.getAll();

        if (sdt != null && !sdt.trim().isEmpty()) {
            trungSdt = all.stream()
                    .anyMatch(kh -> sdt.trim().equals(kh.getSdt())
                            && (excludeId == null || !excludeId.equals(kh.getId())));
        }

        if (email != null && !email.trim().isEmpty()) {
            trungEmail = all.stream()
                    .anyMatch(kh -> email.trim().equalsIgnoreCase(kh.getEmail())
                            && (excludeId == null || !excludeId.equals(kh.getId())));
        }

        return ResponseEntity.ok(java.util.Map.of(
                "trungSdt", trungSdt,
                "trungEmail", trungEmail
        ));
    }

    private String luuFileAvatar(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "avatar";
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        String fileName = UUID.randomUUID() + extension;

        Path target = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Trả về đường dẫn public để FE hiển thị được (cần cấu hình static resource handler trỏ tới UPLOAD_DIR)
        return "/uploads/avatar/khach-hang/" + fileName;
    }
}