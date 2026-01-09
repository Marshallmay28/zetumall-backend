package com.zetumall.product;

import com.zetumall.product.dto.ProductResponse;
import com.zetumall.security.SupabaseAuthenticatedUser;
import com.zetumall.shared.ApiResponse;
import com.zetumall.user.User;
import com.zetumall.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductInteractionController {

    private final ProductLikeRepository productLikeRepository;
    private final RatingRepository ratingRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Like a product (add to favorites)
     * POST /api/products/{id}/like
     */
    @PostMapping("/{id}/like")
    @Transactional
    public ResponseEntity<ApiResponse<String>> likeProduct(
            @PathVariable String id,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if already liked
            if (productLikeRepository.existsByUserIdAndProductId(user.getId(), id)) {
                return ResponseEntity.ok(ApiResponse.success("Product already in favorites"));
            }

            ProductLike like = new ProductLike();
            like.setUserId(user.getId());
            like.setProductId(id);
            productLikeRepository.save(like);

            return ResponseEntity.ok(ApiResponse.success("Product added to favorites"));

        } catch (Exception e) {
            log.error("Error liking product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to like product"));
        }
    }

    /**
     * Unlike a product (remove from favorites)
     * DELETE /api/products/{id}/like
     */
    @DeleteMapping("/{id}/like")
    @Transactional
    public ResponseEntity<ApiResponse<String>> unlikeProduct(
            @PathVariable String id,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            productLikeRepository.deleteByUserIdAndProductId(user.getId(), id);

            return ResponseEntity.ok(ApiResponse.success("Product removed from favorites"));

        } catch (Exception e) {
            log.error("Error unliking product", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to unlike product"));
        }
    }

    /**
     * Get user's favorite products
     * GET /api/products/favorites
     */
    @GetMapping("/favorites")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFavorites(
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            List<ProductLike> likes = productLikeRepository.findByUserId(user.getId());
            List<String> productIds = likes.stream()
                    .map(ProductLike::getProductId)
                    .collect(Collectors.toList());

            List<Product> products = productRepository.findAllById(productIds);
            List<ProductResponse> responses = products.stream()
                    .map(ProductResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            log.error("Error fetching favorites", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch favorites"));
        }
    }

    /**
     * Add a rating/review to a product
     * POST /api/products/{id}/rating
     */
    @PostMapping("/{id}/rating")
    public ResponseEntity<ApiResponse<Rating>> addRating(
            @PathVariable String id,
            @RequestBody Map<String, Object> request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verify product exists
            productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            Integer ratingValue = (Integer) request.get("rating");
            String review = (String) request.get("review");

            if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Rating must be between 1 and 5"));
            }

            Rating rating = new Rating();
            rating.setUserId(user.getId());
            rating.setProductId(id);
            rating.setRating(ratingValue);
            rating.setReview(review);

            Rating savedRating = ratingRepository.save(rating);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(savedRating, "Rating added successfully"));

        } catch (RuntimeException e) {
            log.error("Error adding rating: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error adding rating", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to add rating"));
        }
    }

    /**
     * Get ratings for a product
     * GET /api/products/{id}/ratings
     */
    @GetMapping("/{id}/ratings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProductRatings(
            @PathVariable String id
    ) {
        try {
            List<Rating> ratings = ratingRepository.findByProductId(id);
            Double averageRating = ratingRepository.getAverageRatingForProduct(id);
            long totalRatings = ratingRepository.countByProductId(id);

            Map<String, Object> response = Map.of(
                "ratings", ratings,
                "averageRating", averageRating != null ? averageRating : 0.0,
                "totalRatings", totalRatings
            );

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            log.error("Error fetching ratings", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch ratings"));
        }
    }
}
