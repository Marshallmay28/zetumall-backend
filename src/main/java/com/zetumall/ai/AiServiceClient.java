package com.zetumall.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final WebClient.Builder webClientBuilder;
    
    @Value("${ai-service.url}")
    private String aiServiceUrl;
    
    /**
     * Generate AI product description
     */
    public String generateProductDescription(String name, String category, List<String> features, String authToken) {
        try {
            Map<String, Object> request = Map.of(
                "name", name,
                "category", category,
                "features", features != null ? features : List.of()
            );
            
            WebClient webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
            
            Mono<Map> response = webClient.post()
                    .uri("/api/ai/description")
                    .header("Authorization", "Bearer " + authToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> result = response.block();
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return (String) result.get("description");
            }
            
            log.warn("AI service returned unsuccessful response");
            return null;
            
        } catch (Exception e) {
            log.error("Failed to generate product description via AI service: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate AI store description
     */
    public String generateStoreDescription(String name, String category, String tagline, String authToken) {
        try {
            Map<String, Object> request = Map.of(
                "name", name,
                "category", category,
                "tagline", tagline != null ? tagline : ""
            );
            
            WebClient webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
            
            Mono<Map> response = webClient.post()
                    .uri("/api/ai/generate-store-description")
                    .header("Authorization", "Bearer " + authToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> result = response.block();
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return (String) result.get("description");
            }
            
            return null;
            
        } catch (Exception e) {
            log.error("Failed to generate store description via AI service: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Generate product tags
     */
    public List<String> generateProductTags(String name, String description, String category, String authToken) {
        try {
            Map<String, Object> request = Map.of(
                "name", name,
                "description", description,
                "category", category
            );
            
            WebClient webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
            
            Mono<Map> response = webClient.post()
                    .uri("/api/ai/tags")
                    .header("Authorization", "Bearer " + authToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> result = response.block();
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return (List<String>) result.get("tags");
            }
            
            return List.of();
            
        } catch (Exception e) {
            log.error("Failed to generate product tags via AI service: {}", e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Generate SEO metadata
     */
    public Map<String, String> generateSeoMetadata(String name, String description, String category, String authToken) {
        try {
            Map<String, Object> request = Map.of(
                "name", name,
                "description", description,
                "category", category
            );
            
            WebClient webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
            
            Mono<Map> response = webClient.post()
                    .uri("/api/ai/seo")
                    .header("Authorization", "Bearer " + authToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> result = response.block();
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return (Map<String, String>) result.get("seo");
            }
            
            return Map.of();
            
        } catch (Exception e) {
            log.error("Failed to generate SEO metadata via AI service: {}", e.getMessage());
            return Map.of();
        }
    }
    
    /**
     * Analyze product quality
     */
    public Map<String, Object> analyzeProductQuality(String name, String description, Double price, String category, String authToken) {
        try {
            Map<String, Object> request = Map.of(
                "name", name,
                "description", description,
                "price", price,
                "category", category
            );
            
            WebClient webClient = webClientBuilder.baseUrl(aiServiceUrl).build();
            
            Mono<Map> response = webClient.post()
                    .uri("/api/ai/quality-analysis")
                    .header("Authorization", "Bearer " + authToken)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> result = response.block();
            
            if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                return (Map<String, Object>) result.get("analysis");
            }
            
            return Map.of();
            
        } catch (Exception e) {
            log.error("Failed to analyze product quality via AI service: {}", e.getMessage());
            return Map.of();
        }
    }
}
