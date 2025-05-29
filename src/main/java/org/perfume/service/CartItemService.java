package org.perfume.service;

import org.perfume.model.dto.response.CartItemResponse;

import java.util.List;

public interface CartItemService {
    List<CartItemResponse> getCartItems(Long cartId);
    CartItemResponse addCartItem(Long cartId, Long productId, Integer quantity);
    CartItemResponse updateCartItem(Long cartId, Long productId, Integer quantity);
    void removeCartItem(Long cartId, Long productId);
    void removeAllCartItems(Long cartId);
}