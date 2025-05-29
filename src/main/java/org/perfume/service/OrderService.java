package org.perfume.service;

import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    CheckoutResponse checkout(Long userId, OrderRequest request);
    OrderResponse getOrder(Long orderId);
    List<OrderResponse> getUserOrders(Long userId);
    List<OrderResponse> getAllOrders();
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
    void cancelOrder(Long orderId);
}