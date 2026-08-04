package com.example.AerionSports_BE.service;

import com.example.AerionSports_BE.entity.ChiTietSanPham;
import com.example.AerionSports_BE.entity.SanPham;
import com.example.AerionSports_BE.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final WebClient webClient;
    private final SanPhamRepository sanPhamRepository;

    @Value("${groq.api.key}")
    private String apiKey;

    public AIService(WebClient.Builder builder, SanPhamRepository sanPhamRepository) {
        this.sanPhamRepository = sanPhamRepository;
        this.webClient = builder.baseUrl("https://api.groq.com/openai/v1").build();
    }

    @Transactional(readOnly = true) // Đảm bảo đọc được LAZY fields như chiTietSanPhams
    public String getAiReply(String userMessage) {
        try {
            // 1. Tạo Context sản phẩm thực tế từ Database
            String productContext = buildProductContext(userMessage);

            // 2. Ghép dữ liệu DB vào System Instruction
            String systemInstruction = """
                Bạn là Trợ lý AI Chăm sóc Khách hàng chuyên nghiệp và thân thiện của Cửa hàng Vợt Cầu Lông Aerion Sports.
                
                [QUY TẮC PHONG CÁCH & ỨNG XỬ]:
                - Luôn xưng "Em" và gọi khách là "Anh/Chị" hoặc "Bạn".
                - Bắt đầu câu trả lời một cách lịch sự, hào hứng và dùng thêm icon hợp lý (🏸, 🔥, ✅, 👟).
                - Trả lời ngắn gọn, cô đọng, đi thẳng vào vấn đề (tối đa 3 - 4 câu).
                - Tuyệt đối KHÔNG tự nghĩ ra giá cả hay sản phẩm không có trong danh mục bên dưới.
                - Nếu khách muốn gặp người thật hoặc hỏi vấn đề ngoài khả năng (đổi trả phức tạp, khiếu nại nặng), hãy khuyên khách bấm nút "Gặp NV" phía trên khung chat.
                
                [DANH MỤC SẢN PHẨM THỰC TẾ TỪ CỬA HÀNG (DATABASE)]:
                %s
                
                [THÔNG TIN & CHÍNH SÁCH CỦA AERION SPORTS]:
                - Địa chỉ cửa hàng: 36 Đ. Hồ Tùng Mậu, Phú Diễn, Hà Nội.
                - Hotline hỗ trợ: 1900 6868 (8:00 - 22:00 tất cả các ngày).
                - Sản phẩm chính: Vợt cầu lông chính hãng (Yonex, Victor, Lining,...).
                - Chính sách bảo hành: Bảo hành 90 ngày với khung vợt lỗi từ nhà sản xuất.
                - Chính sách đổi trả: Đổi mới trong 7 ngày nếu sản phẩm chưa qua sử dụng.
                - Giao hàng: Đơn nội tỉnh / nội thành: Áp dụng cố định 22.000đ. Đơn liên tỉnh / nội miền: Áp dụng cố định 30.000đ. Đơn liên miền: 32.000đ.
                """.formatted(productContext);

            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "temperature", 0.5,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemInstruction),
                            Map.of("role", "user", "content", userMessage)
                    )
            );

            // 3. Gọi Groq API với Header UTF-8
            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(new MediaType("application", "json", StandardCharsets.UTF_8))
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }

            return "Em chào Anh/Chị! Em có thể tư vấn hoặc hỗ trợ gì về các dòng vợt cầu lông hôm nay ạ? 🏸";

        } catch (WebClientResponseException e) {
            System.err.println("❌ Groq API error status: " + e.getStatusCode());
            System.err.println("❌ Groq API error body: " + e.getResponseBodyAsString());
            return "Em chào Anh/Chị! Hiện hệ thống tư vấn tự động đang bận chút xíu. Anh/Chị bấm nút 'Gặp NV' để nhân viên bên em hỗ trợ trực tiếp ngay nhé! 🏸";
        } catch (Exception e) {
            e.printStackTrace();
            return "Em chào Anh/Chị! Hiện hệ thống tư vấn tự động đang bận chút xíu. Anh/Chị bấm nút 'Gặp NV' để nhân viên bên em hỗ trợ trực tiếp ngay nhé! 🏸";
        }
    }

    /**
     * ⚡ Tra cứu sản phẩm từ Database và định dạng thành Context cho AI đọc
     */
    private String buildProductContext(String userMessage) {
        StringBuilder sb = new StringBuilder();
        List<SanPham> sanPhams;

        String msgLower = userMessage.toLowerCase();

        // Lọc danh sách theo từ khóa khách hỏi
        if (msgLower.contains("yonex")) {
            sanPhams = sanPhamRepository.findTopProductsByKeyword("Yonex");
        } else if (msgLower.contains("victor")) {
            sanPhams = sanPhamRepository.findTopProductsByKeyword("Victor");
        } else if (msgLower.contains("lining")) {
            sanPhams = sanPhamRepository.findTopProductsByKeyword("Lining");
        } else {
            // ⚡ Đã cập nhật gọi hàm lấy 10 sản phẩm mới nhất theo ID
            sanPhams = sanPhamRepository.findTop10ByOrderByIdDesc();
        }

        if (sanPhams == null || sanPhams.isEmpty()) {
            return "Hiện chưa tìm thấy danh sách sản phẩm phù hợp trực tiếp với từ khóa.";
        }

        // Duyệt từng SanPham và lấy dữ liệu từ chiTietSanPhams (Set<ChiTietSanPham>)
        for (SanPham sp : sanPhams) {
            String tenThuongHieu = (sp.getIdThuongHieu() != null && sp.getIdThuongHieu().getTenThuongHieu() != null)
                    ? sp.getIdThuongHieu().getTenThuongHieu()
                    : "Aerion Sports";

            int tongSoLuong = 0;
            BigDecimal giaMin = null;
            BigDecimal giaMax = null;

            if (sp.getChiTietSanPhams() != null && !sp.getChiTietSanPhams().isEmpty()) {
                for (ChiTietSanPham ct : sp.getChiTietSanPhams()) {
                    if (ct.getSoLuong() != null) {
                        tongSoLuong += ct.getSoLuong();
                    }

                    BigDecimal gia = ct.getGiaBan();
                    if (gia != null) {
                        if (giaMin == null || gia.compareTo(giaMin) < 0) giaMin = gia;
                        if (giaMax == null || gia.compareTo(giaMax) > 0) giaMax = gia;
                    }
                }
            }

            String giaHienThi = "Liên hệ";
            if (giaMin != null && giaMax != null) {
                if (giaMin.compareTo(giaMax) == 0) {
                    giaHienThi = String.format("%,.0f VNĐ", giaMin);
                } else {
                    giaHienThi = String.format("%,.0f VNĐ - %,.0f VNĐ", giaMin, giaMax);
                }
            }

            sb.append(String.format("- [Mã: %s] %s | Thương hiệu: %s | Giá: %s | Tồn kho: %d\n",
                    sp.getMaSanPham(),
                    sp.getTenSanPham(),
                    tenThuongHieu,
                    giaHienThi,
                    tongSoLuong));
        }

        return sb.toString();
    }
}