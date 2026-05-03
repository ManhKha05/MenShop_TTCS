package com.ttcs.menshop.modules.search.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiServiceClient {

    private final WebClient webClient;

    public AiServiceClient(WebClient.Builder webClientBuilder,
                           @Value("${ai.server.url:http://127.0.0.1:8000}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiResponse {
        private List<Double> vector;
    }

    public float[] getVectorFromText(String keyword) {
        try {
            long startTime = System.currentTimeMillis();

            AiResponse response = webClient.post()
                    .uri("/api/search/vectorize")
                    .bodyValue(Map.of("text", keyword))
                    .retrieve()
                    .bodyToMono(AiResponse.class)
                    .block();

            if (response != null && response.getVector() != null) {
                log.info("Lấy vector thành công: {} ms", (System.currentTimeMillis() - startTime));

                float[] floatVector = new float[response.getVector().size()];
                for (int i = 0; i < response.getVector().size(); i++) {
                    floatVector[i] = response.getVector().get(i).floatValue();
                }
                return floatVector;
            }
        } catch (Exception e) {
            log.error("Lỗi kết nối AI Server: {}", e.getMessage());
        }
        throw new RuntimeException("AI_UNAVAILABLE: " + keyword);
    }
}