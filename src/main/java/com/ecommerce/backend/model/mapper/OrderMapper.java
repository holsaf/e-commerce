package com.ecommerce.backend.model.mapper;

import com.ecommerce.backend.entity.Payment;
import org.mapstruct.Mapper;

import com.ecommerce.backend.dto.response.OrderItemResponse;
import com.ecommerce.backend.dto.response.OrderResponse;
import com.ecommerce.backend.dto.response.PaymentResponse;
import com.ecommerce.backend.entity.Order;
import com.ecommerce.backend.entity.OrderItem;

@Mapper(componentModel = "spring")
public abstract class OrderMapper {

    public OrderResponse orderToOrderResponse(Order order){
        if ( order == null ) {
            return null;
        }

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerId(order.getCustomer().getId());
        response.setCustomerName(order.getCustomer().getFullName());
        response.setCustomerEmail(order.getCustomer().getEmail());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setShippingAddress(order.getShippingAddress());
        response.setCreatedAt(order.getCreatedAt());

        response.setItems(order.getItems().stream()
                .map(this::mapToOrderItemResponse)
                .toList());

        if (order.getPayment() != null) {
            response.setPayment(mapToPaymentResponse(order.getPayment()));
        }

        return response;
        
    }

    public OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        if (item == null) {
            return null;
        }

        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getName());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getProduct().getPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }

    public PaymentResponse mapToPaymentResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        return response;
    }

}
