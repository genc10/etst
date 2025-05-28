package org.perfume.service;

import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartResponse;

public interface CartService {
    CartResponse getCart();
    CartResponse addToCart(CartItemRequest request);
    CartResponse updateCartItem(Long cartItemId, CartItemRequest request);
    CartResponse removeFromCart(Long cartItemId);
    void clearCart();
}