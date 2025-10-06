package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.model.enums.OrderStatus;
import com.ecommerce.backend.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order with multiple products")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {
        OrderResponse order = orderService.createOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/history")
    @Operation(summary = "Get user's order history with optional status filter")
    public ResponseEntity<Page<OrderResponse>> getOrderHistory(
            @RequestParam(required = false) OrderStatus status,
            Authentication authentication,
            Pageable pageable) {
        Page<OrderResponse> orders = orderService.getOrdersByUserMailAndStatus(
                authentication.getName(), 
                status, 
                pageable
        );
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order details by ID")
    public ResponseEntity<OrderResponse> getOrderById(
            @PathVariable Long orderId,
            Authentication authentication) {
        OrderResponse order = orderService.getOrderByIdWithUserValidation(orderId, authentication.getName());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/{orderId}")
    @Operation(summary = "Get order details by ID (Admin only)")
    public ResponseEntity<OrderResponse> getOrderByIdAdmin(
            @PathVariable Long orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
}
