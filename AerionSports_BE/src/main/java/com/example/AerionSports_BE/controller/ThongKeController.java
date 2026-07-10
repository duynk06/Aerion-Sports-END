package com.example.AerionSports_BE.controller;

import com.example.AerionSports_BE.dto.response.ThongKeCardResponse;
import com.example.AerionSports_BE.dto.response.ThongKeChiTietResponse;
import com.example.AerionSports_BE.service.EmailService;
import com.example.AerionSports_BE.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

// 🌟 ĐÃ SỬA: Không còn @RestController -> dùng @Controller để có thể vừa trả JSON (qua @ResponseBody)
// vừa trả về tên view Thymeleaf (trang /thong-ke) trong cùng 1 class.
@CrossOrigin("*")
@Controller
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    @Autowired
    private EmailService emailService;

    // 🌟 ĐÃ SỬA: Controller trỏ THẲNG vào file thong-ke.html, không cần file trung gian nữa.
    // File thong-ke.html tự chứa th:replace ở thẻ <html> để gọi layout + tự chọn
    // fragment "content" của chính nó qua ~{::content} — đây là đúng pattern chuẩn
    // của Thymeleaf (Template Layout), tránh được lỗi FragmentExpression khi trước.
    @GetMapping("/thong-ke")
    public String trangThongKe() {
        return "thong-ke/thong-ke";
    }

    // ============================================================
    // CÁC API BÊN DƯỚI GIỮ NGUYÊN LOGIC CŨ, CHỈ THÊM @ResponseBody
    // VÌ CLASS KHÔNG CÒN LÀ @RestController NỮA
    // ============================================================

    @GetMapping("/api/thong-ke/dashboard-cards")
    @ResponseBody
    public ResponseEntity<Map<String, ThongKeCardResponse>> getAllDashboardCards() {
        Map<String, ThongKeCardResponse> responseMap = new java.util.HashMap<>();
        responseMap.put("today", thongKeService.getSingleCardData("today"));
        responseMap.put("week", thongKeService.getSingleCardData("week"));
        responseMap.put("month", thongKeService.getSingleCardData("month"));
        responseMap.put("year", thongKeService.getSingleCardData("year"));
        return ResponseEntity.ok(responseMap);
    }

    @GetMapping("/api/thong-ke/chi-tiet-tables")
    @ResponseBody
    public ResponseEntity<ThongKeChiTietResponse> getChiTietTables(
            @RequestParam(value = "tuNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(value = "denNgay", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(thongKeService.getThongKeChiTietDuLieuDong(tuNgay, denNgay));
    }

    @PostMapping("/api/thong-ke/gui-email-thu-cong")
    @ResponseBody
    public ResponseEntity<Map<String, String>> triggerSendEmailManual() {
        emailService.executeExportExcelAndSendEmail();
        Map<String, String> response = new java.util.HashMap<>();
        response.put("status", "success");
        response.put("message", "Hệ thống đã kết xuất Excel và gửi về Email quản trị thành công!");
        return ResponseEntity.ok(response);
    }

    // 🌟 ĐÃ SỬA ĐỒNG BỘ: Hỗ trợ linh hoạt cả 2 cấu trúc hiển thị Thường và So Sánh
    @GetMapping("/api/thong-ke/bieu-do-line")
    @ResponseBody
    public ResponseEntity<?> getChartLineData(
            @RequestParam("loai") String loai,
            @RequestParam(value = "isCompare", required = false, defaultValue = "false") boolean isCompare,
            @RequestParam(value = "thang", required = false, defaultValue = "6") int thang,
            @RequestParam(value = "nam", required = false, defaultValue = "2026") int nam,
            @RequestParam(value = "thangGoc", required = false, defaultValue = "4") int thangGoc,
            @RequestParam(value = "thangSoSanh", required = false, defaultValue = "5") int thangSoSanh,
            @RequestParam(value = "namGoc", required = false, defaultValue = "2026") int namGoc,
            @RequestParam(value = "namSoSanh", required = false, defaultValue = "2025") int namSoSanh) {

        // 🌟 LUỒNG 1: Nếu FE đang tắt nút so sánh (Chế độ xem thông thường)
        if (!isCompare) {
            return ResponseEntity.ok(thongKeService.getDoanhThuDoThiBieuDo(thang, nam));
        }

        // 🌟 LUỒNG 2: Nếu FE đang bật chế độ so sánh nâng cao
        Map<String, Object> response = new java.util.HashMap<>();
        if ("thang".equalsIgnoreCase(loai)) {
            response.put("gocLabel", "Năm " + namGoc);
            response.put("gocData", thongKeService.getDoanhThuTheoNam(namGoc));
            response.put("ssLabel", "Năm " + namSoSanh);
            response.put("ssData", thongKeService.getDoanhThuTheoNam(namSoSanh));
        } else if ("quy".equalsIgnoreCase(loai)) {
            response.put("gocLabel", "Năm " + namGoc);
            response.put("gocData", thongKeService.getDoanhThuTheoQuy(namGoc));
            response.put("ssLabel", "Năm " + namSoSanh);
            response.put("ssData", thongKeService.getDoanhThuTheoQuy(namSoSanh));
        } else {
            response.put("gocLabel", "Tháng " + thangGoc + "/" + namGoc);
            response.put("gocData", thongKeService.getDoanhThuDoThiBieuDo(thangGoc, namGoc));
            response.put("ssLabel", "Tháng " + thangSoSanh + "/" + namGoc);
            response.put("ssData", thongKeService.getDoanhThuDoThiBieuDo(thangSoSanh, namGoc));
        }
        return ResponseEntity.ok(response);
    }
}