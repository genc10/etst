package org.perfume.service;

import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.UserRole;

import java.util.List;

public interface UserService {
    UserResponse getCurrentUser(Long userId);
    UserResponse updateProfile(Long userId, ProfileUpdateRequest request);
    void updatePassword(Long userId, UpdatePasswordRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(UserRole role);
    void deleteUser(Long id);
}