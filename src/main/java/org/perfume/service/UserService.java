package org.perfume.service;

import org.perfume.model.dto.request.*;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.UserResponse;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getCurrentUser();
    UserResponse updateProfile(ProfileUpdateRequest request);
    void updatePassword(UpdatePasswordRequest request);
    void deleteAccount();
}