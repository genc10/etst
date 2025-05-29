package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.UserMapper;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.UserRole;
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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        return userMapper.toDto(userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    @Override
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userDao.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already exists");
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        return userMapper.toDto(userDao.save(user));
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidInputException("Current password is incorrect");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidInputException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return userMapper.toDto(userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userDao.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(UserRole role) {
        return userDao.findByRole(role).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long id) {
        if (!userDao.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        userDao.deleteById(id);
    }
}