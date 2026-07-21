package com.example.AerionSports_BE.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class AIService {

    private final WebClient webClient;

    @Value("${groq.api.key:}")
    private String apiKey;

    public AIService(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("https://api.groq.com/openai/v1").build();
    }

    public String getAiReply(String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            return "AI chưa được cấu hình. Vui lòng thiết lập biến môi trường GROQ_API_KEY.";
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", "llama-3.3-70b-versatile",
                    "messages", List.of(
                            Map.of("role", "user", "content", userMessage)
                    )
            );

            Map<String, Object> response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("content-type", "application/json")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");

        } catch (WebClientResponseException e) {
            System.out.println("Groq API error status: " + e.getStatusCode());
            System.out.println("Groq API error body: " + e.getResponseBodyAsString());
            e.printStackTrace();
            return "Xin lỗi, hệ thống AI đang gặp sự cố. Vui lòng thử lại hoặc bấm 'Gặp NV' để được hỗ trợ trực tiếp.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, hệ thống AI đang gặp sự cố. Vui lòng thử lại hoặc bấm 'Gặp NV' để được hỗ trợ trực tiếp.";
        }
    }
}
