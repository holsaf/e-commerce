package com.ecommerce.backend.service;

import com.ecommerce.backend.data.TestData;
import com.ecommerce.backend.dto.request.OrderItemRequest;
import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.model.enums.OrderStatus;
import com.ecommerce.backend.model.enums.PaymentMethod;
import com.ecommerce.backend.model.mapper.OrderMapper;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
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
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Should create order successfully with single product")
    void testCreateOrder_SingleProduct_Success() {
        // Arrange
        Customer customer = TestData.createTestCustomer();
        Product product = TestData.createTestProduct();
        Order order = TestData.createTestOrder();
        String userEmail = customer.getEmail();
        
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(1L);
        itemRequest.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemRequest));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        orderRequest.setTransactionId("TXN-123");
        orderRequest.setShippingAddress("456 Shipping St");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(customer));
        when(productRepository.findById(any())).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        OrderResponse response = TestData.createTestOrderResponse();
        when(orderMapper.orderToOrderResponse(any(Order.class))).thenReturn(response);

        // Act
        OrderResponse result = orderService.createOrder(userEmail, orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(productRepository, times(1)).findById(any());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    @DisplayName("Should create order with multiple products")
    void testCreateOrder_MultipleProducts_Success() {
        // Arrange
        Customer customer = TestData.createTestCustomer();
        Product product1 = TestData.createTestProduct();
        Product product2 = TestData.createTestProduct(2L, "Product 2", BigDecimal.valueOf(49.99));
        Order order = TestData.createTestOrder();
        OrderResponse response = TestData.createTestOrderResponse();
        String userEmail = customer.getEmail();
        
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(1L);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(2L);
        item2.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(item1, item2));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        orderRequest.setTransactionId("TXN-456");

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(customer));
        when(productRepository.findById(any())).thenReturn(Optional.of(product1));
        when(productRepository.findById(any())).thenReturn(Optional.of(product2));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.orderToOrderResponse(any(Order.class))).thenReturn(response);

        // Act
        OrderResponse result = orderService.createOrder(userEmail, orderRequest);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(2)).findById(any());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void testCreateOrder_UserNotFound() {
        // Arrange
        String userEmail = "nonexistent@test.com";
        OrderRequest orderRequest = new OrderRequest();

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(userEmail, orderRequest);
        });
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void testCreateOrder_ProductNotFound() {
        // Arrange
        Customer customer = TestData.createTestCustomer();
        String userEmail = customer.getEmail();
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(999L);
        itemRequest.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(customer));
        when(productRepository.findById(any())).thenReturn(null);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.createOrder(userEmail, orderRequest);
        });
    }

    @Test
    @DisplayName("Should get orders by user email and status")
    void testGetOrdersByUserMailAndStatus_Success() {
        // Arrange
        String userEmail = "customer@test.com";
        OrderStatus status = OrderStatus.PAID;
        Pageable pageable = PageRequest.of(0, 10);
        Order order = TestData.createTestOrder();
        OrderResponse response = TestData.createTestOrderResponse();

        List<Order> orders = Arrays.asList(order);
        Page<Order> orderPage = new PageImpl<>(orders);

        when(orderRepository.findByCustomerEmailWithOptionalStatus(any(), any(), any()))
                .thenReturn(orderPage);
        when(orderMapper.orderToOrderResponse(any(Order.class))).thenReturn(response);

        // Act
        Page<OrderResponse> result = orderService.getOrdersByUserMailAndStatus(userEmail, status, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1))
                .findByCustomerEmailWithOptionalStatus(userEmail, status, pageable);
    }

    @Test
    @DisplayName("Should get all orders when status is null")
    void testGetOrdersByUserMailAndStatus_NoStatusFilter() {
        // Arrange
        String userEmail = "test@example.com";
        Pageable pageable = PageRequest.of(0, 10);
        Order order = TestData.createTestOrder();
        OrderResponse response = TestData.createTestOrderResponse();

        List<Order> orders = Arrays.asList(order, order);
        Page<Order> orderPage = new PageImpl<>(orders);

        when(orderRepository.findByCustomerEmailWithOptionalStatus(any(), isNull(), any()))
                .thenReturn(orderPage);
        when(orderMapper.orderToOrderResponse(any(Order.class))).thenReturn(response);

        // Act
        Page<OrderResponse> result = orderService.getOrdersByUserMailAndStatus(userEmail, null, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
    }

    @Test
    @DisplayName("Should get order by ID with user validation")
    void testGetOrderByIdWithUserValidation_Success() {
        // Arrange
        Long orderId = 1L;
        String userEmail = "test@example.com";
        Order order = TestData.createTestOrder();
        OrderResponse response = TestData.createTestOrderResponse();

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderResponse(any())).thenReturn(response);

        // Act
        OrderResponse result = orderService.getOrderByIdWithUserValidation(orderId, userEmail);

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should throw exception when order not found by ID")
    void testGetOrderByIdWithUserValidation_OrderNotFound() {
        // Arrange
        Long orderId = 999L;
        String userEmail = "test@example.com";

        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByIdWithUserValidation(orderId, userEmail);
        });
    }

    @Test
    @DisplayName("Should throw exception when user tries to access other user's order")
    void testGetOrderByIdWithUserValidation_UnauthorizedAccess() {
        // Arrange
        Long orderId = 1L;
        String wrongUserEmail = "hacker@test.com";
        Order order = TestData.createTestOrder();

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByIdWithUserValidation(orderId, wrongUserEmail);
        });
    }

    @Test
    @DisplayName("Should get order by ID (admin access)")
    void testGetOrderById_Success() {
        // Arrange
        Long orderId = 1L;
        Order order = TestData.createTestOrder();
        OrderResponse response = TestData.createTestOrderResponse();

        when(orderRepository.findById(any())).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderResponse(any())).thenReturn(response);

        // Act
        OrderResponse result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    @DisplayName("Should throw exception when admin tries to get non-existent order")
    void testGetOrderById_NotFound() {
        // Arrange
        Long orderId = 999L;

        when(orderRepository.findById(any())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderById(orderId);
        });
    }
}
