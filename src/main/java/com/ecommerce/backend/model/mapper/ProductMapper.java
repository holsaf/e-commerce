package com.ecommerce.backend.model.mapper;

import com.ecommerce.backend.dto.request.ProductRequest;
import com.ecommerce.backend.dto.response.ProductResponse;
import com.ecommerce.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;


@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "productName", source = "name")
    @Mapping(target = "productPrice", source = "price")
    @Mapping(target = "createdTime", source = "createdAt")
    @Mapping(target = "updatedTime", source = "updatedAt")
    ProductResponse productToProductDto(Product product);

    @Mapping(target = "name", source = "productName")
    @Mapping(target = "price", source = "productPrice")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    Product productRequestToProduct(ProductRequest productRequest);

    @Mapping(target = "name", source = "productName")
    @Mapping(target = "price", source = "productPrice")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateProductFromRequest(ProductRequest productRequest, @MappingTarget Product product);

    @Mapping(target = "name", source = "productName")
    @Mapping(target = "price", source = "productPrice")
    @Mapping(target = "createdAt", source = "createdTime")
    @Mapping(target = "updatedAt", source = "updatedTime")
    Product productDToProduct(ProductResponse productResponse);

}
