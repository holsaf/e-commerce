package com.ecommerce.backend.integration;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.model.enums.ProductCategory;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProductService with H2 database.
 * Tests the full stack: Service -> Repository -> H2 Database
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Product Integration Tests with H2")
class ProductIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        productRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create and retrieve product from H2 database")
    void testCreateAndRetrieveProduct() {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setProductName("Integration Test Product");
        request.setDescription("Testing H2 database integration");
        request.setCategory(ProductCategory.ELECTRONICS);
        request.setProductPrice(BigDecimal.valueOf(299.99));

        // Act - Create product
        String productId = productService.createProduct(request);
        
        // Assert - Verify product was created
        assertNotNull(productId);
        
        // Act - Retrieve product
        ProductResponse retrieved = productService.getProductById(Long.parseLong(productId));
        
        // Assert - Verify product details
        assertNotNull(retrieved);
        assertEquals("Integration Test Product", retrieved.getProductName());
        assertEquals("Testing H2 database integration", retrieved.getDescription());
        assertEquals(ProductCategory.ELECTRONICS, retrieved.getCategory());
        assertEquals(BigDecimal.valueOf(299.99), retrieved.getProductPrice());
        assertNotNull(retrieved.getCreatedTime());
        assertNotNull(retrieved.getUpdatedTime());
    }

    @Test
    @DisplayName("Should create multiple products and retrieve all with pagination")
    void testCreateMultipleProductsAndRetrieveAll() {
        // Arrange - Create 5 products
        for (int i = 1; i <= 5; i++) {
            ProductRequest request = new ProductRequest();
            request.setProductName("Product " + i);
            request.setDescription("Description " + i);
            request.setCategory(ProductCategory.ELECTRONICS);
            request.setProductPrice(BigDecimal.valueOf(100.00 * i));
            productService.createProduct(request);
        }

        // Act - Retrieve all products with pagination
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponse> products = productService.getAllProducts(pageable);

        // Assert - Verify all products were retrieved
        assertNotNull(products);
        assertEquals(5, products.getTotalElements());
        assertEquals(5, products.getContent().size());
    }

    @Test
    @DisplayName("Should update product in H2 database")
    void testUpdateProduct() {
        // Arrange - Create a product first
        ProductRequest createRequest = new ProductRequest();
        createRequest.setProductName("Original Product");
        createRequest.setDescription("Original Description");
        createRequest.setCategory(ProductCategory.ELECTRONICS);
        createRequest.setProductPrice(BigDecimal.valueOf(199.99));
        
        String productId = productService.createProduct(createRequest);

        // Act - Update the product
        ProductRequest updateRequest = new ProductRequest();
        updateRequest.setProductName("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setCategory(ProductCategory.BOOKS);
        updateRequest.setProductPrice(BigDecimal.valueOf(299.99));
        
        productService.updateProduct(Long.parseLong(productId), updateRequest);

        // Assert - Verify product was updated
        ProductResponse updated = productService.getProductById(Long.parseLong(productId));
        assertEquals("Updated Product", updated.getProductName());
        assertEquals("Updated Description", updated.getDescription());
        assertEquals(ProductCategory.BOOKS, updated.getCategory());
        assertEquals(BigDecimal.valueOf(299.99), updated.getProductPrice());
    }

    @Test
    @DisplayName("Should delete product from H2 database")
    void testDeleteProduct() {
        // Arrange - Create a product first
        ProductRequest request = new ProductRequest();
        request.setProductName("Product to Delete");
        request.setDescription("This will be deleted");
        request.setCategory(ProductCategory.ELECTRONICS);
        request.setProductPrice(BigDecimal.valueOf(99.99));
        
        String productId = productService.createProduct(request);
        
        // Verify product exists
        assertTrue(productRepository.existsById(Long.parseLong(productId)));

        // Act - Delete the product
        productService.deleteProduct(Long.parseLong(productId));

        // Assert - Verify product was deleted
        assertFalse(productRepository.existsById(Long.parseLong(productId)));
    }

    @Test
    @DisplayName("Should search products by category in H2 database")
    void testSearchProductsByCategory() {
        // Arrange - Create products in different categories
        ProductRequest electronics1 = new ProductRequest();
        electronics1.setProductName("Laptop");
        electronics1.setDescription("Gaming laptop");
        electronics1.setCategory(ProductCategory.ELECTRONICS);
        electronics1.setProductPrice(BigDecimal.valueOf(1500.00));
        productService.createProduct(electronics1);

        ProductRequest electronics2 = new ProductRequest();
        electronics2.setProductName("Phone");
        electronics2.setDescription("Smartphone");
        electronics2.setCategory(ProductCategory.ELECTRONICS);
        electronics2.setProductPrice(BigDecimal.valueOf(800.00));
        productService.createProduct(electronics2);

        ProductRequest book = new ProductRequest();
        book.setProductName("Java Book");
        book.setDescription("Learn Java");
        book.setCategory(ProductCategory.BOOKS);
        book.setProductPrice(BigDecimal.valueOf(50.00));
        productService.createProduct(book);

        // Act - Search for electronics
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponse> electronics = productService.searchProducts(
            null, ProductCategory.ELECTRONICS, null, null, pageable
        );

        // Assert - Verify only electronics were retrieved
        assertNotNull(electronics);
        assertEquals(2, electronics.getTotalElements());
        assertTrue(electronics.getContent().stream()
            .allMatch(p -> p.getCategory() == ProductCategory.ELECTRONICS));
    }

    @Test
    @DisplayName("Should search products by price range in H2 database")
    void testSearchProductsByPriceRange() {
        // Arrange - Create products with different prices
        for (int i = 1; i <= 5; i++) {
            ProductRequest request = new ProductRequest();
            request.setProductName("Product " + i);
            request.setDescription("Description " + i);
            request.setCategory(ProductCategory.ELECTRONICS);
            request.setProductPrice(BigDecimal.valueOf(100.00 * i)); // 100, 200, 300, 400, 500
            productService.createProduct(request);
        }

        // Act - Search for products between 200 and 400
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponse> products = productService.searchProducts(
            null, null, BigDecimal.valueOf(200), BigDecimal.valueOf(400), pageable
        );

        // Assert - Verify only products in price range were retrieved
        assertNotNull(products);
        assertEquals(3, products.getTotalElements()); // 200, 300, 400
        assertTrue(products.getContent().stream()
            .allMatch(p -> p.getProductPrice().compareTo(BigDecimal.valueOf(200)) >= 0 
                       && p.getProductPrice().compareTo(BigDecimal.valueOf(400)) <= 0));
    }

    @Test
    @DisplayName("Should search products by name in H2 database")
    void testSearchProductsByName() {
        // Arrange - Create products with different names
        ProductRequest laptop = new ProductRequest();
        laptop.setProductName("Gaming Laptop");
        laptop.setDescription("High-end laptop");
        laptop.setCategory(ProductCategory.ELECTRONICS);
        laptop.setProductPrice(BigDecimal.valueOf(1500.00));
        productService.createProduct(laptop);

        ProductRequest phone = new ProductRequest();
        phone.setProductName("Smartphone");
        phone.setDescription("Latest phone");
        phone.setCategory(ProductCategory.ELECTRONICS);
        phone.setProductPrice(BigDecimal.valueOf(800.00));
        productService.createProduct(phone);

        // Act - Search for "laptop"
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponse> results = productService.searchProducts(
            "laptop", null, null, null, pageable
        );

        // Assert - Verify only laptop was found
        assertNotNull(results);
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().get(0).getProductName().toLowerCase().contains("laptop"));
    }

    @Test
    @DisplayName("Should verify database constraints and validations")
    void testDatabaseConstraints() {
        // Arrange - Create a valid product
        ProductRequest request = new ProductRequest();
        request.setProductName("Test Product");
        request.setDescription("Test Description");
        request.setCategory(ProductCategory.ELECTRONICS);
        request.setProductPrice(BigDecimal.valueOf(99.99));

        // Act - Create product
        String productId = productService.createProduct(request);

        // Assert - Verify product has proper timestamps
        Product product = productRepository.findById(Long.parseLong(productId))
            .orElseThrow();
        
        assertNotNull(product.getCreatedAt(), "Created timestamp should not be null");
        assertNotNull(product.getUpdatedAt(), "Updated timestamp should not be null");
        // Timestamps should be within 1 second of each other on creation
        long diffInSeconds = Math.abs(java.time.Duration.between(
            product.getCreatedAt(), product.getUpdatedAt()).getSeconds());
        assertTrue(diffInSeconds <= 1, 
            "Created and updated timestamps should be within 1 second on creation, diff was: " + diffInSeconds);
    }

    @Test
    @DisplayName("Should test pagination with multiple pages")
    void testPaginationWithMultiplePages() {
        // Arrange - Create 15 products
        for (int i = 1; i <= 15; i++) {
            ProductRequest request = new ProductRequest();
            request.setProductName("Product " + i);
            request.setDescription("Description " + i);
            request.setCategory(ProductCategory.ELECTRONICS);
            request.setProductPrice(BigDecimal.valueOf(100.00));
            productService.createProduct(request);
        }

        // Act - Get first page (5 items)
        Pageable page1 = PageRequest.of(0, 5);
        Page<ProductResponse> firstPage = productService.getAllProducts(page1);

        // Assert - Verify first page
        assertNotNull(firstPage);
        assertEquals(15, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        assertEquals(5, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());

        // Act - Get second page
        Pageable page2 = PageRequest.of(1, 5);
        Page<ProductResponse> secondPage = productService.getAllProducts(page2);

        // Assert - Verify second page
        assertEquals(5, secondPage.getContent().size());
        assertTrue(secondPage.hasPrevious());
        assertTrue(secondPage.hasNext());

        // Act - Get third page
        Pageable page3 = PageRequest.of(2, 5);
        Page<ProductResponse> thirdPage = productService.getAllProducts(page3);

        // Assert - Verify third page
        assertEquals(5, thirdPage.getContent().size());
        assertTrue(thirdPage.hasPrevious());
        assertFalse(thirdPage.hasNext());
    }
}
