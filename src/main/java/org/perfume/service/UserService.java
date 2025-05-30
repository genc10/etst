package org.perfume.service;

import org.perfume.domain.entity.User;
import org.perfume.model.dto.request.LoginRequest;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.RegisterRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.UserResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);
    void updatePassword(Long userId, UpdatePasswordRequest request);
    void deleteUser(Long userId);
    UserResponse getUserById(Long userId);
    List<UserResponse> getAllUsers();
    Optional<User> findByEmail(String email);
    User save(User user);
}