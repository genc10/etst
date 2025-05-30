package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.*;
import org.perfume.domain.repo.*;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.OrderMapper;
import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.dto.response.PageResponse;
import org.perfume.model.enums.OrderStatus;
import org.perfume.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;
    private final UserDao userDao;
    private final CartDao cartDao;
    private final CartItemDao cartItemDao;
    private final OrderMapper orderMapper;

    @Override
    public CheckoutResponse checkout(Long userId, OrderRequest request) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Cart cart = cartDao.findUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new InvalidInputException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(user);
        order.setWhatsappNumber(request.getWhatsappNumber());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setCustomerNotes(request.getCustomerNotes());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        StringBuilder whatsappMessage = new StringBuilder("New Order:\\n");

        for (CartItem cartItem : cart.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setPerfume(cartItem.getPerfume());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPerfume().getDiscountedPrice());
            orderItem.setProductName(cartItem.getPerfume().getName());
            orderItem.setBrandName(cartItem.getPerfume().getBrand().getName());

            order.getItems().add(orderItem);
            totalAmount = totalAmount.add(orderItem.getSubtotal());

            whatsappMessage.append(String.format("%s x%d - %s AZN\\n", 
                orderItem.getProductName(), 
                orderItem.getQuantity(),
                orderItem.getSubtotal()));
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderDao.save(order);

        // Clear the cart
        cartItemDao.deleteByCartId(cart.getId());

        whatsappMessage.append(String.format("\\nTotal: %s AZN\\n", totalAmount));
        whatsappMessage.append(String.format("Delivery Address: %s\\n", request.getDeliveryAddress()));
        if (request.getCustomerNotes() != null && !request.getCustomerNotes().isEmpty()) {
            whatsappMessage.append(String.format("Notes: %s\\n", request.getCustomerNotes()));
        }

        String encodedMessage = URLEncoder.encode(whatsappMessage.toString(), StandardCharsets.UTF_8);
        String whatsappLink = "https://wa.me/" + request.getWhatsappNumber() + "?text=" + encodedMessage;

        return new CheckoutResponse(
                "Order created successfully",
                whatsappLink,
                orderMapper.toDto(savedOrder)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new InvalidInputException("Order does not belong to user");
        }

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getUserOrders(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orderPage = orderDao.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        List<OrderResponse> content = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                orderPage.getNumber(),
                orderPage.getSize(),
                orderPage.getTotalElements(),
                orderPage.getTotalPages(),
                orderPage.isLast(),
                orderPage.isFirst()
        );
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setStatus(status);
        return orderMapper.toDto(orderDao.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderDao.findByStatus(status).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelOrder(Long userId, Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(userId)) {
            throw new InvalidInputException("Order does not belong to user");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidInputException("Can only cancel pending orders");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderDao.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getTopCustomers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return orderDao.findTopCustomers(pageable);
    }
}