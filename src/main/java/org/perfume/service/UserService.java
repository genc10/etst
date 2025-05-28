package org.perfume.service;

import org.perfume.model.dto.request.LoginRequest;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.RegisterRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser();
    UserResponse updateProfile(ProfileUpdateRequest request);
    void updatePassword(UpdatePasswordRequest request);
    List<UserResponse> getAllUsers();
    void deleteUser(Long id);
}