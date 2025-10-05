package com.ecommerce.backend.dto.request;

import com.ecommerce.backend.model.enums.ProductCategory;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name can be at most 100 characters")
    private String productName;

    @Size(max = 500, message = "Description can be at most 500 characters")
    private String description;

    @NotNull(message = "Product category is required")
    private ProductCategory category;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Product price must be at least 0.01")
    @DecimalMax(value = "1000000.00", message = "Product price must not exceed 1000000.00")
    private BigDecimal productPrice;

}
