package com.ecommerce.backend.service;

import com.ecommerce.backend.dto.request.OrderItemRequest;
import com.ecommerce.backend.dto.request.OrderRequest;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.entity.*;
import com.ecommerce.backend.exception.NotAuthorizedException;
import com.ecommerce.backend.exception.ResourceNotFoundException;
import com.ecommerce.backend.model.enums.OrderStatus;
import com.ecommerce.backend.model.enums.PaymentMethod;
import com.ecommerce.backend.model.enums.PaymentStatus;
import com.ecommerce.backend.model.mapper.OrderMapper;
import com.ecommerce.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    public OrderResponse createOrder(String userEmail, OrderRequest request) {
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setShippingAddress(request.getShippingAddress() != null 
                ? request.getShippingAddress() 
                : customer.getAddress());

        Set<OrderItem> orderItems = new HashSet<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            BigDecimal itemSubtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            orderItem.setSubtotal(itemSubtotal);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(itemSubtotal);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        Payment payment = new Payment();
        payment.setMethod(request.getPaymentMethod());
        payment.setTransactionId(request.getTransactionId());
        PaymentStatus status = getTransactionStatus(request.getTransactionId(), request.getPaymentMethod());
        payment.setStatus(status);
        payment.setMethod(request.getPaymentMethod());

        order.setPayment(payment);

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            order.setStatus(OrderStatus.PAID);
        }

        Order savedOrder = orderRepository.save(order);

        return orderMapper.orderToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserMailAndStatus(String userEmail, OrderStatus status, Pageable pageable) {

        return orderRepository.findByCustomerEmailWithOptionalStatus(userEmail, status, pageable)
                .map(orderMapper::orderToOrderResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdWithUserValidation(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

        if (!order.getCustomer().getEmail().equals(userEmail)) {
            throw new NotAuthorizedException("Unauthorized access to order: " + orderId);
        }

        return orderMapper.orderToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
        return orderMapper.orderToOrderResponse(order);
    }

    private PaymentStatus getTransactionStatus(String transactionId, PaymentMethod paymentMethod) {
        if (transactionId == null || transactionId.isEmpty()) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        // Simulate checking transaction status from external system
        // For demo purposes, we'll assume for CREDIT_CARD method, the transaction is COMPLETED
        // for BANK_TRANSFER, it is PENDING
        return switch (paymentMethod) {
            case CREDIT_CARD -> PaymentStatus.COMPLETED;
            case BANK_TRANSFER -> PaymentStatus.PENDING;
            default -> throw new IllegalArgumentException("Unsupported payment method");
        };
    }

}
