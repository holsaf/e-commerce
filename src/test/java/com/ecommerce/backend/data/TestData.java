package com.ecommerce.backend.data;

import com.ecommerce.backend.dto.request.*;
import com.ecommerce.backend.dto.response.*;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Utility class for creating test data objects.
 * Contains static factory methods for entities, DTOs, and test scenarios.
 */
public class TestData {

    // ==================== PRODUCT TEST DATA ====================
    
    public static Product createTestProduct() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
    
    public static Product createTestProduct(Long id, String name, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Test Description for " + name);
        product.setCategory(ProductCategory.ELECTRONICS);
        product.setPrice(price);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return product;
    }
    
    public static Product createTestProductWithCategory(ProductCategory category) {
        Product product = createTestProduct();
        product.setCategory(category);
        return product;
    }
    
    public static ProductRequest createTestProductRequest() {
        ProductRequest request = new ProductRequest();
        request.setProductName("Test Product");
        request.setDescription("Test Description");
        request.setCategory(ProductCategory.ELECTRONICS);
        request.setProductPrice(BigDecimal.valueOf(99.99));
        return request;
    }
    
    public static ProductRequest createTestProductRequest(String name, BigDecimal price, ProductCategory category) {
        ProductRequest request = new ProductRequest();
        request.setProductName(name);
        request.setDescription("Test Description for " + name);
        request.setCategory(category);
        request.setProductPrice(price);
        return request;
    }
    
    public static ProductResponse createTestProductResponse() {
        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setProductName("Test Product");
        response.setDescription("Test Description");
        response.setCategory(ProductCategory.ELECTRONICS);
        response.setProductPrice(BigDecimal.valueOf(99.99));
        response.setCreatedTime(LocalDateTime.now());
        response.setUpdatedTime(LocalDateTime.now());
        return response;
    }
    
    public static ProductResponse createTestProductResponse(Long id, String name) {
        ProductResponse response = createTestProductResponse();
        response.setId(id);
        response.setProductName(name);
        return response;
    }

    // ==================== USER TEST DATA ====================
    
    public static Customer createTestCustomer() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setRole(Role.CUSTOMER);
        customer.setEmail("test@example.com");
        customer.setPassword("encodedPassword");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setPhone("+1234567890");
        customer.setAddress("123 Main St");
        customer.setActive(true);
        return customer;
    }
    
    public static Customer createTestCustomer(String email, String firstName, String lastName) {
        Customer customer = createTestCustomer();
        customer.setEmail(email);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        return customer;
    }
    
    public static Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setId(2L);
        admin.setEmail("test@example.com");
        admin.setPassword("encodedPassword");
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setPhone("+1234567890");
        admin.setAddress("Admin Street");
        admin.setActive(true);
        return admin;
    }
    
    public static Admin createTestAdmin(String email) {
        Admin admin = createTestAdmin();
        admin.setEmail(email);
        return admin;
    }
    
    public static UserRequest createTestUserRequest() {
        UserRequest request = new UserRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("+1234567890");
        request.setAddress("123 Main St");
        return request;
    }
    
    public static UserRequest createTestUserRequest(String email, String password) {
        UserRequest request = createTestUserRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
    
    public static UserUpdateRequest createTestUserUpdateRequest() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        request.setPhone("+9876543210");
        request.setAddress("456 New St");
        return request;
    }
    
    public static UserResponse createTestUserResponse() {
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setEmail("test@example.com");
        response.setFirstName("John");
        response.setLastName("Doe");
        response.setPhone("+1234567890");
        response.setAddress("123 Main St");
        return response;
    }
    
    public static UserResponse createTestUserResponse(Long id, String email) {
        UserResponse response = createTestUserResponse();
        response.setId(id);
        response.setEmail(email);
        return response;
    }

    // ==================== AUTH TEST DATA ====================
    
    public static AuthRequest createTestAuthRequest() {
        AuthRequest request = new AuthRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        return request;
    }
    
    public static AuthRequest createTestAuthRequest(String email, String password) {
        AuthRequest request = new AuthRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
    
    public static AuthResponse createTestAuthResponse() {
        AuthResponse response = new AuthResponse();
        response.setToken("jwt-token-12345");
        response.setEmail("test@example.com");
        response.setRole(Role.CUSTOMER);
        return response;
    }
    
    public static AuthResponse createTestAuthResponse(String token, String email, Role role) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(email);
        response.setRole(role);
        return response;
    }

    // ==================== ORDER TEST DATA ====================
    
    public static Order createTestOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setCustomer(createTestCustomer());
        order.setTotalAmount(BigDecimal.valueOf(199.98));
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress("123 Main St");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        return order;
    }
    
    public static Order createTestOrder(User customer, BigDecimal totalAmount, OrderStatus status) {
        Order order = createTestOrder();
        order.setCustomer(customer);
        order.setTotalAmount(totalAmount);
        order.setStatus(status);
        return order;
    }
    
    public static OrderItem createTestOrderItem() {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(1L);
        orderItem.setProduct(createTestProduct());
        orderItem.setQuantity(2);
        orderItem.setSubtotal(BigDecimal.valueOf(199.98));
        return orderItem;
    }
    
    public static OrderItem createTestOrderItem(Product product, Integer quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setSubtotal(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        return orderItem;
    }
    
    public static OrderItemRequest createTestOrderItemRequest() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);
        return request;
    }
    
    public static OrderItemRequest createTestOrderItemRequest(Long productId, Integer quantity) {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(productId);
        request.setQuantity(quantity);
        return request;
    }
    
    public static OrderRequest createTestOrderRequest() {
        OrderRequest request = new OrderRequest();
        request.setItems(Arrays.asList(createTestOrderItemRequest()));
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setTransactionId("TXN-123");
        request.setShippingAddress("456 Shipping St");
        return request;
    }
    
    public static OrderRequest createTestOrderRequest(List<OrderItemRequest> items, PaymentMethod paymentMethod) {
        OrderRequest request = new OrderRequest();
        request.setItems(items);
        request.setPaymentMethod(paymentMethod);
        request.setTransactionId("TXN-" + System.currentTimeMillis());
        request.setShippingAddress("Test Shipping Address");
        return request;
    }
    
    public static OrderResponse createTestOrderResponse() {
        OrderResponse response = new OrderResponse();
        response.setId(1L);
        response.setTotalAmount(BigDecimal.valueOf(199.98));
        response.setStatus(OrderStatus.PENDING);
        response.setShippingAddress("123 Main St");
        response.setCreatedAt(LocalDateTime.now());
        return response;
    }
    
    public static OrderResponse createTestOrderResponse(Long id, OrderStatus status) {
        OrderResponse response = createTestOrderResponse();
        response.setId(id);
        response.setStatus(status);
        return response;
    }

    // ==================== PAYMENT TEST DATA ====================
    
    public static Payment createTestPayment() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setMethod(PaymentMethod.CREDIT_CARD);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionId("TXN-123456");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        return payment;
    }
    
    public static Payment createTestPayment(PaymentMethod method, PaymentStatus status) {
        Payment payment = createTestPayment();
        payment.setMethod(method);
        payment.setStatus(status);
        return payment;
    }
    
    public static PaymentResponse createTestPaymentResponse() {
        PaymentResponse response = new PaymentResponse();
        response.setId(1L);
        response.setMethod(PaymentMethod.CREDIT_CARD);
        response.setStatus(PaymentStatus.COMPLETED);
        response.setTransactionId("TXN-123456");
        return response;
    }
    
    public static PaymentResponse createTestPaymentResponse(PaymentMethod method, PaymentStatus status) {
        PaymentResponse response = createTestPaymentResponse();
        response.setMethod(method);
        response.setStatus(status);
        return response;
    }

    // ==================== UTILITY METHODS ====================
    
    /**
     * Creates a list of test products for pagination tests
     */
    public static List<Product> createTestProductList(int count) {
        return Arrays.asList(
            createTestProduct(1L, "Product 1", BigDecimal.valueOf(10.00)),
            createTestProduct(2L, "Product 2", BigDecimal.valueOf(20.00)),
            createTestProduct(3L, "Product 3", BigDecimal.valueOf(30.00))
        ).subList(0, Math.min(count, 3));
    }
    
    /**
     * Creates a list of test users for pagination tests
     */
    public static List<User> createTestUserList(int count) {
        Customer user1 = createTestCustomer("user1@test.com", "User", "One");
        Customer user2 = createTestCustomer("user2@test.com", "User", "Two");
        Customer user3 = createTestCustomer("user3@test.com", "User", "Three");
        
        return Arrays.<User>asList(user1, user2, user3).subList(0, Math.min(count, 3));
    }
    
    /**
     * Creates a list of test orders
     */
    public static List<Order> createTestOrderList(int count) {
        return Arrays.asList(
            createTestOrder(createTestCustomer(), BigDecimal.valueOf(100), OrderStatus.PENDING),
            createTestOrder(createTestCustomer(), BigDecimal.valueOf(200), OrderStatus.PAID),
            createTestOrder(createTestCustomer(), BigDecimal.valueOf(300), OrderStatus.SHIPPED)
        ).subList(0, Math.min(count, 3));
    }
}

