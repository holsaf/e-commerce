package com.ecommerce.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ecommerce.backend.model.enums.ProductCategory;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String productName;
    private String description;
    private ProductCategory category;
    private BigDecimal productPrice;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
