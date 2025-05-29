package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Cart;
import org.perfume.domain.entity.CartItem;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.CartDao;
import org.perfume.domain.repo.CartItemDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.CartMapper;
import org.perfume.model.dto.request.CartItemRequest;
import org.perfume.model.dto.response.CartResponse;
import org.perfume.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartDao cartDao;
    private final CartItemDao cartItemDao;
    private final UserDao userDao;
    private final PerfumeDao perfumeDao;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        return cartMapper.toDto(cart);
    }

    @Override
    public CartResponse addToCart(Long userId, CartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        Perfume perfume = perfumeDao.findById(request.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (perfume.getStockQuantity() < request.getQuantity()) {
            throw new InvalidInputException("Not enough stock");
        }

        CartItem cartItem = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), perfume.getId())
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setPerfume(perfume);
            cartItem.setQuantity(request.getQuantity());
        } else {
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        }

        cartItemDao.save(cartItem);
        return cartMapper.toDto(cart);
    }

    @Override
    public CartResponse updateCartItem(Long userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        CartItem cartItem = cartItemDao.findByCartIdAndPerfumeId(cart.getId(), productId)
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (cartItem.getPerfume().getStockQuantity() < quantity) {
            throw new InvalidInputException("Not enough stock");
        }

        cartItem.setQuantity(quantity);
        cartItemDao.save(cartItem);
        return cartMapper.toDto(cart);
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        Cart cart = getOrCreateCart(userId);
        cartItemDao.deleteByCartIdAndPerfumeId(cart.getId(), productId);
    }

    @Override
    public void clearCart(Long userId) {
        Cart cart = getOrCreateCart(userId);
        cartItemDao.deleteByCartId(cart.getId());
    }

    private Cart getOrCreateCart(Long userId) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return cartDao.findUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartDao.save(newCart);
                });
    }
}