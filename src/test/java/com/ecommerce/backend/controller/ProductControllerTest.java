package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.model.enums.ProductCategory;
import com.ecommerce.backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController MockMvc Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/products - Should return paginated products")
    void testGetAllProducts() throws Exception {
        // Arrange
        ProductResponse product1 = createProductResponse(1L, "Product 1", new BigDecimal("99.99"));
        ProductResponse product2 = createProductResponse(2L, "Product 2", new BigDecimal("149.99"));
        
        List<ProductResponse> products = Arrays.asList(product1, product2);
        Page<ProductResponse> page = new PageImpl<>(products, PageRequest.of(0, 10), 2);

        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].productName").value("Product 1"))
                .andExpect(jsonPath("$.content[1].productName").value("Product 2"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/products/{id} - Should return product by ID")
    void testGetProductById() throws Exception {
        // Arrange
        ProductResponse product = createProductResponse(1L, "Test Product", new BigDecimal("199.99"));
        when(productService.getProductById(1L)).thenReturn(product);

        // Act & Assert
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.productName").value("Test Product"))
                .andExpect(jsonPath("$.productPrice").value(199.99));
    }

    @Test
    @DisplayName("GET /api/products/search - Should search products by name")
    void testSearchProductsByName() throws Exception {
        // Arrange
        ProductResponse product = createProductResponse(1L, "Laptop Dell", new BigDecimal("999.99"));
        Page<ProductResponse> page = new PageImpl<>(Arrays.asList(product), PageRequest.of(0, 10), 2);

        when(productService.searchProducts(
                any(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("name", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Laptop Dell"))
                .andExpect(jsonPath("$.content[0].productPrice").value(999.99));
    }

    @Test
    @DisplayName("GET /api/products/search - Should search products by category")
    void testSearchProductsByCategory() throws Exception {
        // Arrange
        ProductResponse product = createProductResponse(1L, "Electronics Item", new BigDecimal("299.99"));
        product.setCategory(ProductCategory.ELECTRONICS);
        Page<ProductResponse> page = new PageImpl<>(Arrays.asList(product), PageRequest.of(0, 10), 2);

        when(productService.searchProducts(
                isNull(), eq(ProductCategory.ELECTRONICS), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("category", "ELECTRONICS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].category").value("ELECTRONICS"));
    }

    @Test
    @DisplayName("GET /api/products/search - Should search products by price range")
    void testSearchProductsByPriceRange() throws Exception {
        // Arrange
        ProductResponse product = createProductResponse(1L, "Mid-range Product", new BigDecimal("150.00"));
        Page<ProductResponse> page = new PageImpl<>(Arrays.asList(product), PageRequest.of(0, 10), 2);

        when(productService.searchProducts(
                isNull(), isNull(), eq(new BigDecimal("100")), eq(new BigDecimal("200")), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products/search")
                .param("minPrice", "100")
                .param("maxPrice", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productPrice").value(150.00));
    }

    @Test
    @DisplayName("GET /api/products/best-sellers - Should return best-selling products")
    void testGetBestSellingProducts() throws Exception {
        // Arrange
        ProductResponse product1 = createProductResponse(1L, "Best Seller 1", new BigDecimal("79.99"));
        ProductResponse product2 = createProductResponse(2L, "Best Seller 2", new BigDecimal("89.99"));
        Page<ProductResponse> page = new PageImpl<>(Arrays.asList(product1, product2), PageRequest.of(0, 10), 2);

        when(productService.getBestSellingProducts(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/products/best-sellers")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    @DisplayName("POST /api/products - Should create new product")
    void testCreateProduct() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setProductName("New Product");
        request.setDescription("Description");
        request.setProductPrice(new BigDecimal("299.99"));
        request.setCategory(ProductCategory.ELECTRONICS);

        when(productService.createProduct(any(ProductRequest.class))).thenReturn("123");

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("123"));
    }

    @Test
    @DisplayName("POST /api/products - Should return 400 for invalid product data")
    void testCreateProductWithInvalidData() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setProductName("");
        request.setProductPrice(new BigDecimal("-10"));

        // Act & Assert
        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/products/{id} - Should update product")
    void testUpdateProduct() throws Exception {
        // Arrange
        ProductRequest request = new ProductRequest();
        request.setProductName("Updated Product");
        request.setDescription("Updated Description");
        request.setProductPrice(new BigDecimal("399.99"));
        request.setCategory(ProductCategory.BOOKS);

        // Act & Assert
        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - Should delete product")
    void testDeleteProduct() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    // Helper method
    private ProductResponse createProductResponse(Long id, String name, BigDecimal price) {
        ProductResponse response = new ProductResponse();
        response.setId(id);
        response.setProductName(name);
        response.setDescription("Test description");
        response.setProductPrice(price);
        response.setCategory(ProductCategory.ELECTRONICS);
        return response;
    }
}
