package org.perfume.service;

import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    CheckoutResponse createOrder(OrderRequest request);
    OrderResponse getOrderById(Long id);
    List<OrderResponse> getUserOrders();
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long id, OrderStatus status);
    void deleteOrder(Long id);
}