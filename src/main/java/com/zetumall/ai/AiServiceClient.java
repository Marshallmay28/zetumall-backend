package com.zetumall.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${ai.gemini.url}")
    private String geminiApiUrl;
    
    @Value("${ai.gemini.api-key}")
    private String geminiApiKey;
    
    /**
     * Generate AI product description
     */
    public String generateProductDescription(String name, String category, List<String> features, String authToken) {
        try {
            String featuresText = features != null && !features.isEmpty() 
                ? "\nKey Features:\n- " + String.join("\n- ", features) 
                : "";
                
            String prompt = String.format("""
                Generate a compelling, SEO-optimized product description for an e-commerce platform.

                Product Name: %s
                Category: %s%s

                Requirements:
                - Write 2-3 paragraphs (150-200 words)
                - Highlight key benefits and features
                - Use persuasive language that encourages purchase
                - Include relevant keywords for SEO
                - Professional and engaging tone
                - Focus on value proposition

                Product Description:""", name, category, featuresText);
                
            return callGemini(prompt);
            
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
            String taglineText = tagline != null && !tagline.isEmpty() ? "\nTagline: " + tagline : "";
            
            String prompt = String.format("""
                Generate a professional store description for an e-commerce marketplace.

                Store Name: %s
                Category: %s%s

                Requirements:
                - Write 2 paragraphs (100-150 words)
                - Establish brand identity and trustworthiness
                - Highlight store's unique value proposition
                - Professional and welcoming tone
                - Include category expertise

                Store Description:""", name, category, taglineText);
                
            return callGemini(prompt);
            
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
            String shortDesc = description != null && description.length() > 200 
                ? description.substring(0, 200) + "..." 
                : description;
                
            String prompt = String.format("""
                Generate 8-12 relevant tags for this product:

                Name: %s
                Category: %s
                Description: %s

                Requirements:
                - Tags should be single words or short phrases (2-3 words max)
                - Focus on searchability and discoverability
                - Include category-specific and general tags
                - Format as comma-separated list

                Tags:""", name, category, shortDesc);
                
            String responseText = callGemini(prompt);
            if (responseText != null) {
                return Arrays.stream(responseText.split(","))
                        .map(String::trim)
                        .limit(12)
                        .toList();
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
            String shortDesc = description != null && description.length() > 150 
                ? description.substring(0, 150) + "..." 
                : description;
                
            String prompt = String.format("""
                Generate SEO-optimized metadata for this product:

                Product Name: %s
                Category: %s
                Description: %s

                Generate:
                1. SEO Title (50-60 characters, include main keyword)
                2. Meta Description (150-160 characters, compelling call-to-action)

                Format:
                TITLE: [your title here]
                META: [your meta description here]""", name, category, shortDesc);
                
            String responseText = callGemini(prompt);
            if (responseText != null) {
                Map<String, String> seoData = new java.util.HashMap<>();
                String[] lines = responseText.split("\n");
                for (String line : lines) {
                    if (line.startsWith("TITLE:")) {
                        seoData.put("title", line.replace("TITLE:", "").trim());
                    } else if (line.startsWith("META:")) {
                        seoData.put("metaDescription", line.replace("META:", "").trim());
                    }
                }
                return seoData;
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
            String prompt = String.format("""
                Analyze this product listing quality:

                Name: %s
                Category: %s
                Price: $%.2f
                Description: %s

                Provide:
                1. Quality Score (0-100)
                2. Key strengths (2-3 points)
                3. Improvement suggestions (2-3 points)

                Format as JSON:
                {
                  "score": 85,
                  "strengths": ["point 1", "point 2"],
                  "improvements": ["suggestion 1", "suggestion 2"]
                }""", name, category, price != null ? price : 0.0, description);
                
            String responseText = callGemini(prompt);
            if (responseText != null) {
                // Strip markdown blocks if present
                String jsonStr = responseText.trim();
                if (jsonStr.startsWith("```json")) {
                    jsonStr = jsonStr.substring(7);
                } else if (jsonStr.startsWith("```")) {
                    jsonStr = jsonStr.substring(3);
                }
                if (jsonStr.endsWith("```")) {
                    jsonStr = jsonStr.substring(0, jsonStr.length() - 3);
                }
                
                try {
                    return objectMapper.readValue(jsonStr.trim(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                } catch (Exception e) {
                    log.warn("Failed to parse Gemini JSON response for quality analysis: {}", e.getMessage());
                }
            }
            
            return Map.of(
                "score", 70,
                "strengths", List.of("Product listed successfully"),
                "improvements", List.of("Consider adding more details")
            );
            
        } catch (Exception e) {
            log.error("Failed to analyze product quality via AI service: {}", e.getMessage());
            return Map.of();
        }
    }
    
    private String callGemini(String prompt) {
        try {
            Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                    Map.of("parts", List.of(
                        Map.of("text", prompt)
                    ))
                )
            );
            
            WebClient webClient = webClientBuilder.baseUrl(geminiApiUrl).build();
            
            Mono<Map> response = webClient.post()
                    .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class);
            
            Map<String, Object> result = response.block();
            
            if (result != null && result.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) result.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> firstCandidate = candidates.get(0);
                    Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                    if (content != null && content.containsKey("parts")) {
                        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                        if (!parts.isEmpty()) {
                            return (String) parts.get(0).get("text");
                        }
                    }
                }
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error making request to Gemini API: {}", e.getMessage());
            throw e;
        }
    }
}
