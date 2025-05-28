package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.Cart;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.CartDao;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.InvalidRequestException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.UserMapper;
import org.perfume.model.dto.request.LoginRequest;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.RegisterRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final CartDao cartDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userDao.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());

        User savedUser = userDao.save(user);

        Cart cart = new Cart();
        cart.setUser(savedUser);
        cartDao.save(cart);

        // TODO: Generate JWT token
        String token = "dummy-token";

        return new AuthResponse(token, userMapper.toDto(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userDao.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidRequestException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidRequestException("Invalid credentials");
        }

        // TODO: Generate JWT token
        String token = "dummy-token";

        return new AuthResponse(token, userMapper.toDto(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return userMapper.toDto(getCurrentUserEntity());
    }

    @Override
    public User getCurrentUserEntity() {
        // TODO: Implement
        return new User();
    }

    @Override
    public UserResponse updateProfile(ProfileUpdateRequest request) {
        User user = getCurrentUserEntity();

        if (request.getEmail() != null &&
                !request.getEmail().equals(user.getEmail()) &&
                userDao.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already registered");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        return userMapper.toDto(userDao.save(user));
    }

    @Override
    public void updatePassword(UpdatePasswordRequest request) {
        User user = getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidRequestException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        // TODO: Add admin check
        return userDao.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        // TODO: Add admin check
        if (!userDao.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userDao.deleteById(id);
    }
}