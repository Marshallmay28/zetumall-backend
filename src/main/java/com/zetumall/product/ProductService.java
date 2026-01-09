package com.zetumall.product;

import com.zetumall.product.dto.ProductCreateRequest;
import com.zetumall.store.Store;
import com.zetumall.store.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    /**
     * Create a new product
     */
    @Transactional
    public Product createProduct(ProductCreateRequest request, String userId) {
        // Verify store ownership
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You don't own this store");
        }

        if (!store.getIsActive() || store.getStatus() != Store.StoreStatus.APPROVED) {
            throw new RuntimeException("Store must be active and approved to add products");
        }

        // Build product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setMrp(request.getMrp());
        product.setPrice(request.getPrice());
        product.setImages(request.getImages() != null ? request.getImages() : new String[0]);
        product.setCategory(request.getCategory());
        product.setInStock(request.getInStock());
        product.setStoreId(request.getStoreId());
        product.setStatus(Product.ProductStatus.APPROVED);  // Auto-approve for MVP

        Product savedProduct = productRepository.save(product);
        log.info("Product created: {} for store: {}", savedProduct.getId(), store.getId());
        
        return savedProduct;
    }

    /**
     * Get product by ID
     */
    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    /**
     * Get all products for a store
     */
    public List<Product> getProductsByStore(String storeId) {
        return productRepository.findByStoreId(storeId);
    }

    /**
     * Update product
     */
    @Transactional
    public Product updateProduct(String productId, ProductCreateRequest request, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Verify ownership
        Store store = storeRepository.findById(product.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this product");
        }

        // Update fields
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getMrp() != null) product.setMrp(request.getMrp());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getImages() != null) product.setImages(request.getImages());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getInStock() != null) product.setInStock(request.getInStock());

        return productRepository.save(product);
    }

    /**
     * Delete product
     */
    @Transactional
    public void deleteProduct(String productId, String userId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Verify ownership
        Store store = storeRepository.findById(product.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        if (!store.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this product");
        }

        productRepository.delete(product);
        log.info("Product deleted: {}", productId);
    }

    /**
     * Get all products with filters
     */
    public List<Product> getAllProducts(String category, String search, Double minPrice, Double maxPrice) {
        if (search != null && !search.isEmpty()) {
            return productRepository.searchProducts(search);
        }

        if (category != null && minPrice != null && maxPrice != null) {
            return productRepository.findByCategoryAndPriceRange(category, minPrice, maxPrice);
        }

        if (category != null) {
            return productRepository.findByCategory(category);
        }

        return productRepository.findByStatus(Product.ProductStatus.APPROVED);
    }

    /**
     * Get featured products
     */
    public List<Product> getFeaturedProducts() {
        return productRepository.findFeaturedProducts();
    }

    /**
     * Increment view count
     */
    @Transactional
    public void incrementViewCount(String productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        product.setViewCount(product.getViewCount() + 1);
        productRepository.save(product);
    }
}
