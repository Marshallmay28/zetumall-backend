package com.zetumall.product;

import com.zetumall.product.dto.ProductCreateRequest;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;
    private final UserRepository userRepository;

    /**
     * Create a new product
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productService.createProduct(request, user.getId());
            ProductResponse response = ProductResponse.fromEntity(product);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(response, "Product created successfully"));

        } catch (RuntimeException e) {
            log.error("Product creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during product creation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create product"));
        }
    }

    /**
     * Get all products with optional filters
     * GET /api/products?category=...&search=...&minPrice=...&maxPrice=...
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
    ) {
        try {
            List<Product> products = productService.getAllProducts(category, search, minPrice, maxPrice);
            List<ProductResponse> responses = products.stream()
                    .map(ProductResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            log.error("Error fetching products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch products"));
        }
    }

    /**
     * Get featured products
     * GET /api/products/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFeaturedProducts() {
        try {
            List<Product> products = productService.getFeaturedProducts();
            List<ProductResponse> responses = products.stream()
                    .map(ProductResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            log.error("Error fetching featured products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch featured products"));
        }
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable String id) {
        try {
            Product product = productService.getProductById(id);
            
            // Increment view count
            productService.incrementViewCount(id);
            
            ProductResponse response = ProductResponse.fromEntity(product);
            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Product not found"));
        }
    }

    /**
     * Get products by store
     * GET /api/products/store/{storeId}
     */
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByStore(
            @PathVariable String storeId
    ) {
        try {
            List<Product> products = productService.getProductsByStore(storeId);
            List<ProductResponse> responses = products.stream()
                    .map(ProductResponse::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(responses));

        } catch (Exception e) {
            log.error("Error fetching store products", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch store products"));
        }
    }

    /**
     * Update product
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable String id,
            @RequestBody ProductCreateRequest request,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Product product = productService.updateProduct(id, request, user.getId());
            ProductResponse response = ProductResponse.fromEntity(product);

            return ResponseEntity.ok(ApiResponse.success(response, "Product updated successfully"));

        } catch (RuntimeException e) {
            log.error("Product update failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during product update", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update product"));
        }
    }

    /**
     * Delete product
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable String id,
            @AuthenticationPrincipal SupabaseAuthenticatedUser authUser
    ) {
        try {
            User user = userRepository.findByAuthId(authUser.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            productService.deleteProduct(id, user.getId());

            return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));

        } catch (RuntimeException e) {
            log.error("Product deletion failed: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during product deletion", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete product"));
        }
    }
}
