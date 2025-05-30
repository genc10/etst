package org.perfume.service;

import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.enums.OrderStatus;

import java.util.List;

public interface OrderService {
    CheckoutResponse checkout(Long userId, OrderRequest request);
    OrderResponse getOrder(Long userId, Long orderId);
    PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size);
    OrderResponse updateOrderStatus(Long orderId, OrderStatus status);
    List<OrderResponse> getOrdersByStatus(OrderStatus status);
    void cancelOrder(Long userId, Long orderId);
    List<Object[]> getTopCustomers(int limit);
}