package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Cart;
import org.perfume.domain.entity.CartItem;
import org.perfume.domain.entity.Perfume;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.CartDao;
import org.perfume.domain.repo.CartItemDao;
import org.perfume.domain.repo.PerfumeDao;
import org.perfume.exception.InvalidRequestException;
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
    private final PerfumeDao perfumeDao;
    private final CartMapper cartMapper;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart() {
        Cart cart = getCurrentUserCart();
        return cartMapper.toDto(cart);
    }

    @Override
    public CartResponse addToCart(CartItemRequest request) {
        Cart cart = getCurrentUserCart();
        Perfume perfume = getPerfume(request.getProductId());

        if (request.getQuantity() > perfume.getStockQuantity()) {
            throw new InvalidRequestException("Not enough stock");
        }

        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getPerfume().getId().equals(request.getProductId()))
                .findFirst()
                .orElse(new CartItem());

        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setPerfume(perfume);
            cartItem.setQuantity(request.getQuantity());
            cart.getItems().add(cartItem);
        } else {
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            if (newQuantity > perfume.getStockQuantity()) {
                throw new InvalidRequestException("Not enough stock");
            }
            cartItem.setQuantity(newQuantity);
        }

        return cartMapper.toDto(cartDao.save(cart));
    }

    @Override
    public CartResponse updateCartItem(Long cartItemId, CartItemRequest request) {
        Cart cart = getCurrentUserCart();
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (request.getQuantity() > cartItem.getPerfume().getStockQuantity()) {
            throw new InvalidRequestException("Not enough stock");
        }

        cartItem.setQuantity(request.getQuantity());
        return cartMapper.toDto(cartDao.save(cart));
    }

    @Override
    public CartResponse removeFromCart(Long cartItemId) {
        Cart cart = getCurrentUserCart();
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        return cartMapper.toDto(cartDao.save(cart));
    }

    @Override
    public void clearCart() {
        Cart cart = getCurrentUserCart();
        cart.getItems().clear();
        cartDao.save(cart);
    }

    private Cart getCurrentUserCart() {
        // TODO: Implement user authentication and get current user
        User currentUser = new User(); // Temporary placeholder
        return cartDao.findByUserId(currentUser.getId())
                .orElseThrow(() -> new NotFoundException("Cart not found"));
    }

    private Perfume getPerfume(Long id) {
        return perfumeDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Perfume not found"));
    }
}