package com.ecommerce.backend.service;

import com.ecommerce.backend.data.TestData;
import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.enums.ProductCategory;
import com.ecommerce.backend.model.mapper.ProductMapper;
import com.ecommerce.backend.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Arrange
        ProductRequest request = TestData.createTestProductRequest();
        Product productToSave = TestData.createTestProduct();
        Product savedProduct = TestData.createTestProduct();
        savedProduct.setId(1L);

        when(productMapper.productRequestToProduct(any())).thenReturn(productToSave);
        when(productRepository.save(any())).thenReturn(savedProduct);

        // Act
        String result = productService.createProduct(request);

        // Assert
        assertEquals("1", result);
        verify(productMapper, times(1)).productRequestToProduct(request);
        verify(productRepository, times(1)).save(productToSave);
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void testGetAllProducts_Success() {
        // Arrange
        Product product = TestData.createTestProduct();
        ProductResponse response = TestData.createTestProductResponse();
        List<Product> products = Arrays.asList(product, product);
        Page<Product> productPage = new PageImpl<>(products);
        Pageable pageable = PageRequest.of(0, 10);

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.productToProductDto(any(Product.class))).thenReturn(response);

        // Act
        Page<ProductResponse> result = productService.getAllProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
        verify(productMapper, times(2)).productToProductDto(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void testGetProductById_Success() {
        // Arrange
        Product product = TestData.createTestProduct();
        ProductResponse response = TestData.createTestProductResponse();
        
        when(productRepository.findById(any(Long.class))).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return id.equals(1L) ? Optional.of(product) : Optional.empty();
        });
        when(productMapper.productToProductDto(any())).thenReturn(response);

        // Act
        ProductResponse result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals(BigDecimal.valueOf(99.99), result.getProductPrice());
        verify(productMapper, times(1)).productToProductDto(product);
    }

    @Test
    @DisplayName("Should throw exception when product not found by ID")
    void testGetProductById_NotFound() {
        // Arrange
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.getProductById(999L);
        });
        verify(productMapper, never()).productToProductDto(any());
    }

    @Test
    @DisplayName("Should search products with all filters")
    void testSearchProducts_WithAllFilters() {
        // Arrange
        Product product = TestData.createTestProduct();
        ProductResponse response = TestData.createTestProductResponse();
        String name = "phone";
        ProductCategory category = ProductCategory.ELECTRONICS;
        BigDecimal minPrice = BigDecimal.valueOf(100);
        BigDecimal maxPrice = BigDecimal.valueOf(2000);
        Pageable pageable = PageRequest.of(0, 10);

        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.searchProductsByFilters(any(), any(), any(), any(), any()))
                .thenReturn(productPage);
        when(productMapper.productToProductDto(any(Product.class))).thenReturn(response);

        // Act
        Page<ProductResponse> result = productService.searchProducts(name, category, minPrice, maxPrice, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1))
                .searchProductsByFilters(name, category, minPrice, maxPrice, pageable);
    }

    @Test
    @DisplayName("Should search products with no filters")
    void testSearchProducts_NoFilters() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Product product = TestData.createTestProduct();
        ProductResponse response = TestData.createTestProductResponse();
        List<Product> products = Arrays.asList(product, product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.searchProductsByFilters(null, null, null, null, pageable))
                .thenReturn(productPage);
        when(productMapper.productToProductDto(any(Product.class))).thenReturn(response);

        // Act
        Page<ProductResponse> result = productService.searchProducts(null, null, null, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get best selling products")
    void testGetBestSellingProducts_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Product product = TestData.createTestProduct();
        ProductResponse response = TestData.createTestProductResponse();
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products);

        when(productRepository.findBestSellingProducts(any())).thenReturn(productPage);
        when(productMapper.productToProductDto(any(Product.class))).thenReturn(response);

        // Act
        Page<ProductResponse> result = productService.getBestSellingProducts(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findBestSellingProducts(pageable);
    }

    @Test
    @DisplayName("Should update product successfully")
    void testUpdateProduct_Success() {
        // Arrange
        Long productId = 1L;
        ProductRequest request = TestData.createTestProductRequest();
        Product existingProduct = TestData.createTestProduct();
        existingProduct.setId(productId);
        existingProduct.setName("Old Product");

        when(productRepository.findById(any(Long.class))).thenReturn(Optional.of(existingProduct));
        doNothing().when(productMapper).updateProductFromRequest(any(), any());
        when(productRepository.saveAndFlush(any())).thenReturn(existingProduct);

        // Act
        productService.updateProduct(productId, request);

        // Assert
        verify(productMapper, times(1)).updateProductFromRequest(request, existingProduct);
        verify(productRepository, times(1)).saveAndFlush(existingProduct);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent product")
    void testUpdateProduct_NotFound() {
        // Arrange
        Long productId = 999L;
        ProductRequest request = TestData.createTestProductRequest();
        when(productRepository.findById(any(Long.class))).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(productId, request);
        });
        verify(productMapper, never()).updateProductFromRequest(any(), any());
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Arrange
        Long productId = 1L;
        when(productRepository.existsById(any())).thenReturn(true);
        doNothing().when(productRepository).deleteById(any());

        // Act
        productService.deleteProduct(productId);

        // Assert
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent product")
    void testDeleteProduct_NotFound() {
        // Arrange
        Long productId = 999L;
        when(productRepository.existsById(any())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct(productId);
        });
        verify(productRepository, times(1)).existsById(productId);
        verify(productRepository, never()).deleteById(any());
    }
}
