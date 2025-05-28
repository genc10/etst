package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Cart;
import org.perfume.domain.entity.Order;
import org.perfume.domain.entity.OrderItem;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.OrderDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.exception.InvalidRequestException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.OrderMapper;
import org.perfume.model.dto.request.OrderRequest;
import org.perfume.model.dto.response.CheckoutResponse;
import org.perfume.model.dto.response.OrderResponse;
import org.perfume.model.enums.OrderStatus;
import org.perfume.service.CartService;
import org.perfume.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final PerfumeDao perfumeDao;
    private final CartService cartService;
    private final OrderMapper orderMapper;

    @Override
    public CheckoutResponse checkout(OrderRequest request) {
        Cart cart = getCurrentUserCart();
        if (cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cart is empty");
        }

        Order order = new Order();
        order.setUser(getCurrentUser());
        order.setTotalAmount(cart.getTotalPrice());
        order.setWhatsappNumber(request.getWhatsappNumber());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setCustomerNotes(request.getCustomerNotes());

        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setPerfume(cartItem.getPerfume());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(cartItem.getPerfume().getDiscountedPrice());
            orderItem.setProductName(cartItem.getPerfume().getName());
            orderItem.setBrandName(cartItem.getPerfume().getBrand().getName());
            order.getItems().add(orderItem);

            // Update stock
            cartItem.getPerfume().setStockQuantity(
                    cartItem.getPerfume().getStockQuantity() - cartItem.getQuantity()
            );
            perfumeDao.save(cartItem.getPerfume());
        });

        Order savedOrder = orderDao.save(order);
        cartService.clearCart();

        String whatsappLink = generateWhatsAppLink(savedOrder);
        return new CheckoutResponse(
                "Order placed successfully",
                whatsappLink,
                orderMapper.toDto(savedOrder)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(getCurrentUser().getId())) {
            throw new InvalidRequestException("Not authorized to view this order");
        }

        return orderMapper.toDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getUserOrders() {
        return orderDao.findByUserId(getCurrentUser().getId()).stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        // TODO: Add admin check
        return orderDao.findAll().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        // TODO: Add admin check
        Order order = orderDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        order.setStatus(status);
        return orderMapper.toDto(orderDao.save(order));
    }

    @Override
    public void cancelOrder(Long id) {
        Order order = orderDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getUser().getId().equals(getCurrentUser().getId())) {
            throw new InvalidRequestException("Not authorized to cancel this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException("Cannot cancel order in current status");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore stock
        order.getItems().forEach(orderItem -> {
            orderItem.getPerfume().setStockQuantity(
                    orderItem.getPerfume().getStockQuantity() + orderItem.getQuantity()
            );
            perfumeDao.save(orderItem.getPerfume());
        });

        orderDao.save(order);
    }

    private String generateWhatsAppLink(Order order) {
        String message = String.format(
                "New order #%d%nTotal: $%.2f%nDelivery Address: %s%nNotes: %s",
                order.getId(),
                order.getTotalAmount(),
                order.getDeliveryAddress(),
                order.getCustomerNotes()
        );

        return String.format(
                "https://wa.me/%s?text=%s",
                order.getWhatsappNumber().replaceAll("[^0-9]", ""),
                message.replace(" ", "%20")
        );
    }

    private Cart getCurrentUserCart() {
        // TODO: Implement
        return new Cart();
    }

    private User getCurrentUser() {
        // TODO: Implement
        return new User();
    }
}