package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.*;
import org.perfume.domain.repo.CartDao;
import org.perfume.domain.repo.OrderDao;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.OrderMapper;
import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.enums.OrderStatus;
import org.perfume.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final CartDao cartDao;
    private final UserDao userDao;
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
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderDao.save(order);

        String whatsappMessage = createWhatsappMessage(savedOrder);
        String whatsappLink = String.format("https://wa.me/%s?text=%s",
                savedOrder.getWhatsappNumber().replaceAll("[^0-9]", ""),
                whatsappMessage);

        return new CheckoutResponse(
                "Order created successfully",
                whatsappLink,
                orderMapper.toDto(savedOrder)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId) {
        Order order = orderDao.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders(Long userId) {
        return orderDao.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderDao.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(status);
        return orderMapper.toDto(orderDao.save(order));
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderDao.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        order.setStatus(OrderStatus.CANCELLED);
        orderDao.save(order);
    }

    private String createWhatsappMessage(Order order) {
        StringBuilder message = new StringBuilder();
        message.append("New Order #").append(order.getId()).append("\n\n");
        message.append("Products:\n");
        
        for (OrderItem item : order.getItems()) {
            message.append("- ")
                    .append(item.getProductName())
                    .append(" (").append(item.getBrandName()).append(")")
                    .append(" x").append(item.getQuantity())
                    .append(" = ").append(item.getSubtotal())
                    .append("\n");
        }
        
        message.append("\nTotal Amount: ").append(order.getTotalAmount())
                .append("\nDelivery Address: ").append(order.getDeliveryAddress());

        if (order.getCustomerNotes() != null && !order.getCustomerNotes().isEmpty()) {
            message.append("\nNotes: ").append(order.getCustomerNotes());
        }

        return message.toString().replace(" ", "+");
    }
}