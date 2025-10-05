package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.enums.ProductCategory;
import com.ecommerce.backend.model.mapper.ProductMapper;
import com.ecommerce.backend.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper ;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productMapper = productMapper;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(productMapper::productToProductDto);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.productToProductDto(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String name, ProductCategory category,
                                                BigDecimal minPrice, BigDecimal maxPrice,
                                                Pageable pageable) {
        return productRepository.searchProductsByFilters(name, category, minPrice, maxPrice, pageable)
                .map(productMapper::productToProductDto);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getBestSellingProducts(Pageable pageable) {
        return productRepository.findBestSellingProducts(pageable)
                .map(productMapper::productToProductDto);
    }

    @Transactional
    public String createProduct(ProductRequest request) {
        Product product = productMapper.productRequestToProduct(request);
        Product newProduct = productRepository.save(product);
        return newProduct.getId().toString();
    }

    @Transactional
    public void updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        productMapper.updateProductFromRequest(request, product);
        productRepository.saveAndFlush(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

}
