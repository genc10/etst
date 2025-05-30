package org.perfume.service.impl;

import lombok.RequiredArgsConstructor;
import org.perfume.domain.entity.User;
import org.perfume.domain.repo.UserDao;
import org.perfume.exception.AlreadyExistsException;
import org.perfume.exception.InvalidInputException;
import org.perfume.exception.NotFoundException;
import org.perfume.mapper.UserMapper;
import org.perfume.model.dto.request.LoginRequest;
import org.perfume.model.dto.request.ProfileUpdateRequest;
import org.perfume.model.dto.request.RegisterRequest;
import org.perfume.model.dto.request.UpdatePasswordRequest;
import org.perfume.model.dto.response.AuthResponse;
import org.perfume.model.dto.response.UserResponse;
import org.perfume.model.enums.UserRole;
import org.perfume.security.JwtUtils;
import org.perfume.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userDao.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

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
        user.setRole(UserRole.USER);

        User savedUser = userDao.save(user);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(jwt, userMapper.toDto(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = (User) authentication.getPrincipal();
        String jwt = jwtUtils.generateJwtToken(authentication);

        return new AuthResponse(jwt, userMapper.toDto(user));
    }

    @Override
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userDao.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!user.getEmail().equals(request.getEmail()) && userDao.existsByEmail(request.getEmail())) {
            throw new AlreadyExistsException("Email already in use");
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
            throw new InvalidInputException("New passwords don't match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDao.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userDao.existsById(userId)) {
            throw new NotFoundException("User not found");
        }
        userDao.deleteById(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        return userMapper.toDto(userDao.findById(userId)
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
    public Optional<User> findByEmail(String email) {
        return userDao.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userDao.save(user);
    }
}