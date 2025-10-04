package com.ecommerce.backend.model.mapper;

import com.ecommerce.backend.dto.ProductDto;
import com.ecommerce.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "productPrice", source = "price")
    @Mapping(target = "createdTime", source = "createdAt")
    @Mapping(target = "updatedTime", source = "updatedAt")
    ProductDto productToProductDto(Product product);

    @Mapping(target = "name", source = "productDto.productName")
    @Mapping(target = "price", source = "productDto.productPrice")
    @Mapping(target = "createdAt", source = "productDto.createdTime")
    @Mapping(target = "updatedAt", source = "productDto.updatedTime")
    Product productDToProduct(ProductDto productDto);

}
