package org.perfume.service;

import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart(Long userId);
    CartResponse addToCart(Long userId, CartItemRequest request);
    CartResponse updateCartItem(Long userId, Long productId, Integer quantity);
    void removeFromCart(Long userId, Long productId);
    void clearCart(Long userId);
}