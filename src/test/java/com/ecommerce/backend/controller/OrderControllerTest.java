package com.ecommerce.backend.controller;

import com.ecommerce.backend.dto.request.OrderItemRequest;
import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.model.enums.OrderStatus;
import com.ecommerce.backend.model.enums.PaymentMethod;
import com.ecommerce.backend.service.OrderService;
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
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController MockMvc Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("POST /api/orders - Should create new order")
    void testCreateOrder() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setItems(Arrays.asList(itemRequest));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setTransactionId("TXN-12345");
        request.setShippingAddress("123 Test St");

        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setStatus(OrderStatus.PAID);
        response.setTotalAmount(new BigDecimal("199.98"));
        response.setShippingAddress("123 Test St");

        when(orderService.createOrder(eq("user@test.com"), any(OrderRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.totalAmount").value(199.98))
                .andExpect(jsonPath("$.shippingAddress").value("123 Test St"));
    }

    @Test
    @DisplayName("POST /api/orders - Should return 400 for invalid order data")
    void testCreateOrderWithInvalidData() throws Exception {
        // Arrange
        OrderRequest request = new OrderRequest();
        request.setItems(Arrays.asList());
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/orders/history - Should return user's order history")
    void testGetOrderHistory() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        
        OrderResponse order1 = createOrderResponse(1L, OrderStatus.PAID, new BigDecimal("299.99"));
        OrderResponse order2 = createOrderResponse(2L, OrderStatus.PENDING, new BigDecimal("149.99"));
        
        List<OrderResponse> orders = Arrays.asList(order1, order2);
        Page<OrderResponse> page = new PageImpl<>(orders, PageRequest.of(0, 10), 2);

        when(orderService.getOrdersByUserMailAndStatus(
                eq("user@test.com"), isNull(), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/orders/history")
                .principal(auth)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }


    @Test
    @DisplayName("GET /api/orders/{orderId} - Should return order details")
    void testGetOrderById() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        
        OrderResponse order = createOrderResponse(1L, OrderStatus.PAID, new BigDecimal("499.99"));
        
        when(orderService.getOrderByIdWithUserValidation(1L, "user@test.com"))
                .thenReturn(order);

        // Act & Assert
        mockMvc.perform(get("/api/orders/1")
                .principal(auth))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.totalAmount").value(499.99));
    }

    @Test
    @DisplayName("GET /api/orders/admin/{orderId} - Should return order details for admin")
    void testGetOrderByIdAdmin() throws Exception {
        // Arrange
        OrderResponse order = createOrderResponse(1L, OrderStatus.SHIPPED, new BigDecimal("699.99"));
        
        when(orderService.getOrderById(1L)).thenReturn(order);

        // Act & Assert
        mockMvc.perform(get("/api/orders/admin/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @DisplayName("POST /api/orders - Should handle multiple items")
    void testCreateOrderWithMultipleItems() throws Exception {
        // Arrange
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        OrderRequest request = new OrderRequest();
        request.setItems(Arrays.asList(item1, item2));
        request.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request.setTransactionId("TXN-MULTI-001");
        request.setShippingAddress("456 Multi St");

        OrderResponse response = new OrderResponse();
        response.setId(2L);
        response.setStatus(OrderStatus.PENDING);
        response.setTotalAmount(new BigDecimal("349.97"));

        when(orderService.createOrder(eq("user@test.com"), any(OrderRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                .principal(auth)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(349.97));
    }

    // Helper method
    private OrderResponse createOrderResponse(Long id, OrderStatus status, BigDecimal totalAmount) {
        OrderResponse response = new OrderResponse();
        response.setId(id);
        response.setStatus(status);
        response.setTotalAmount(totalAmount);
        response.setShippingAddress("Test Address");
        return response;
    }
}
