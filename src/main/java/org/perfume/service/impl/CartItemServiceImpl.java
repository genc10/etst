package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.CartItem;
import org.perfume.domain.repo.CartItemDao;
import org.perfume.mapper.CartItemMapper;
import org.perfume.model.dto.response.CartItemResponse;
import org.perfume.service.CartItemService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartItemServiceImpl implements CartItemService {

    private final CartItemDao cartItemDao;
    private final CartItemMapper cartItemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Long cartId) {
        return cartItemDao.findByCartId(cartId).stream()
                .map(cartItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CartItemResponse addCartItem(Long cartId, Long productId, Integer quantity) {
        CartItem cartItem = cartItemDao.findByCartIdAndPerfumeId(cartId, productId)
                .orElse(new CartItem());
        cartItem.setQuantity(quantity);
        return cartItemMapper.toDto(cartItemDao.save(cartItem));
    }

    @Override
    public CartItemResponse updateCartItem(Long cartId, Long productId, Integer quantity) {
        cartItemDao.updateQuantity(cartId, productId, quantity);
        return cartItemMapper.toDto(cartItemDao.findByCartIdAndPerfumeId(cartId, productId).orElseThrow());
    }

    @Override
    public void removeCartItem(Long cartId, Long productId) {
        cartItemDao.deleteByCartIdAndPerfumeId(cartId, productId);
    }

    @Override
    public void removeAllCartItems(Long cartId) {
        cartItemDao.deleteByCartId(cartId);
    }
}