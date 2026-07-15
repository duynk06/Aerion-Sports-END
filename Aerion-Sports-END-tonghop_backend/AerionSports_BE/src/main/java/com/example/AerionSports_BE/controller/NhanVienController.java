package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.entity.NhanVien;
import com.example.AerionSports_BE.repository.NhanVienRepository;
import com.example.AerionSports_BE.service.NhanVienService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/nhan-vien")
@CrossOrigin("*")
public class NhanVienController {

    @Autowired
    private NhanVienService nhanVienService;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // Đường dẫn thư mục lưu ảnh: đọc từ application.properties (key: app.upload.dir)
    // Nếu không cấu hình, mặc định là thư mục "uploads" ngay trong thư mục chạy project
    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    private String luuFileAnh(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "anh";
        String safeName = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
        String tenFileMoi = UUID.randomUUID() + "_" + safeName;

        Path thuMuc = Paths.get(uploadDir);
        Files.createDirectories(thuMuc);

        Path duongDan = thuMuc.resolve(tenFileMoi);
        Files.write(duongDan, file.getBytes());
        return tenFileMoi;
    }

    @GetMapping("/hien-thi")
    public ResponseEntity<List<NhanVien>> getAll() {
        List<NhanVien> list = nhanVienService.findAll();
        System.out.println("======> Số lượng nhân viên lấy được từ DB: "
                + (list != null ? list.size() : 0));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            NhanVien nv = nhanVienService.findById(id);

            if (nv == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "Không tìm thấy nhân viên"));
            }

            Map<String, Object> data = new HashMap<>();
            data.put("id", nv.getId());
            data.put("tenNv", nv.getTenNv());
            data.put("sdt", nv.getSdt());
            data.put("email", nv.getEmail());
            data.put("ngaySinh", nv.getNgaySinh());
            data.put("gioiTinh", nv.getGioiTinh());
            data.put("trangThai", nv.getTrangThai());
            data.put("avatar", nv.getAvatar());
            data.put("vaiTro", nv.getVaiTro());
            data.put("diaChi", nv.getDiaChi());

            // 🌟 Dùng trực tiếp các cột đã tách sẵn, không parse lại từ diaChi nữa
            data.put("tinhThanh", nv.getTinhThanh() != null ? nv.getTinhThanh() : "");
            data.put("quanHuyen", nv.getQuanHuyen() != null ? nv.getQuanHuyen() : "");
            data.put("phuongXa", nv.getPhuongXa() != null ? nv.getPhuongXa() : "");
            data.put("diaChiChiTiet", nv.getDiaChiChiTiet() != null ? nv.getDiaChiChiTiet() : "");

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy nhân viên"));
        }
    }

    @PostMapping(value = "/add", consumes = {"multipart/form-data"})
    public ResponseEntity<?> add(
            @RequestParam("data") String data,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            NhanVien nhanVien = objectMapper.readValue(data, NhanVien.class);

            if (avatarFile != null && !avatarFile.isEmpty()) {
                String tenFileMoi = luuFileAnh(avatarFile);
                nhanVien.setAvatar("/uploads/" + tenFileMoi);
            }

            return ResponseEntity.ok(nhanVienService.add(nhanVien));
        } catch (Exception e) {
            Map<String, String> body = new HashMap<>();
            body.put("message", "Thêm nhân viên thất bại: " + e.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    // Giữ lại: cập nhật bằng JSON thuần, dùng khi KHÔNG đổi ảnh đại diện
    @PutMapping(value = "/update/{id}", consumes = {"application/json"})
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @RequestBody NhanVien nhanVien) {
        try {
            return ResponseEntity.ok(nhanVienService.update(id, nhanVien));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    // 🌟 MỚI: cập nhật bằng multipart, dùng khi có đổi ảnh đại diện (nhận file ảnh thật
    // thay vì nhét chuỗi base64 vào JSON — tránh lỗi vượt quá độ dài cột avatar trong DB,
    // giống cách đã sửa cho KhachHangController).
    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateMultipart(
            @PathVariable Integer id,
            @RequestParam("data") String data,
            @RequestParam(value = "avatar", required = false) MultipartFile avatarFile) {
        try {
            NhanVien nhanVien = objectMapper.readValue(data, NhanVien.class);


            if (avatarFile != null && !avatarFile.isEmpty()) {
                String tenFileMoi = luuFileAnh(avatarFile);
                nhanVien.setAvatar("/uploads/" + tenFileMoi);
            }

            return ResponseEntity.ok(nhanVienService.update(id, nhanVien));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Cập nhật thất bại: " + e.getMessage()));
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            nhanVienService.delete(id);
            return ResponseEntity.ok(Map.of("message", "Xóa nhân viên thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Xóa nhân viên thất bại: " + e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<NhanVien>> search(@RequestParam String tenNv) {
        return ResponseEntity.ok(nhanVienService.search(tenNv));
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<Boolean> checkDuplicate(@RequestParam String sdt, @RequestParam String email) {
        boolean exists = nhanVienRepository.existsBySdtOrEmail(sdt, email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/check-duplicate-update")
    public ResponseEntity<Boolean> checkDuplicateUpdate(@RequestParam String sdt,
                                                        @RequestParam String email,
                                                        @RequestParam Integer id) {
        boolean sdtExists = nhanVienRepository.existsBySdtAndIdNot(sdt, id);
        boolean emailExists = nhanVienRepository.existsByEmailAndIdNot(email, id);
        return ResponseEntity.ok(sdtExists || emailExists);
    }
}