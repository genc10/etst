package org.perfume.mapper;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Cart;
import org.perfume.model.dto.response.CartResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CartMapper implements EntityMapper<Cart, CartResponse> {

    private final CartItemMapper cartItemMapper;

    @Override
    public CartResponse toDto(Cart entity) {
        if (entity == null) {
            return null;
        }

        return new CartResponse(
                entity.getId(),
                entity.getItems().stream()
                        .map(cartItemMapper::toDto)
                        .collect(Collectors.toList()),
                entity.getTotalPrice(),
                entity.getTotalItems()
        );
    }

    @Override
    public Cart toEntity(CartResponse dto) {
        if (dto == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setId(dto.getId());
        return cart;
    }
}