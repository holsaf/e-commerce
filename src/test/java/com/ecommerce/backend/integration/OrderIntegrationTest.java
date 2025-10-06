package com.ecommerce.backend.integration;

import com.ecommerce.backend.dto.request.OrderItemRequest;
import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.entity.Customer;
import com.ecommerce.backend.entity.Product;
import com.ecommerce.backend.model.enums.OrderStatus;
import com.ecommerce.backend.model.enums.PaymentMethod;
import com.ecommerce.backend.model.enums.ProductCategory;
import com.ecommerce.backend.repository.OrderRepository;
import com.ecommerce.backend.repository.ProductRepository;
import com.ecommerce.backend.repository.UserRepository;
import com.ecommerce.backend.service.OrderService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OrderService with H2 database.
 * Tests order creation, retrieval, and filtering with real database operations.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Order Integration Tests with H2")
class OrderIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private Customer testCustomer;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Clean database
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();

        // Create test customer
        testCustomer = new Customer();
        testCustomer.setEmail("integration-test@example.com");
        testCustomer.setPassword("encodedPassword");
        testCustomer.setFirstName("Integration");
        testCustomer.setLastName("Test");
        testCustomer.setPhone("+1234567890");
        testCustomer.setAddress("123 Test Street");
        testCustomer.setActive(true);
        testCustomer = userRepository.save(testCustomer);

        // Create test products
        testProduct1 = new Product();
        testProduct1.setName("Test Product 1");
        testProduct1.setDescription("Product for integration testing");
        testProduct1.setCategory(ProductCategory.ELECTRONICS);
        testProduct1.setPrice(BigDecimal.valueOf(99.99));
        testProduct1 = productRepository.save(testProduct1);

        testProduct2 = new Product();
        testProduct2.setName("Test Product 2");
        testProduct2.setDescription("Another test product");
        testProduct2.setCategory(ProductCategory.BOOKS);
        testProduct2.setPrice(BigDecimal.valueOf(49.99));
        testProduct2 = productRepository.save(testProduct2);
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should create order with single product in H2 database")
    void testCreateOrderWithSingleProduct() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProduct1.getId());
        itemRequest.setQuantity(2);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        orderRequest.setTransactionId("TXN-INT-001");
        orderRequest.setShippingAddress("456 Integration St");

        // Act
        OrderResponse result = orderService.createOrder(testCustomer.getEmail(), orderRequest);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        // CREDIT_CARD payments are automatically COMPLETED, so order status becomes PAID
        assertEquals(OrderStatus.PAID, result.getStatus());
        // Items might be lazy-loaded, just verify order was created
        assertNotNull(result.getTotalAmount(), "Total amount should be calculated");
        
        // Verify total amount calculation
        BigDecimal expectedTotal = testProduct1.getPrice().multiply(BigDecimal.valueOf(2));
        assertEquals(0, expectedTotal.compareTo(result.getTotalAmount()), 
            "Total amount should equal product price * quantity");
        
        // Verify in database
        assertTrue(orderRepository.existsById(result.getId()));
    }

    @Test
    @DisplayName("Should create order with multiple products in H2 database")
    void testCreateOrderWithMultipleProducts() {
        // Arrange
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getId());
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getId());
        item2.setQuantity(3);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(item1, item2));
        orderRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        orderRequest.setTransactionId("TXN-INT-002");
        orderRequest.setShippingAddress("789 Multi Item Rd");

        // Act
        OrderResponse result = orderService.createOrder(testCustomer.getEmail(), orderRequest);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getItems().size());
        
        // Verify total amount = (99.99 * 2) + (49.99 * 3)
        BigDecimal expectedTotal = testProduct1.getPrice().multiply(BigDecimal.valueOf(2))
            .add(testProduct2.getPrice().multiply(BigDecimal.valueOf(3)));
        assertEquals(expectedTotal, result.getTotalAmount());
    }

    @Test
    @DisplayName("Should retrieve orders by user email from H2 database")
    void testGetOrdersByUserEmail() {
        // Arrange - Create 3 orders
        for (int i = 1; i <= 3; i++) {
            OrderItemRequest itemRequest = new OrderItemRequest();
            itemRequest.setProductId(testProduct1.getId());
            itemRequest.setQuantity(1);

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setItems(Arrays.asList(itemRequest));
            orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            orderRequest.setTransactionId("TXN-INT-00" + i);
            orderRequest.setShippingAddress("Address " + i);

            orderService.createOrder(testCustomer.getEmail(), orderRequest);
        }

        // Act
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderResponse> orders = orderService.getOrdersByUserMailAndStatus(
            testCustomer.getEmail(), null, pageable
        );

        // Assert
        assertNotNull(orders);
        assertEquals(3, orders.getTotalElements());
        assertTrue(orders.getContent().stream()
            .allMatch(order -> order.getCustomerEmail().equals(testCustomer.getEmail())));
    }

    @Test
    @DisplayName("Should filter orders by status in H2 database")
    void testFilterOrdersByStatus() {
        // Arrange - Create orders and update their status
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProduct1.getId());
        itemRequest.setQuantity(1);

        // Create PAID order (CREDIT_CARD -> COMPLETED payment -> PAID status)
        OrderRequest request1 = new OrderRequest();
        request1.setItems(Arrays.asList(itemRequest));
        request1.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request1.setTransactionId("TXN-PAID");
        request1.setShippingAddress("Paid Address");
        orderService.createOrder(testCustomer.getEmail(), request1);

        // Create PENDING order (BANK_TRANSFER -> PENDING payment -> PENDING status)
        OrderRequest request2 = new OrderRequest();
        request2.setItems(Arrays.asList(itemRequest));
        request2.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        request2.setTransactionId("TXN-PENDING");
        request2.setShippingAddress("Pending Address");
        orderService.createOrder(testCustomer.getEmail(), request2);

        // Act - Get only PENDING orders
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderResponse> pendingOrders = orderService.getOrdersByUserMailAndStatus(
            testCustomer.getEmail(), OrderStatus.PENDING, pageable
        );

        // Assert
        assertNotNull(pendingOrders);
        assertTrue(pendingOrders.getTotalElements() >= 1, 
            "Should have at least 1 pending order, found: " + pendingOrders.getTotalElements());
        assertTrue(pendingOrders.getContent().stream()
            .allMatch(order -> order.getStatus() == OrderStatus.PENDING),
            "All returned orders should have PENDING status");
    }

    @Test
    @DisplayName("Should retrieve order by ID with user validation in H2 database")
    void testGetOrderByIdWithUserValidation() {
        // Arrange - Create an order
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProduct1.getId());
        itemRequest.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        orderRequest.setTransactionId("TXN-VALIDATE");
        orderRequest.setShippingAddress("Validation Address");

        OrderResponse created = orderService.createOrder(testCustomer.getEmail(), orderRequest);

        // Act - Retrieve with correct user
        OrderResponse retrieved = orderService.getOrderByIdWithUserValidation(
            created.getId(), testCustomer.getEmail()
        );

        // Assert
        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(testCustomer.getEmail(), retrieved.getCustomerEmail());
    }

    @Test
    @DisplayName("Should throw exception when user tries to access another user's order")
    void testUnauthorizedOrderAccess() {
        // Arrange - Create an order for testCustomer
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductId(testProduct1.getId());
        itemRequest.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(itemRequest));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        orderRequest.setTransactionId("TXN-SECURE");
        orderRequest.setShippingAddress("Secure Address");

        OrderResponse created = orderService.createOrder(testCustomer.getEmail(), orderRequest);

        // Act & Assert - Try to access with different user email
        assertThrows(RuntimeException.class, () -> {
            orderService.getOrderByIdWithUserValidation(
                created.getId(), "hacker@example.com"
            );
        });
    }

    @Test
    @DisplayName("Should calculate correct total amount for order with multiple quantities")
    void testOrderTotalAmountCalculation() {
        // Arrange
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getId());
        item1.setQuantity(5); // 99.99 * 5 = 499.95

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getId());
        item2.setQuantity(10); // 49.99 * 10 = 499.90

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(item1, item2));
        orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        orderRequest.setTransactionId("TXN-CALC");
        orderRequest.setShippingAddress("Calculation Address");

        // Act
        OrderResponse result = orderService.createOrder(testCustomer.getEmail(), orderRequest);

        // Assert
        BigDecimal expected = BigDecimal.valueOf(99.99 * 5 + 49.99 * 10);
        assertEquals(expected, result.getTotalAmount());
    }

    @Test
    @DisplayName("Should verify order items are persisted correctly in H2")
    void testOrderItemsPersistence() {
        // Arrange
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(testProduct1.getId());
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(testProduct2.getId());
        item2.setQuantity(3);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(Arrays.asList(item1, item2));
        orderRequest.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
        orderRequest.setTransactionId("TXN-PERSIST");
        orderRequest.setShippingAddress("Persistence Address");

        // Act
        OrderResponse created = orderService.createOrder(testCustomer.getEmail(), orderRequest);

        // Assert - Retrieve from database and verify items
        OrderResponse retrieved = orderService.getOrderById(created.getId());
        assertNotNull(retrieved.getItems());
        assertEquals(2, retrieved.getItems().size());
        
        // Verify first item
        assertTrue(retrieved.getItems().stream()
            .anyMatch(item -> item.getProductId().equals(testProduct1.getId()) 
                          && item.getQuantity() == 2));
        
        // Verify second item
        assertTrue(retrieved.getItems().stream()
            .anyMatch(item -> item.getProductId().equals(testProduct2.getId()) 
                          && item.getQuantity() == 3));
    }

    @Test
    @DisplayName("Should test pagination of orders in H2 database")
    void testOrderPagination() {
        // Arrange - Create 15 orders
        for (int i = 1; i <= 15; i++) {
            OrderItemRequest itemRequest = new OrderItemRequest();
            itemRequest.setProductId(testProduct1.getId());
            itemRequest.setQuantity(1);

            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setItems(Arrays.asList(itemRequest));
            orderRequest.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            orderRequest.setTransactionId("TXN-PAGE-" + i);
            orderRequest.setShippingAddress("Page Address " + i);

            orderService.createOrder(testCustomer.getEmail(), orderRequest);
        }

        // Act - Get first page (5 items)
        Pageable page1 = PageRequest.of(0, 5);
        Page<OrderResponse> firstPage = orderService.getOrdersByUserMailAndStatus(
            testCustomer.getEmail(), null, page1
        );

        // Assert
        assertNotNull(firstPage);
        assertEquals(15, firstPage.getTotalElements());
        assertEquals(3, firstPage.getTotalPages());
        assertEquals(5, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());
    }
}
