package com.zetumall.ai;

import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AiController {

    private final AiServiceClient aiServiceClient;

    /**
     * Generate product description using AI
     * POST /api/ai/product-description
     */
    @PostMapping("/product-description")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateProductDescription(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser,
            HttpServletRequest httpRequest) {
        try {
            String name = (String) request.get("name");
            String category = (String) request.get("category");
            List<String> features = (List<String>) request.get("features");

            // Get the JWT token from request to pass to AI service
            String token = extractJwtToken(httpRequest);

            String description = aiServiceClient.generateProductDescription(name, category, features, token);

            if (description != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(Map.of("description", description), "Description generated successfully"));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.error("Failed to generate description. AI service may be unavailable."));
            }

        } catch (Exception e) {
            log.error("Error generating product description", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate description"));
        }
    }

    /**
     * Generate store description using AI
     * POST /api/ai/store-description
     */
    @PostMapping("/store-description")
    public ResponseEntity<ApiResponse<Map<String, String>>> generateStoreDescription(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser,
            HttpServletRequest httpRequest) {
        try {
            String name = (String) request.get("name");
            String category = (String) request.get("category");
            String tagline = (String) request.get("tagline");

            String token = extractJwtToken(httpRequest);

            String description = aiServiceClient.generateStoreDescription(name, category, tagline, token);

            if (description != null) {
                return ResponseEntity.ok(
                        ApiResponse.success(Map.of("description", description),
                                "Store description generated successfully"));
            } else {
                return ResponseEntity.ok(
                        ApiResponse.error("Failed to generate store description"));
            }

        } catch (Exception e) {
            log.error("Error generating store description", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate store description"));
        }
    }

    /**
     * Generate product tags using AI
     * POST /api/ai/tags
     */
    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<Map<String, List<String>>>> generateTags(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser,
            HttpServletRequest httpRequest) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            String category = (String) request.get("category");

            String token = extractJwtToken(httpRequest);

            List<String> tags = aiServiceClient.generateProductTags(name, description, category, token);

            return ResponseEntity.ok(
                    ApiResponse.success(Map.of("tags", tags), "Tags generated successfully"));

        } catch (Exception e) {
            log.error("Error generating tags", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate tags"));
        }
    }

    /**
     * Generate SEO metadata using AI
     * POST /api/ai/seo
     */
    @PostMapping("/seo")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateSeo(
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser,
            HttpServletRequest httpRequest) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            String category = (String) request.get("category");

            String token = extractJwtToken(httpRequest);

            Map<String, String> seoData = aiServiceClient.generateSeoMetadata(name, description, category, token);

            return ResponseEntity.ok(
                    ApiResponse.success(Map.of("seo", seoData), "SEO metadata generated successfully"));

        } catch (Exception e) {
            log.error("Error generating SEO metadata", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to generate SEO metadata"));
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return "";
    }
}
